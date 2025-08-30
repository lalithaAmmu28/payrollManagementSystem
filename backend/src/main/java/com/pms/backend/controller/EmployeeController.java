package com.pms.backend.controller;

import com.pms.backend.dto.ApiResponse;
import com.pms.backend.dto.employee.EmployeeCreateRequest;
import com.pms.backend.dto.employee.EmployeeResponse;
import com.pms.backend.dto.employee.EmployeeUpdateRequest;
import com.pms.backend.dto.salary.SalaryStructureRequest;
import com.pms.backend.dto.salary.SalaryStructureResponse;
import com.pms.backend.service.EmployeeService;
import com.pms.backend.service.SalaryStructureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employee Management", description = "Admin-only endpoints for managing employees")
@SecurityRequirement(name = "Bearer Authentication")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final SalaryStructureService salaryStructureService;

    @Autowired
    public EmployeeController(EmployeeService employeeService, SalaryStructureService salaryStructureService) {
        this.employeeService = employeeService;
        this.salaryStructureService = salaryStructureService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new employee", 
               description = "Create a new employee with associated user account (Admin only)")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody EmployeeCreateRequest request) {
        
        EmployeeResponse employee = employeeService.createEmployee(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Employee created successfully", employee));
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get employee by ID", 
               description = "Retrieve a specific employee by ID (Admin only)")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(
            @Parameter(description = "Employee ID") @PathVariable String employeeId) {
        
        EmployeeResponse employee = employeeService.getEmployeeById(employeeId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Employee retrieved successfully", employee));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all employees", 
               description = "Retrieve all employees (Admin only)")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getAllEmployees() {
        
        List<EmployeeResponse> employees = employeeService.getAllEmployees();
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Employees retrieved successfully", employees));
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update employee", 
               description = "Update an existing employee (Admin only)")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @Parameter(description = "Employee ID") @PathVariable String employeeId,
            @Valid @RequestBody EmployeeUpdateRequest request) {
        
        EmployeeResponse employee = employeeService.updateEmployee(employeeId, request);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Employee updated successfully", employee));
    }

    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete employee", 
               description = "Delete an employee and associated user account (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(
            @Parameter(description = "Employee ID") @PathVariable String employeeId) {
        
        employeeService.deleteEmployee(employeeId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Employee deleted successfully", null));
    }

    // === SALARY STRUCTURE ENDPOINTS ===

    @GetMapping("/{employeeId}/salary-structures")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get employee salary history", 
               description = "Retrieve the complete salary structure history for an employee (Admin only)")
    public ResponseEntity<ApiResponse<List<SalaryStructureResponse>>> getEmployeeSalaryHistory(
            @Parameter(description = "Employee ID") @PathVariable String employeeId) {
        
        List<SalaryStructureResponse> salaryHistory = salaryStructureService.getStructureHistoryForEmployee(employeeId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Salary history retrieved successfully", salaryHistory));
    }

    @PostMapping("/{employeeId}/salary-structures")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign new salary structure", 
               description = "Assign a new salary structure to an employee. Automatically manages timeline by closing previous structures (Admin only)")
    public ResponseEntity<ApiResponse<SalaryStructureResponse>> assignSalaryStructure(
            @Parameter(description = "Employee ID") @PathVariable String employeeId,
            @Valid @RequestBody SalaryStructureRequest request) {
        
        SalaryStructureResponse salaryStructure = salaryStructureService.assignNewStructure(employeeId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Salary structure assigned successfully", salaryStructure));
    }

    @GetMapping("/{employeeId}/salary-structures/current")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get current salary structure", 
               description = "Retrieve the currently active salary structure for an employee (Admin only)")
    public ResponseEntity<ApiResponse<SalaryStructureResponse>> getCurrentSalaryStructure(
            @Parameter(description = "Employee ID") @PathVariable String employeeId) {
        
        SalaryStructureResponse currentStructure = salaryStructureService.getCurrentStructureForEmployee(employeeId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Current salary structure retrieved successfully", currentStructure));
    }
}