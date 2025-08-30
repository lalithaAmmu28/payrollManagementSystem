package com.pms.backend.service.impl;

import com.pms.backend.dto.salary.SalaryStructureRequest;
import com.pms.backend.dto.salary.SalaryStructureResponse;
import com.pms.backend.entity.Employee;
import com.pms.backend.entity.SalaryStructure;
import com.pms.backend.exception.BadRequestException;
import com.pms.backend.exception.ResourceNotFoundException;
import com.pms.backend.repository.EmployeeRepository;
import com.pms.backend.repository.SalaryStructureRepository;
import com.pms.backend.service.SalaryStructureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SalaryStructureServiceImpl implements SalaryStructureService {
    
    private final SalaryStructureRepository salaryStructureRepository;
    private final EmployeeRepository employeeRepository;
    
    @Autowired
    public SalaryStructureServiceImpl(SalaryStructureRepository salaryStructureRepository, 
                                    EmployeeRepository employeeRepository) {
        this.salaryStructureRepository = salaryStructureRepository;
        this.employeeRepository = employeeRepository;
    }
    
    @Override
    public SalaryStructureResponse assignNewStructure(String employeeId, SalaryStructureRequest request) {
        // Validate employee exists
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }
        
        // Validate effective dates
        validateEffectiveDates(request);
        
        // Handle timeline management: close previous open structures
        closePreviousStructures(employeeId, request.getEffectiveFrom());
        
        // Create new salary structure
        SalaryStructure salaryStructure = new SalaryStructure();
        salaryStructure.setEmployeeId(employeeId);
        salaryStructure.setBaseSalary(request.getBaseSalary());
        salaryStructure.setBonusDetails(request.getBonusDetails());
        salaryStructure.setEffectiveFrom(request.getEffectiveFrom());
        salaryStructure.setEffectiveTo(request.getEffectiveTo());
        
        SalaryStructure savedStructure = salaryStructureRepository.save(salaryStructure);
        
        return convertToResponse(savedStructure);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<SalaryStructureResponse> getStructureHistoryForEmployee(String employeeId) {
        // Validate employee exists
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }
        
        List<SalaryStructure> structures = salaryStructureRepository.findByEmployeeIdOrderByEffectiveFromDesc(employeeId);
        
        return structures.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public SalaryStructureResponse getCurrentStructureForEmployee(String employeeId) {
        // Validate employee exists
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }
        
        SalaryStructure currentStructure = salaryStructureRepository
                .findActiveStructureForEmployee(employeeId, LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException("No active salary structure found for employee: " + employeeId));
        
        return convertToResponse(currentStructure);
    }
    
    @Override
    @Transactional(readOnly = true)
    public SalaryStructureResponse getStructureById(String structureId) {
        SalaryStructure structure = salaryStructureRepository.findById(structureId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found with ID: " + structureId));
        
        return convertToResponse(structure);
    }
    
    @Override
    public SalaryStructureResponse updateStructure(String structureId, SalaryStructureRequest request) {
        SalaryStructure structure = salaryStructureRepository.findById(structureId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found with ID: " + structureId));
        
        // Validate effective dates
        validateEffectiveDates(request);
        
        // Check for overlaps with other structures (excluding current one)
        if (hasOverlapWithOtherStructures(structure.getEmployeeId(), structureId, request)) {
            throw new BadRequestException("The updated dates would overlap with existing salary structures");
        }
        
        // Update structure
        structure.setBaseSalary(request.getBaseSalary());
        structure.setBonusDetails(request.getBonusDetails());
        structure.setEffectiveFrom(request.getEffectiveFrom());
        structure.setEffectiveTo(request.getEffectiveTo());
        
        SalaryStructure updatedStructure = salaryStructureRepository.save(structure);
        
        return convertToResponse(updatedStructure);
    }
    
    @Override
    public void deleteStructure(String structureId) {
        if (!salaryStructureRepository.existsById(structureId)) {
            throw new ResourceNotFoundException("Salary structure not found with ID: " + structureId);
        }
        
        salaryStructureRepository.deleteById(structureId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean employeeExists(String employeeId) {
        return employeeRepository.existsById(employeeId);
    }
    
    /**
     * Close previous open structures when assigning a new one
     */
    private void closePreviousStructures(String employeeId, LocalDate newEffectiveFrom) {
        List<SalaryStructure> structuresToClose = salaryStructureRepository
                .findStructuresToCloseForEmployee(employeeId, newEffectiveFrom);
        
        for (SalaryStructure structure : structuresToClose) {
            // Set effective_to to one day before the new structure's effective_from
            structure.setEffectiveTo(newEffectiveFrom.minusDays(1));
            salaryStructureRepository.save(structure);
        }
    }
    
    /**
     * Validate effective dates in the request
     */
    private void validateEffectiveDates(SalaryStructureRequest request) {
        if (request.getEffectiveFrom() == null) {
            throw new BadRequestException("Effective from date is required");
        }
        
        if (request.getEffectiveTo() != null && 
            !request.getEffectiveTo().isAfter(request.getEffectiveFrom())) {
            throw new BadRequestException("Effective to date must be after effective from date");
        }
        
        // Allow past dates for correcting historical data, but warn if too far in the past
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        if (request.getEffectiveFrom().isBefore(oneYearAgo)) {
            // Just log a warning, don't throw an exception
            System.out.println("Warning: Effective from date is more than a year in the past: " + request.getEffectiveFrom());
        }
    }
    
    /**
     * Check if the request would create overlaps with other structures
     */
    private boolean hasOverlapWithOtherStructures(String employeeId, String excludeStructureId, SalaryStructureRequest request) {
        LocalDate effectiveTo = request.getEffectiveTo();
        if (effectiveTo == null) {
            effectiveTo = LocalDate.of(9999, 12, 31); // Far future date for open-ended structures
        }
        
        return salaryStructureRepository.hasOverlapWithExistingStructures(
                employeeId, excludeStructureId, request.getEffectiveFrom(), effectiveTo);
    }
    
    /**
     * Convert SalaryStructure entity to SalaryStructureResponse DTO
     */
    private SalaryStructureResponse convertToResponse(SalaryStructure structure) {
        SalaryStructureResponse response = new SalaryStructureResponse();
        
        response.setStructureId(structure.getStructureId());
        response.setEmployeeId(structure.getEmployeeId());
        response.setBaseSalary(structure.getBaseSalary());
        response.setBonusDetails(structure.getBonusDetails());
        response.setEffectiveFrom(structure.getEffectiveFrom());
        response.setEffectiveTo(structure.getEffectiveTo());
        response.setCreatedAt(structure.getCreatedAt());
        response.setUpdatedAt(structure.getUpdatedAt());
        response.setActive(structure.isCurrentlyActive());
        
        // Optionally add employee information
        if (structure.getEmployee() != null) {
            response.setEmployeeName(structure.getEmployee().getFirstName() + " " + structure.getEmployee().getLastName());
            if (structure.getEmployee().getUser() != null) {
                response.setEmployeeEmail(structure.getEmployee().getUser().getEmail());
            }
        } else {
            // Fallback: fetch employee separately if lazy loading fails
            employeeRepository.findById(structure.getEmployeeId()).ifPresent(employee -> {
                response.setEmployeeName(employee.getFirstName() + " " + employee.getLastName());
                if (employee.getUser() != null) {
                    response.setEmployeeEmail(employee.getUser().getEmail());
                }
            });
        }
        
        return response;
    }
}
