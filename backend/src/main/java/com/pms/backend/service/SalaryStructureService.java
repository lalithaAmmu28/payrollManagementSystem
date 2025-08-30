package com.pms.backend.service;

import com.pms.backend.dto.salary.SalaryStructureRequest;
import com.pms.backend.dto.salary.SalaryStructureResponse;

import java.util.List;

public interface SalaryStructureService {
    
    /**
     * Assign a new salary structure to an employee
     * This method handles timeline management - automatically closes previous structures
     */
    SalaryStructureResponse assignNewStructure(String employeeId, SalaryStructureRequest request);
    
    /**
     * Get the complete salary structure history for an employee
     * Returns structures ordered by effective date (newest first)
     */
    List<SalaryStructureResponse> getStructureHistoryForEmployee(String employeeId);
    
    /**
     * Get the currently active salary structure for an employee
     */
    SalaryStructureResponse getCurrentStructureForEmployee(String employeeId);
    
    /**
     * Get salary structure by ID
     */
    SalaryStructureResponse getStructureById(String structureId);
    
    /**
     * Update an existing salary structure
     */
    SalaryStructureResponse updateStructure(String structureId, SalaryStructureRequest request);
    
    /**
     * Delete a salary structure
     */
    void deleteStructure(String structureId);
    
    /**
     * Check if an employee exists
     */
    boolean employeeExists(String employeeId);
}
