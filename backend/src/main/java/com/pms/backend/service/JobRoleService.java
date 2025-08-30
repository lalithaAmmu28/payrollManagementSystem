package com.pms.backend.service;

import com.pms.backend.dto.jobrole.JobRoleCreateRequest;
import com.pms.backend.dto.jobrole.JobRoleResponse;
import com.pms.backend.dto.jobrole.JobRoleUpdateRequest;

import java.util.List;

public interface JobRoleService {
    
    /**
     * Create a new job role
     */
    JobRoleResponse createJobRole(JobRoleCreateRequest request);
    
    /**
     * Get job role by ID
     */
    JobRoleResponse getJobRoleById(String jobId);
    
    /**
     * Get all job roles
     */
    List<JobRoleResponse> getAllJobRoles();
    
    /**
     * Update job role
     */
    JobRoleResponse updateJobRole(String jobId, JobRoleUpdateRequest request);
    
    /**
     * Delete job role
     */
    void deleteJobRole(String jobId);
    
    /**
     * Check if job role exists
     */
    boolean existsById(String jobId);
}