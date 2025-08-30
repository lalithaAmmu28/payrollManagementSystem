package com.pms.backend.controller;

import com.pms.backend.dto.ApiResponse;
import com.pms.backend.dto.department.DepartmentCreateRequest;
import com.pms.backend.dto.department.DepartmentResponse;
import com.pms.backend.dto.department.DepartmentUpdateRequest;
import com.pms.backend.service.DepartmentService;
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
@RequestMapping("/api/v1/departments")
@Tag(name = "Department Management", description = "Admin-only endpoints for managing departments")
@SecurityRequirement(name = "Bearer Authentication")
public class DepartmentController {

    private final DepartmentService departmentService;

    @Autowired
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new department", description = "Create a new department (Admin only)")
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
            @Valid @RequestBody DepartmentCreateRequest request) {
        
        DepartmentResponse department = departmentService.createDepartment(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Department created successfully", department));
    }

    @GetMapping("/{departmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get department by ID", description = "Retrieve a specific department by ID (Admin only)")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(
            @Parameter(description = "Department ID") @PathVariable String departmentId) {
        
        DepartmentResponse department = departmentService.getDepartmentById(departmentId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Department retrieved successfully", department));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all departments", description = "Retrieve all departments (Admin only)")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartments() {
        
        List<DepartmentResponse> departments = departmentService.getAllDepartments();
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Departments retrieved successfully", departments));
    }

    @PutMapping("/{departmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update department", description = "Update an existing department (Admin only)")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @Parameter(description = "Department ID") @PathVariable String departmentId,
            @Valid @RequestBody DepartmentUpdateRequest request) {
        
        DepartmentResponse department = departmentService.updateDepartment(departmentId, request);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Department updated successfully", department));
    }

    @DeleteMapping("/{departmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete department", description = "Delete a department (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(
            @Parameter(description = "Department ID") @PathVariable String departmentId) {
        
        departmentService.deleteDepartment(departmentId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Department deleted successfully", null));
    }
}