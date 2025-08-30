package com.pms.backend.controller;

import com.pms.backend.dto.ApiResponse;
import com.pms.backend.dto.jobrole.JobRoleCreateRequest;
import com.pms.backend.dto.jobrole.JobRoleResponse;
import com.pms.backend.dto.jobrole.JobRoleUpdateRequest;
import com.pms.backend.service.JobRoleService;
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
@RequestMapping("/api/v1/jobs")
@Tag(name = "Job Role Management", description = "Admin-only endpoints for managing job roles")
@SecurityRequirement(name = "Bearer Authentication")
public class JobRoleController {

    private final JobRoleService jobRoleService;

    @Autowired
    public JobRoleController(JobRoleService jobRoleService) {
        this.jobRoleService = jobRoleService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new job role", description = "Create a new job role (Admin only)")
    public ResponseEntity<ApiResponse<JobRoleResponse>> createJobRole(
            @Valid @RequestBody JobRoleCreateRequest request) {
        
        JobRoleResponse jobRole = jobRoleService.createJobRole(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Job role created successfully", jobRole));
    }

    @GetMapping("/{jobId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get job role by ID", description = "Retrieve a specific job role by ID (Admin only)")
    public ResponseEntity<ApiResponse<JobRoleResponse>> getJobRoleById(
            @Parameter(description = "Job Role ID") @PathVariable String jobId) {
        
        JobRoleResponse jobRole = jobRoleService.getJobRoleById(jobId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Job role retrieved successfully", jobRole));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all job roles", description = "Retrieve all job roles (Admin only)")
    public ResponseEntity<ApiResponse<List<JobRoleResponse>>> getAllJobRoles() {
        
        List<JobRoleResponse> jobRoles = jobRoleService.getAllJobRoles();
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Job roles retrieved successfully", jobRoles));
    }

    @PutMapping("/{jobId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update job role", description = "Update an existing job role (Admin only)")
    public ResponseEntity<ApiResponse<JobRoleResponse>> updateJobRole(
            @Parameter(description = "Job Role ID") @PathVariable String jobId,
            @Valid @RequestBody JobRoleUpdateRequest request) {
        
        JobRoleResponse jobRole = jobRoleService.updateJobRole(jobId, request);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Job role updated successfully", jobRole));
    }

    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete job role", description = "Delete a job role (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteJobRole(
            @Parameter(description = "Job Role ID") @PathVariable String jobId) {
        
        jobRoleService.deleteJobRole(jobId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Job role deleted successfully", null));
    }
}