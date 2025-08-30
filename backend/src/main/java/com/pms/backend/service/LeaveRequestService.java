package com.pms.backend.service;

import com.pms.backend.dto.leave.LeaveRequestCreateDto;
import com.pms.backend.dto.leave.LeaveRequestResponseDto;
import com.pms.backend.dto.leave.LeaveStatusUpdateDto;
import com.pms.backend.entity.enums.LeaveStatus;

import java.util.List;

public interface LeaveRequestService {
    
    /**
     * Apply for leave (Employee self-service)
     * Includes business logic for paid leave balance validation
     */
    LeaveRequestResponseDto applyForLeave(String employeeId, LeaveRequestCreateDto requestDto);
    
    /**
     * Get all leave requests for a specific employee
     */
    List<LeaveRequestResponseDto> getLeaveRequestsForEmployee(String employeeId);
    
    /**
     * Get all leave requests (Admin only)
     */
    List<LeaveRequestResponseDto> getAllLeaveRequests();
    
    /**
     * Get leave requests by status (Admin only)
     */
    List<LeaveRequestResponseDto> getLeaveRequestsByStatus(LeaveStatus status);
    
    /**
     * Get leave request by ID
     */
    LeaveRequestResponseDto getLeaveRequestById(String leaveId);
    
    /**
     * Update leave request status (Admin only)
     * Includes critical business logic for leave balance deduction
     */
    LeaveRequestResponseDto updateLeaveStatus(String leaveId, LeaveStatusUpdateDto updateDto);
    
    /**
     * Cancel leave request (Employee self-service, only if pending)
     */
    void cancelLeaveRequest(String leaveId, String employeeId);
    
    /**
     * Check if employee exists
     */
    boolean employeeExists(String employeeId);
    
    /**
     * Validate leave request constraints (overlap, balance, etc.)
     */
    void validateLeaveRequest(String employeeId, LeaveRequestCreateDto requestDto);
}