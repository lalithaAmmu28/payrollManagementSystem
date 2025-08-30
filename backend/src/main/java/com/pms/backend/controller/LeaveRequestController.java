package com.pms.backend.controller;

import com.pms.backend.dto.ApiResponse;
import com.pms.backend.dto.leave.LeaveRequestCreateDto;
import com.pms.backend.dto.leave.LeaveRequestResponseDto;
import com.pms.backend.dto.leave.LeaveStatusUpdateDto;
import com.pms.backend.entity.User;
import com.pms.backend.entity.enums.LeaveStatus;
import com.pms.backend.exception.ResourceNotFoundException;
import com.pms.backend.repository.UserRepository;
import com.pms.backend.service.EmployeeService;
import com.pms.backend.service.LeaveRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leave-requests")
@Tag(name = "Leave Management", description = "Endpoints for managing leave requests")
@SecurityRequirement(name = "Bearer Authentication")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;
    private final EmployeeService employeeService;
    private final UserRepository userRepository;

    @Autowired
    public LeaveRequestController(LeaveRequestService leaveRequestService, 
                                EmployeeService employeeService,
                                UserRepository userRepository) {
        this.leaveRequestService = leaveRequestService;
        this.employeeService = employeeService;
        this.userRepository = userRepository;
    }

    // === EMPLOYEE ENDPOINTS ===

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('ADMIN')")
    @Operation(summary = "Apply for leave", 
               description = "Submit a new leave request (Employees and Admins)")
    public ResponseEntity<ApiResponse<LeaveRequestResponseDto>> applyForLeave(
            @Valid @RequestBody LeaveRequestCreateDto requestDto) {
        
        String currentUserId = getCurrentUserId();
        String employeeId = getEmployeeIdByUserId(currentUserId);
        
        LeaveRequestResponseDto response = leaveRequestService.applyForLeave(employeeId, requestDto);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Leave request submitted successfully", response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('ADMIN')")
    @Operation(summary = "Get my leave requests", 
               description = "Retrieve all leave requests for the current user")
    public ResponseEntity<ApiResponse<List<LeaveRequestResponseDto>>> getMyLeaveRequests() {
        
        String currentUserId = getCurrentUserId();
        String employeeId = getEmployeeIdByUserId(currentUserId);
        
        List<LeaveRequestResponseDto> requests = leaveRequestService.getLeaveRequestsForEmployee(employeeId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Leave requests retrieved successfully", requests));
    }

    @DeleteMapping("/{leaveId}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('ADMIN')")
    @Operation(summary = "Cancel leave request", 
               description = "Cancel a pending leave request (only your own)")
    public ResponseEntity<ApiResponse<Void>> cancelLeaveRequest(
            @Parameter(description = "Leave request ID") @PathVariable String leaveId) {
        
        String currentUserId = getCurrentUserId();
        String employeeId = getEmployeeIdByUserId(currentUserId);
        
        leaveRequestService.cancelLeaveRequest(leaveId, employeeId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Leave request cancelled successfully", null));
    }

    // === ADMIN ENDPOINTS ===

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all leave requests", 
               description = "Retrieve all leave requests in the system (Admin only)")
    public ResponseEntity<ApiResponse<List<LeaveRequestResponseDto>>> getAllLeaveRequests(
            @Parameter(description = "Filter by status (optional)") 
            @RequestParam(required = false) LeaveStatus status) {
        
        List<LeaveRequestResponseDto> requests;
        
        if (status != null) {
            requests = leaveRequestService.getLeaveRequestsByStatus(status);
        } else {
            requests = leaveRequestService.getAllLeaveRequests();
        }
        
        String message = status != null ? 
            "Leave requests with status '" + status + "' retrieved successfully" :
            "All leave requests retrieved successfully";
        
        return ResponseEntity.ok(new ApiResponse<>(true, message, requests));
    }

    @GetMapping("/{leaveId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get leave request by ID", 
               description = "Retrieve a specific leave request (Admin only)")
    public ResponseEntity<ApiResponse<LeaveRequestResponseDto>> getLeaveRequestById(
            @Parameter(description = "Leave request ID") @PathVariable String leaveId) {
        
        LeaveRequestResponseDto request = leaveRequestService.getLeaveRequestById(leaveId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Leave request retrieved successfully", request));
    }

    @PatchMapping("/{leaveId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update leave request status", 
               description = "Approve or reject a leave request (Admin only). Approving paid leave will deduct from employee's balance.")
    public ResponseEntity<ApiResponse<LeaveRequestResponseDto>> updateLeaveStatus(
            @Parameter(description = "Leave request ID") @PathVariable String leaveId,
            @Valid @RequestBody LeaveStatusUpdateDto updateDto) {
        
        LeaveRequestResponseDto response = leaveRequestService.updateLeaveStatus(leaveId, updateDto);
        
        String message = "Leave request " + updateDto.getStatus().name().toLowerCase() + " successfully";
        if (updateDto.isApproval() && response.isPaidLeave()) {
            message += ". Employee's leave balance has been updated.";
        }
        
        return ResponseEntity.ok(new ApiResponse<>(true, message, response));
    }

    // === HELPER METHODS ===

    /**
     * Get the current user's ID from the security context
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("No authenticated user found");
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        return user.getUserId();
    }

    /**
     * Get employee ID by user ID
     */
    private String getEmployeeIdByUserId(String userId) {
        try {
            return employeeService.getEmployeeByUserId(userId).getEmployeeId();
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Employee record not found for user: " + userId);
        }
    }
}