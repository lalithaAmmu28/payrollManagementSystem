package com.pms.backend.service;

import com.pms.backend.dto.report.DepartmentCostDto;
import com.pms.backend.dto.report.LeaveTrendDto;
import com.pms.backend.dto.report.PayrollSummaryDto;
import com.pms.backend.entity.enums.LeaveType;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for generating various reports
 * Provides analytics and insights for Admin users
 */
public interface ReportService {
    
    // ====== PAYROLL REPORTS ======
    
    /**
     * Get department-wise payroll cost analysis
     * @param year Specific year (optional)
     * @param month Specific month (optional)
     * @param startYear Start year for range (optional)
     * @param endYear End year for range (optional)
     * @return List of department cost summaries
     */
    List<DepartmentCostDto> getDepartmentCostReport(Integer year, Integer month, 
                                                   Integer startYear, Integer endYear);
    
    /**
     * Get monthly payroll summary for specified period
     * @param startYear Start year (optional)
     * @param endYear End year (optional)
     * @return List of monthly payroll summaries
     */
    List<PayrollSummaryDto> getMonthlyPayrollSummary(Integer startYear, Integer endYear);
    
    /**
     * Get overall payroll summary (aggregated across all periods)
     * @param startYear Start year (optional)
     * @param endYear End year (optional)
     * @return Overall payroll summary
     */
    PayrollSummaryDto getOverallPayrollSummary(Integer startYear, Integer endYear);
    
    /**
     * Get top spending departments by total payroll cost
     * @param year Specific year (optional, defaults to current year)
     * @param limit Maximum number of departments to return
     * @return List of top spending departments
     */
    List<DepartmentCostDto> getTopSpendingDepartments(Integer year, Integer limit);
    
    // ====== LEAVE REPORTS ======
    
    /**
     * Get leave usage trends by leave type
     * @param startDate Start date for filtering (optional)
     * @param endDate End date for filtering (optional)
     * @param year Specific year (optional)
     * @return List of leave trends by type
     */
    List<LeaveTrendDto> getLeaveTrendsReport(LocalDate startDate, LocalDate endDate, Integer year);
    
    /**
     * Get leave trends grouped by department and leave type
     * @param startDate Start date for filtering (optional)
     * @param endDate End date for filtering (optional)
     * @param year Specific year (optional)
     * @return List of leave trends by department
     */
    List<Object[]> getLeaveTrendsByDepartment(LocalDate startDate, LocalDate endDate, Integer year);
    
    /**
     * Get monthly leave statistics for a specific year
     * @param year Year to analyze
     * @return List of monthly leave statistics
     */
    List<Object[]> getMonthlyLeaveStatistics(Integer year);
    
    /**
     * Get top leave-taking employees
     * @param year Specific year (optional)
     * @param leaveType Specific leave type (optional)
     * @param limit Maximum number of employees to return
     * @return List of top leave-taking employees
     */
    List<Object[]> getTopLeaveTakingEmployees(Integer year, LeaveType leaveType, Integer limit);
    
    /**
     * Get overall leave statistics
     * @param startDate Start date for filtering (optional)
     * @param endDate End date for filtering (optional)
     * @param year Specific year (optional)
     * @return Overall leave statistics as array
     */
    Object[] getOverallLeaveStatistics(LocalDate startDate, LocalDate endDate, Integer year);
    
    // ====== COMBINED REPORTS ======
    
    /**
     * Get comprehensive analytics dashboard data
     * @param year Year for analysis (defaults to current year)
     * @return Map containing various analytics data
     */
    java.util.Map<String, Object> getAnalyticsDashboard(Integer year);
}