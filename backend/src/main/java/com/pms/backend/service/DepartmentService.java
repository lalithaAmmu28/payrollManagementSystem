package com.pms.backend.service;

import com.pms.backend.dto.department.DepartmentCreateRequest;
import com.pms.backend.dto.department.DepartmentResponse;
import com.pms.backend.dto.department.DepartmentUpdateRequest;

import java.util.List;

public interface DepartmentService {
    
    /**
     * Create a new department
     */
    DepartmentResponse createDepartment(DepartmentCreateRequest request);
    
    /**
     * Get department by ID
     */
    DepartmentResponse getDepartmentById(String departmentId);
    
    /**
     * Get all departments
     */
    List<DepartmentResponse> getAllDepartments();
    
    /**
     * Update department
     */
    DepartmentResponse updateDepartment(String departmentId, DepartmentUpdateRequest request);
    
    /**
     * Delete department
     */
    void deleteDepartment(String departmentId);
    
    /**
     * Check if department exists
     */
    boolean existsById(String departmentId);
}