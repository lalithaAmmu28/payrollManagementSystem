package com.pms.backend.controller;

import com.pms.backend.dto.ApiResponse;
import com.pms.backend.dto.payroll.PayrollItemResponse;
import com.pms.backend.dto.payroll.PayrollRunCreateRequest;
import com.pms.backend.dto.payroll.PayrollRunResponse;
import com.pms.backend.entity.User;
import com.pms.backend.exception.ResourceNotFoundException;
import com.pms.backend.repository.UserRepository;
import com.pms.backend.service.EmployeeService;
import com.pms.backend.service.PayrollService;
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
@RequestMapping("/api/v1/payroll")
@Tag(name = "Payroll Management", description = "Core payroll processing and payslip management")
@SecurityRequirement(name = "Bearer Authentication")
public class PayrollController {

    private final PayrollService payrollService;
    private final EmployeeService employeeService;
    private final UserRepository userRepository;

    @Autowired
    public PayrollController(PayrollService payrollService, 
                           EmployeeService employeeService,
                           UserRepository userRepository) {
        this.payrollService = payrollService;
        this.employeeService = employeeService;
        this.userRepository = userRepository;
    }

    // === ADMIN PAYROLL MANAGEMENT ENDPOINTS ===

    @PostMapping("/runs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create payroll run", 
               description = "Create a new payroll run for a specific year and month (Admin only)")
    public ResponseEntity<ApiResponse<PayrollRunResponse>> createPayrollRun(
            @Valid @RequestBody PayrollRunCreateRequest request) {
        
        PayrollRunResponse response = payrollService.createPayrollRun(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Payroll run created successfully", response));
    }

    @GetMapping("/runs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all payroll runs", 
               description = "Retrieve all payroll runs with summary information (Admin only)")
    public ResponseEntity<ApiResponse<List<PayrollRunResponse>>> getAllPayrollRuns() {
        
        List<PayrollRunResponse> runs = payrollService.getAllPayrollRuns();
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Payroll runs retrieved successfully", runs));
    }

    @GetMapping("/runs/{runId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get payroll run by ID", 
               description = "Retrieve detailed information about a specific payroll run (Admin only)")
    public ResponseEntity<ApiResponse<PayrollRunResponse>> getPayrollRunById(
            @Parameter(description = "Payroll run ID") @PathVariable String runId) {
        
        PayrollRunResponse run = payrollService.getPayrollRunById(runId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Payroll run retrieved successfully", run));
    }

    @PostMapping("/runs/{runId}/process")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Process payroll run", 
               description = "Execute the core payroll calculation engine with salary structures, bonuses, and deductions (Admin only)")
    public ResponseEntity<ApiResponse<PayrollRunResponse>> processPayrollRun(
            @Parameter(description = "Payroll run ID") @PathVariable String runId) {
        
        PayrollRunResponse response = payrollService.processPayrollRun(runId);
        
        String message = "Payroll run processed successfully";
        if (response.getEmployeeCount() != null) {
            message += String.format(" (%d employees processed)", response.getEmployeeCount());
        }
        
        return ResponseEntity.ok(new ApiResponse<>(true, message, response));
    }

    @PostMapping("/runs/{runId}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lock payroll run", 
               description = "Lock a processed payroll run and set pay dates for all employees (Admin only)")
    public ResponseEntity<ApiResponse<PayrollRunResponse>> lockPayrollRun(
            @Parameter(description = "Payroll run ID") @PathVariable String runId) {
        
        PayrollRunResponse response = payrollService.lockPayrollRun(runId);
        
        String message = "Payroll run locked successfully. Payslips are now available to employees.";
        
        return ResponseEntity.ok(new ApiResponse<>(true, message, response));
    }

    @GetMapping("/runs/{runId}/items")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get payroll items for run", 
               description = "Retrieve all payroll items (employee payslips) for a specific run (Admin only)")
    public ResponseEntity<ApiResponse<List<PayrollItemResponse>>> getPayrollItemsForRun(
            @Parameter(description = "Payroll run ID") @PathVariable String runId) {
        
        List<PayrollItemResponse> items = payrollService.getPayrollItemsForRun(runId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, 
                String.format("Payroll items retrieved successfully (%d items)", items.size()), items));
    }

    @GetMapping("/runs/{runId}/employees/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get individual employee payroll item for Admin verification", 
               description = "Retrieve specific employee's payroll item details for a run to verify before locking (Admin only)")
    public ResponseEntity<ApiResponse<PayrollItemResponse>> getEmployeePayrollItemForAdmin(
            @Parameter(description = "Payroll run ID") @PathVariable String runId,
            @Parameter(description = "Employee ID") @PathVariable String employeeId) {
        
        PayrollItemResponse item = payrollService.getEmployeePayrollItemForAdmin(runId, employeeId);
        
        String message = String.format("Payroll item retrieved successfully for employee %s in run %s", 
                                       employeeId, runId);
        return ResponseEntity.ok(new ApiResponse<>(true, message, item));
    }

    @GetMapping("/runs/{runId}/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get payroll statistics", 
               description = "Get detailed statistics and summary for a payroll run (Admin only)")
    public ResponseEntity<ApiResponse<PayrollRunResponse>> getPayrollStatistics(
            @Parameter(description = "Payroll run ID") @PathVariable String runId) {
        
        PayrollRunResponse statistics = payrollService.getPayrollStatistics(runId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Payroll statistics retrieved successfully", statistics));
    }

    // === EMPLOYEE PAYSLIP ENDPOINTS ===

    @GetMapping("/payslips")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my payslips", 
               description = "Retrieve all available payslips for the current employee")
    public ResponseEntity<ApiResponse<List<PayrollItemResponse>>> getMyPayslips() {
        
        String currentUserId = getCurrentUserId();
        String employeeId = getEmployeeIdByUserId(currentUserId);
        
        List<PayrollItemResponse> payslips = payrollService.getEmployeePayslips(employeeId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, 
                String.format("Payslips retrieved successfully (%d payslips)", payslips.size()), payslips));
    }

    @GetMapping("/payslips/{runId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my payslip for specific run", 
               description = "Retrieve payslip for a specific payroll run (only available for locked runs)")
    public ResponseEntity<ApiResponse<PayrollItemResponse>> getMyPayslipForRun(
            @Parameter(description = "Payroll run ID") @PathVariable String runId) {
        
        String currentUserId = getCurrentUserId();
        String employeeId = getEmployeeIdByUserId(currentUserId);
        
        PayrollItemResponse payslip = payrollService.getEmployeePayslip(runId, employeeId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Payslip retrieved successfully", payslip));
    }

    // === ADMIN EMPLOYEE PAYSLIP ACCESS ===

    @GetMapping("/employees/{employeeId}/payslips")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get employee payslips", 
               description = "Retrieve all payslips for a specific employee (Admin only)")
    public ResponseEntity<ApiResponse<List<PayrollItemResponse>>> getEmployeePayslips(
            @Parameter(description = "Employee ID") @PathVariable String employeeId) {
        
        List<PayrollItemResponse> payslips = payrollService.getEmployeePayslips(employeeId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, 
                String.format("Employee payslips retrieved successfully (%d payslips)", payslips.size()), payslips));
    }

    @GetMapping("/employees/{employeeId}/payslips/{runId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get employee payslip for specific run", 
               description = "Retrieve a specific employee's payslip for a payroll run (Admin only)")
    public ResponseEntity<ApiResponse<PayrollItemResponse>> getEmployeePayslipForRun(
            @Parameter(description = "Employee ID") @PathVariable String employeeId,
            @Parameter(description = "Payroll run ID") @PathVariable String runId) {
        
        PayrollItemResponse payslip = payrollService.getEmployeePayslip(runId, employeeId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Employee payslip retrieved successfully", payslip));
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