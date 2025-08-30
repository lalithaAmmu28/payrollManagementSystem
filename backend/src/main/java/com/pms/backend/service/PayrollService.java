package com.pms.backend.service;

import com.pms.backend.dto.payroll.PayrollItemResponse;
import com.pms.backend.dto.payroll.PayrollRunCreateRequest;
import com.pms.backend.dto.payroll.PayrollRunResponse;

import java.util.List;

public interface PayrollService {
    
    /**
     * Create a new payroll run for a specific year and month
     * Validates that no duplicate run exists for the same period
     */
    PayrollRunResponse createPayrollRun(PayrollRunCreateRequest request);
    
    /**
     * Get all payroll runs (Admin only)
     */
    List<PayrollRunResponse> getAllPayrollRuns();
    
    /**
     * Get payroll run by ID with summary statistics
     */
    PayrollRunResponse getPayrollRunById(String runId);
    
    /**
     * CORE PAYROLL ENGINE: Process a payroll run
     * Implements the complete calculation logic with salary structures,
     * bonus calculations, and loss of pay deductions
     */
    PayrollRunResponse processPayrollRun(String runId);
    
    /**
     * Lock a payroll run and set pay dates
     * Only processed runs can be locked
     */
    PayrollRunResponse lockPayrollRun(String runId);
    
    /**
     * Get all payroll items for a specific run (Admin only)
     */
    List<PayrollItemResponse> getPayrollItemsForRun(String runId);
    
    /**
     * Get employee's payslip for a specific run
     * Only available for locked runs
     */
    PayrollItemResponse getEmployeePayslip(String runId, String employeeId);
    
    /**
     * Get all locked payslips for an employee
     */
    List<PayrollItemResponse> getEmployeePayslips(String employeeId);
    
    /**
     * Check if a payroll run exists for a specific year and month
     */
    boolean payrollRunExists(Integer year, Integer month);
    
    /**
     * Get payroll statistics for reporting
     */
    PayrollRunResponse getPayrollStatistics(String runId);
}