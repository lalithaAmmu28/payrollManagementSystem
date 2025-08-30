package com.pms.backend.service;

import com.pms.backend.dto.employee.EmployeeCreateRequest;
import com.pms.backend.dto.employee.EmployeeResponse;
import com.pms.backend.dto.employee.EmployeeUpdateRequest;
import com.pms.backend.dto.user.PasswordChangeRequest;
import com.pms.backend.dto.user.UserProfileUpdateRequest;

import java.util.List;

public interface EmployeeService {
    
    /**
     * Create a new employee (Admin only)
     * This creates both User and Employee entities
     */
    EmployeeResponse createEmployee(EmployeeCreateRequest request);
    
    /**
     * Get employee by ID (Admin only)
     */
    EmployeeResponse getEmployeeById(String employeeId);
    
    /**
     * Get all employees (Admin only)
     */
    List<EmployeeResponse> getAllEmployees();
    
    /**
     * Update employee (Admin only)
     */
    EmployeeResponse updateEmployee(String employeeId, EmployeeUpdateRequest request);
    
    /**
     * Delete employee (Admin only)
     */
    void deleteEmployee(String employeeId);
    
    /**
     * Get employee by user ID
     */
    EmployeeResponse getEmployeeByUserId(String userId);
    
    /**
     * Update employee profile (Self-service for employees)
     */
    EmployeeResponse updateEmployeeProfile(String userId, UserProfileUpdateRequest request);
    
    /**
     * Change user password (Self-service)
     */
    void changePassword(String userId, PasswordChangeRequest request);
    
    /**
     * Check if employee exists by ID
     */
    boolean existsById(String employeeId);
}