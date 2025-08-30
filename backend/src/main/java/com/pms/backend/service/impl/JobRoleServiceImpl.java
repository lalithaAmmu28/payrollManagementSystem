package com.pms.backend.service.impl;

import com.pms.backend.dto.jobrole.JobRoleCreateRequest;
import com.pms.backend.dto.jobrole.JobRoleResponse;
import com.pms.backend.dto.jobrole.JobRoleUpdateRequest;
import com.pms.backend.entity.JobRole;
import com.pms.backend.exception.BadRequestException;
import com.pms.backend.exception.ConstraintViolationException;
import com.pms.backend.exception.ResourceNotFoundException;
import com.pms.backend.repository.EmployeeRepository;
import com.pms.backend.repository.JobRoleRepository;
import com.pms.backend.service.JobRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class JobRoleServiceImpl implements JobRoleService {
    
    private final JobRoleRepository jobRoleRepository;
    private final EmployeeRepository employeeRepository;
    
    @Autowired
    public JobRoleServiceImpl(JobRoleRepository jobRoleRepository, EmployeeRepository employeeRepository) {
        this.jobRoleRepository = jobRoleRepository;
        this.employeeRepository = employeeRepository;
    }
    
    @Override
    public JobRoleResponse createJobRole(JobRoleCreateRequest request) {
        // Check if job title already exists
        if (jobRoleRepository.existsByJobTitle(request.getJobTitle())) {
            throw new BadRequestException("Job role with title '" + request.getJobTitle() + "' already exists");
        }
        
        JobRole jobRole = new JobRole();
        jobRole.setJobTitle(request.getJobTitle());
        jobRole.setBaseSalary(request.getBaseSalary());
        
        JobRole savedJobRole = jobRoleRepository.save(jobRole);
        return convertToResponse(savedJobRole);
    }
    
    @Override
    @Transactional(readOnly = true)
    public JobRoleResponse getJobRoleById(String jobId) {
        JobRole jobRole = jobRoleRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job role not found with ID: " + jobId));
        return convertToResponse(jobRole);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<JobRoleResponse> getAllJobRoles() {
        return jobRoleRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public JobRoleResponse updateJobRole(String jobId, JobRoleUpdateRequest request) {
        JobRole jobRole = jobRoleRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job role not found with ID: " + jobId));
        
        // Check if new title conflicts with existing job role (excluding current one)
        if (!jobRole.getJobTitle().equals(request.getJobTitle()) &&
            jobRoleRepository.existsByJobTitle(request.getJobTitle())) {
            throw new BadRequestException("Job role with title '" + request.getJobTitle() + "' already exists");
        }
        
        jobRole.setJobTitle(request.getJobTitle());
        jobRole.setBaseSalary(request.getBaseSalary());
        
        JobRole updatedJobRole = jobRoleRepository.save(jobRole);
        return convertToResponse(updatedJobRole);
    }
    
    @Override
    public void deleteJobRole(String jobId) {
        if (!jobRoleRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Job role not found with ID: " + jobId);
        }
        
        // Check if job role is being used by any employees
        long employeeCount = employeeRepository.countByJobId(jobId);
        if (employeeCount > 0) {
            throw new ConstraintViolationException(
                "Cannot delete job role. It is currently assigned to " + employeeCount + " employee(s)"
            );
        }
        
        jobRoleRepository.deleteById(jobId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(String jobId) {
        return jobRoleRepository.existsById(jobId);
    }
    
    private JobRoleResponse convertToResponse(JobRole jobRole) {
        return new JobRoleResponse(
                jobRole.getJobId(),
                jobRole.getJobTitle(),
                jobRole.getBaseSalary(),
                jobRole.getCreatedAt(),
                jobRole.getUpdatedAt()
        );
    }
}