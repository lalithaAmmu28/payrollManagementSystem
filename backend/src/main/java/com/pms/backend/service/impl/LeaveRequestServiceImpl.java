package com.pms.backend.service.impl;

import com.pms.backend.dto.leave.LeaveRequestCreateDto;
import com.pms.backend.dto.leave.LeaveRequestResponseDto;
import com.pms.backend.dto.leave.LeaveStatusUpdateDto;
import com.pms.backend.entity.Employee;
import com.pms.backend.entity.LeaveRequest;
import com.pms.backend.entity.enums.LeaveStatus;
import com.pms.backend.entity.enums.LeaveType;
import com.pms.backend.exception.BadRequestException;
import com.pms.backend.exception.ResourceNotFoundException;
import com.pms.backend.repository.EmployeeRepository;
import com.pms.backend.repository.LeaveRequestRepository;
import com.pms.backend.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeaveRequestServiceImpl implements LeaveRequestService {
    
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    
    @Autowired
    public LeaveRequestServiceImpl(LeaveRequestRepository leaveRequestRepository, 
                                 EmployeeRepository employeeRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
    }
    
    @Override
    public LeaveRequestResponseDto applyForLeave(String employeeId, LeaveRequestCreateDto requestDto) {
        // Validate employee exists
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));
        
        // Validate leave request constraints
        validateLeaveRequest(employeeId, requestDto);
        
        // CRITICAL BUSINESS LOGIC: Check paid leave balance
        if (LeaveType.Paid.equals(requestDto.getLeaveType())) {
            long requestedDays = requestDto.getDurationInDays();
            BigDecimal currentBalance = employee.getLeaveBalance();
            
            if (currentBalance.compareTo(BigDecimal.valueOf(requestedDays)) < 0) {
                throw new BadRequestException(
                    String.format("Insufficient paid leave balance. Requested: %d days, Available: %.2f days", 
                                requestedDays, currentBalance)
                );
            }
        }
        
        // Create leave request
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployeeId(employeeId);
        leaveRequest.setLeaveType(requestDto.getLeaveType());
        leaveRequest.setStartDate(requestDto.getStartDate());
        leaveRequest.setEndDate(requestDto.getEndDate());
        leaveRequest.setReason(requestDto.getReason());
        leaveRequest.setStatus(LeaveStatus.Pending);
        
        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        
        return convertToResponseDto(savedRequest);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestResponseDto> getLeaveRequestsForEmployee(String employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }
        
        List<LeaveRequest> requests = leaveRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
        return requests.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestResponseDto> getAllLeaveRequests() {
        List<LeaveRequest> requests = leaveRequestRepository.findAllByOrderByCreatedAtDesc();
        return requests.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestResponseDto> getLeaveRequestsByStatus(LeaveStatus status) {
        List<LeaveRequest> requests = leaveRequestRepository.findByStatusOrderByCreatedAtAsc(status);
        return requests.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public LeaveRequestResponseDto getLeaveRequestById(String leaveId) {
        LeaveRequest request = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + leaveId));
        
        return convertToResponseDto(request);
    }
    
    @Override
    public LeaveRequestResponseDto updateLeaveStatus(String leaveId, LeaveStatusUpdateDto updateDto) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + leaveId));
        
        // Validate status transition
        if (!leaveRequest.isPending()) {
            throw new BadRequestException("Only pending leave requests can have their status updated");
        }
        
        LeaveStatus oldStatus = leaveRequest.getStatus();
        LeaveStatus newStatus = updateDto.getStatus();
        
        // Update status
        leaveRequest.setStatus(newStatus);
        
        // CRITICAL BUSINESS LOGIC: Handle approved paid leave
        if (LeaveStatus.Approved.equals(newStatus) && leaveRequest.isPaidLeave()) {
            deductLeaveBalance(leaveRequest);
        }
        
        LeaveRequest updatedRequest = leaveRequestRepository.save(leaveRequest);
        
        return convertToResponseDto(updatedRequest);
    }
    
    @Override
    public void cancelLeaveRequest(String leaveId, String employeeId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + leaveId));
        
        // Validate ownership
        if (!employeeId.equals(leaveRequest.getEmployeeId())) {
            throw new BadRequestException("You can only cancel your own leave requests");
        }
        
        // Validate status
        if (!leaveRequest.isPending()) {
            throw new BadRequestException("Only pending leave requests can be cancelled");
        }
        
        leaveRequestRepository.delete(leaveRequest);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean employeeExists(String employeeId) {
        return employeeRepository.existsById(employeeId);
    }
    
    @Override
    public void validateLeaveRequest(String employeeId, LeaveRequestCreateDto requestDto) {
        // Validate dates
        if (requestDto.getStartDate().isAfter(requestDto.getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }
        
        // Check for overlapping leave requests
        List<LeaveRequest> overlappingRequests = leaveRequestRepository.findOverlappingLeaveRequests(
                employeeId, 
                requestDto.getStartDate(), 
                requestDto.getEndDate(), 
                null // No exclusion for new requests
        );
        
        if (!overlappingRequests.isEmpty()) {
            throw new BadRequestException(
                "You already have pending or approved leave requests that overlap with the requested dates"
            );
        }
        
        // Validate reasonable duration (e.g., max 30 days)
        long duration = requestDto.getDurationInDays();
        if (duration > 30) {
            throw new BadRequestException("Leave requests cannot exceed 30 days");
        }
        
        if (duration <= 0) {
            throw new BadRequestException("Leave duration must be at least 1 day");
        }
    }
    
    /**
     * CRITICAL BUSINESS LOGIC: Deduct leave balance for approved paid leave
     * This method must be called within a transaction
     */
    private void deductLeaveBalance(LeaveRequest leaveRequest) {
        Employee employee = employeeRepository.findById(leaveRequest.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        
        long leaveDays = leaveRequest.getDurationInDays();
        BigDecimal currentBalance = employee.getLeaveBalance();
        BigDecimal newBalance = currentBalance.subtract(BigDecimal.valueOf(leaveDays));
        
        // Ensure balance doesn't go negative (safety check)
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(
                String.format("Cannot approve leave. Would result in negative balance. Current: %.2f, Requested: %d", 
                            currentBalance, leaveDays)
            );
        }
        
        employee.setLeaveBalance(newBalance);
        employeeRepository.save(employee);
        
        System.out.println(String.format(
            "Leave balance deducted for employee %s: %.2f -> %.2f (-%d days)",
            employee.getEmployeeId(), currentBalance, newBalance, leaveDays
        ));
    }
    
    /**
     * Convert LeaveRequest entity to LeaveRequestResponseDto
     */
    private LeaveRequestResponseDto convertToResponseDto(LeaveRequest request) {
        LeaveRequestResponseDto dto = new LeaveRequestResponseDto();
        
        dto.setLeaveId(request.getLeaveId());
        dto.setEmployeeId(request.getEmployeeId());
        dto.setLeaveType(request.getLeaveType());
        dto.setStartDate(request.getStartDate());
        dto.setEndDate(request.getEndDate());
        dto.setStatus(request.getStatus());
        dto.setReason(request.getReason());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());
        dto.setDurationInDays(request.getDurationInDays());
        
        // Add employee information if available
        if (request.getEmployee() != null) {
            Employee employee = request.getEmployee();
            dto.setEmployeeName(employee.getFirstName() + " " + employee.getLastName());
            
            if (employee.getUser() != null) {
                dto.setEmployeeEmail(employee.getUser().getEmail());
            }
            
            if (employee.getDepartment() != null) {
                dto.setDepartmentName(employee.getDepartment().getDepartmentName());
            }
        } else {
            // Fallback: fetch employee separately if lazy loading fails
            employeeRepository.findById(request.getEmployeeId()).ifPresent(employee -> {
                dto.setEmployeeName(employee.getFirstName() + " " + employee.getLastName());
                
                if (employee.getUser() != null) {
                    dto.setEmployeeEmail(employee.getUser().getEmail());
                }
                
                if (employee.getDepartment() != null) {
                    dto.setDepartmentName(employee.getDepartment().getDepartmentName());
                }
            });
        }
        
        return dto;
    }
}