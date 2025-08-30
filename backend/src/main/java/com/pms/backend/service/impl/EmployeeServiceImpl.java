package com.pms.backend.service.impl;

import com.pms.backend.dto.employee.EmployeeCreateRequest;
import com.pms.backend.dto.employee.EmployeeResponse;
import com.pms.backend.dto.employee.EmployeeUpdateRequest;
import com.pms.backend.dto.user.PasswordChangeRequest;
import com.pms.backend.dto.user.UserProfileUpdateRequest;
import com.pms.backend.entity.Employee;
import com.pms.backend.entity.User;
import com.pms.backend.entity.enums.Role;
import com.pms.backend.exception.BadRequestException;
import com.pms.backend.exception.ResourceNotFoundException;
import com.pms.backend.repository.*;
import com.pms.backend.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final JobRoleRepository jobRoleRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public EmployeeServiceImpl(
            EmployeeRepository employeeRepository,
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            JobRoleRepository jobRoleRepository,
            PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.jobRoleRepository = jobRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        // Validate username and email uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username '" + request.getUsername() + "' already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email '" + request.getEmail() + "' already exists");
        }
        
        // Validate department and job role exist
        if (!departmentRepository.existsById(request.getDepartmentId())) {
            throw new BadRequestException("Department not found with ID: " + request.getDepartmentId());
        }
        
        if (!jobRoleRepository.existsById(request.getJobId())) {
            throw new BadRequestException("Job role not found with ID: " + request.getJobId());
        }
        
        // Create User entity first
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.Employee);
        user.setIsActive(true);
        
        User savedUser = userRepository.save(user);
        
        // Create Employee entity
        Employee employee = new Employee();
        employee.setUserId(savedUser.getUserId());
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setPhone(request.getPhone());
        employee.setAddress(request.getAddress());
        employee.setJobId(request.getJobId());
        employee.setDepartmentId(request.getDepartmentId());
        employee.setLeaveBalance(request.getLeaveBalance());
        
        Employee savedEmployee = employeeRepository.save(employee);
        
        return convertToResponse(savedEmployee);
    }
    
    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(String employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));
        return convertToResponse(employee);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public EmployeeResponse updateEmployee(String employeeId, EmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));
        
        // Validate department and job role exist
        if (!departmentRepository.existsById(request.getDepartmentId())) {
            throw new BadRequestException("Department not found with ID: " + request.getDepartmentId());
        }
        
        if (!jobRoleRepository.existsById(request.getJobId())) {
            throw new BadRequestException("Job role not found with ID: " + request.getJobId());
        }
        
        // Update employee fields
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setPhone(request.getPhone());
        employee.setAddress(request.getAddress());
        employee.setJobId(request.getJobId());
        employee.setDepartmentId(request.getDepartmentId());
        employee.setLeaveBalance(request.getLeaveBalance());
        
        Employee updatedEmployee = employeeRepository.save(employee);
        return convertToResponse(updatedEmployee);
    }
    
    @Override
    public void deleteEmployee(String employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));
        
        // Delete employee first (this will cascade to user due to FK constraint)
        employeeRepository.delete(employee);
        
        // Delete associated user
        userRepository.deleteById(employee.getUserId());
    }
    
    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByUserId(String userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for user ID: " + userId));
        return convertToResponse(employee);
    }
    
    @Override
    public EmployeeResponse updateEmployeeProfile(String userId, UserProfileUpdateRequest request) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for user ID: " + userId));
        
        // Update only profile fields (employees can't change job/department/leave balance)
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setPhone(request.getPhone());
        employee.setAddress(request.getAddress());
        
        Employee updatedEmployee = employeeRepository.save(employee);
        return convertToResponse(updatedEmployee);
    }
    
    @Override
    public void changePassword(String userId, PasswordChangeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(String employeeId) {
        return employeeRepository.existsById(employeeId);
    }
    
    private EmployeeResponse convertToResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        
        // Employee data
        response.setEmployeeId(employee.getEmployeeId());
        response.setUserId(employee.getUserId());
        response.setFirstName(employee.getFirstName());
        response.setLastName(employee.getLastName());
        response.setDateOfBirth(employee.getDateOfBirth());
        response.setPhone(employee.getPhone());
        response.setAddress(employee.getAddress());
        response.setLeaveBalance(employee.getLeaveBalance());
        response.setCreatedAt(employee.getCreatedAt());
        response.setUpdatedAt(employee.getUpdatedAt());
        
        // User data
        if (employee.getUser() != null) {
            response.setUsername(employee.getUser().getUsername());
            response.setEmail(employee.getUser().getEmail());
            response.setRole(employee.getUser().getRole().name());
            response.setIsActive(employee.getUser().getIsActive());
        } else {
            // Fallback: fetch user separately if lazy loading fails
            userRepository.findById(employee.getUserId()).ifPresent(user -> {
                response.setUsername(user.getUsername());
                response.setEmail(user.getEmail());
                response.setRole(user.getRole().name());
                response.setIsActive(user.getIsActive());
            });
        }
        
        // Job role data
        response.setJobId(employee.getJobId());
        if (employee.getJobRole() != null) {
            response.setJobTitle(employee.getJobRole().getJobTitle());
            response.setBaseSalary(employee.getJobRole().getBaseSalary());
        } else {
            // Fallback: fetch job role separately if lazy loading fails
            jobRoleRepository.findById(employee.getJobId()).ifPresent(jobRole -> {
                response.setJobTitle(jobRole.getJobTitle());
                response.setBaseSalary(jobRole.getBaseSalary());
            });
        }
        
        // Department data
        response.setDepartmentId(employee.getDepartmentId());
        if (employee.getDepartment() != null) {
            response.setDepartmentName(employee.getDepartment().getDepartmentName());
        } else {
            // Fallback: fetch department separately if lazy loading fails
            departmentRepository.findById(employee.getDepartmentId()).ifPresent(department -> {
                response.setDepartmentName(department.getDepartmentName());
            });
        }
        
        return response;
    }
}