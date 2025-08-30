package com.pms.backend.service.impl;

import com.pms.backend.dto.department.DepartmentCreateRequest;
import com.pms.backend.dto.department.DepartmentResponse;
import com.pms.backend.dto.department.DepartmentUpdateRequest;
import com.pms.backend.entity.Department;
import com.pms.backend.exception.BadRequestException;
import com.pms.backend.exception.ConstraintViolationException;
import com.pms.backend.exception.ResourceNotFoundException;
import com.pms.backend.repository.DepartmentRepository;
import com.pms.backend.repository.EmployeeRepository;
import com.pms.backend.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {
    
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    
    @Autowired
    public DepartmentServiceImpl(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }
    
    @Override
    public DepartmentResponse createDepartment(DepartmentCreateRequest request) {
        // Check if department name already exists
        if (departmentRepository.existsByDepartmentName(request.getDepartmentName())) {
            throw new BadRequestException("Department with name '" + request.getDepartmentName() + "' already exists");
        }
        
        Department department = new Department();
        department.setDepartmentName(request.getDepartmentName());
        
        Department savedDepartment = departmentRepository.save(department);
        return convertToResponse(savedDepartment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(String departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + departmentId));
        return convertToResponse(department);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public DepartmentResponse updateDepartment(String departmentId, DepartmentUpdateRequest request) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + departmentId));
        
        // Check if new name conflicts with existing department (excluding current one)
        if (!department.getDepartmentName().equals(request.getDepartmentName()) &&
            departmentRepository.existsByDepartmentName(request.getDepartmentName())) {
            throw new BadRequestException("Department with name '" + request.getDepartmentName() + "' already exists");
        }
        
        department.setDepartmentName(request.getDepartmentName());
        
        Department updatedDepartment = departmentRepository.save(department);
        return convertToResponse(updatedDepartment);
    }
    
    @Override
    public void deleteDepartment(String departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department not found with ID: " + departmentId);
        }
        
        // Check if department is being used by any employees
        long employeeCount = employeeRepository.countByDepartmentId(departmentId);
        if (employeeCount > 0) {
            throw new ConstraintViolationException(
                "Cannot delete department. It currently has " + employeeCount + " employee(s) assigned"
            );
        }
        
        departmentRepository.deleteById(departmentId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(String departmentId) {
        return departmentRepository.existsById(departmentId);
    }
    
    private DepartmentResponse convertToResponse(Department department) {
        return new DepartmentResponse(
                department.getDepartmentId(),
                department.getDepartmentName(),
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }
}