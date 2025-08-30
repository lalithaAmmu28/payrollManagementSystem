package com.pms.backend.service.impl;

import com.pms.backend.dto.report.DepartmentCostDto;
import com.pms.backend.dto.report.LeaveTrendDto;
import com.pms.backend.dto.report.PayrollSummaryDto;
import com.pms.backend.entity.enums.LeaveType;
import com.pms.backend.repository.LeaveRequestRepository;
import com.pms.backend.repository.PayrollItemRepository;
import com.pms.backend.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of ReportService
 * Provides analytics and reporting functionality for Admin users
 */
@Service
public class ReportServiceImpl implements ReportService {
    
    private final PayrollItemRepository payrollItemRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    
    @Autowired
    public ReportServiceImpl(PayrollItemRepository payrollItemRepository,
                           LeaveRequestRepository leaveRequestRepository) {
        this.payrollItemRepository = payrollItemRepository;
        this.leaveRequestRepository = leaveRequestRepository;
    }
    
    // ====== PAYROLL REPORTS ======
    
    @Override
    public List<DepartmentCostDto> getDepartmentCostReport(Integer year, Integer month, 
                                                          Integer startYear, Integer endYear) {
        List<Object[]> results = payrollItemRepository.getDepartmentCostReport(year, month, startYear, endYear);
        
        return results.stream()
                .map(result -> new DepartmentCostDto(
                    (String) result[0],  // departmentId
                    (String) result[1],  // departmentName
                    ((Number) result[2]).longValue(),  // employeeCount
                    (BigDecimal) result[3],  // totalBaseSalary
                    (BigDecimal) result[4],  // totalBonus
                    (BigDecimal) result[5],  // totalDeductions
                    (BigDecimal) result[6]   // totalNetSalary
                ))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PayrollSummaryDto> getMonthlyPayrollSummary(Integer startYear, Integer endYear) {
        List<Object[]> results = payrollItemRepository.getMonthlyPayrollSummary(startYear, endYear);
        
        return results.stream()
                .map(result -> new PayrollSummaryDto(
                    ((Number) result[0]).intValue(),  // year
                    ((Number) result[1]).intValue(),  // month
                    ((Number) result[2]).longValue(), // totalEmployees
                    (BigDecimal) result[3],  // totalBaseSalary
                    (BigDecimal) result[4],  // totalBonus
                    (BigDecimal) result[5],  // totalDeductions
                    (BigDecimal) result[6],  // totalNetSalary
                    ((Number) result[7]).longValue(), // totalPayrollRuns
                    ((Number) result[8]).longValue()  // lockedPayrollRuns
                ))
                .collect(Collectors.toList());
    }
    
    @Override
    public PayrollSummaryDto getOverallPayrollSummary(Integer startYear, Integer endYear) {
        Object[] result = payrollItemRepository.getOverallPayrollSummary(startYear, endYear);
        
        // Handle nested array issue similar to the payroll statistics fix
        if (result != null && result.length > 0 && result[0] instanceof Object[]) {
            result = (Object[]) result[0];
        }
        
        if (result != null && result.length >= 7) {
            return new PayrollSummaryDto(
                ((Number) result[0]).longValue(), // totalEmployees
                (BigDecimal) result[1],  // totalBaseSalary
                (BigDecimal) result[2],  // totalBonus
                (BigDecimal) result[3],  // totalDeductions
                (BigDecimal) result[4],  // totalNetSalary
                ((Number) result[5]).longValue(), // totalPayrollRuns
                ((Number) result[6]).longValue()  // lockedPayrollRuns
            );
        }
        
        // Return empty summary if no data
        return new PayrollSummaryDto(0L, BigDecimal.ZERO, BigDecimal.ZERO, 
                                   BigDecimal.ZERO, BigDecimal.ZERO, 0L, 0L);
    }
    
    @Override
    public List<DepartmentCostDto> getTopSpendingDepartments(Integer year, Integer limit) {
        if (year == null) {
            year = Year.now().getValue();
        }
        
        List<Object[]> results = payrollItemRepository.getTopSpendingDepartments(year);
        
        return results.stream()
                .limit(limit != null ? limit : 10)  // Default to top 10
                .map(result -> {
                    // Create simplified DTO for top spending departments
                    DepartmentCostDto dto = new DepartmentCostDto();
                    dto.setDepartmentId((String) result[0]);
                    dto.setDepartmentName((String) result[1]);
                    dto.setTotalNetSalary((BigDecimal) result[2]);
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    // ====== LEAVE REPORTS ======
    
    @Override
    public List<LeaveTrendDto> getLeaveTrendsReport(LocalDate startDate, LocalDate endDate, Integer year) {
        List<Object[]> results = leaveRequestRepository.getLeaveTrendsReport(startDate, endDate, year);
        
        return results.stream()
                .map(result -> new LeaveTrendDto(
                    (LeaveType) result[0],  // leaveType
                    ((Number) result[1]).longValue(),  // totalRequests
                    ((Number) result[2]).longValue(),  // approvedRequests
                    ((Number) result[3]).longValue(),  // rejectedRequests
                    ((Number) result[4]).longValue(),  // pendingRequests
                    ((Number) result[5]).longValue()   // totalApprovedDays
                ))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Object[]> getLeaveTrendsByDepartment(LocalDate startDate, LocalDate endDate, Integer year) {
        return leaveRequestRepository.getLeaveTrendsByDepartment(startDate, endDate, year);
    }
    
    @Override
    public List<Object[]> getMonthlyLeaveStatistics(Integer year) {
        return leaveRequestRepository.getMonthlyLeaveStatistics(year);
    }
    
    @Override
    public List<Object[]> getTopLeaveTakingEmployees(Integer year, LeaveType leaveType, Integer limit) {
        List<Object[]> results = leaveRequestRepository.getTopLeaveTakingEmployees(year, leaveType);
        
        if (limit != null && limit > 0) {
            return results.stream().limit(limit).collect(Collectors.toList());
        }
        
        return results;
    }
    
    @Override
    public Object[] getOverallLeaveStatistics(LocalDate startDate, LocalDate endDate, Integer year) {
        Object[] result = leaveRequestRepository.getOverallLeaveStatistics(startDate, endDate, year);
        
        // Handle nested array issue
        if (result != null && result.length > 0 && result[0] instanceof Object[]) {
            result = (Object[]) result[0];
        }
        
        return result;
    }
    
    // ====== COMBINED REPORTS ======
    
    @Override
    public Map<String, Object> getAnalyticsDashboard(Integer year) {
        if (year == null) {
            year = Year.now().getValue();
        }
        
        Map<String, Object> dashboard = new HashMap<>();
        
        try {
            // Payroll Analytics
            PayrollSummaryDto payrollSummary = getOverallPayrollSummary(year, year);
            dashboard.put("payrollSummary", payrollSummary);
            
            // Top Departments by Cost
            List<DepartmentCostDto> topDepartments = getTopSpendingDepartments(year, 5);
            dashboard.put("topSpendingDepartments", topDepartments);
            
            // Department Cost Breakdown
            List<DepartmentCostDto> departmentCosts = getDepartmentCostReport(year, null, null, null);
            dashboard.put("departmentCostBreakdown", departmentCosts);
            
            // Leave Analytics
            List<LeaveTrendDto> leaveByType = getLeaveTrendsReport(null, null, year);
            dashboard.put("leaveTrendsByType", leaveByType);
            
            // Monthly Leave Stats
            List<Object[]> monthlyLeave = getMonthlyLeaveStatistics(year);
            dashboard.put("monthlyLeaveStatistics", monthlyLeave);
            
            // Overall Leave Stats
            Object[] overallLeave = getOverallLeaveStatistics(null, null, year);
            dashboard.put("overallLeaveStatistics", overallLeave);
            
            // Top Leave Taking Employees
            List<Object[]> topLeaveEmployees = getTopLeaveTakingEmployees(year, null, 10);
            dashboard.put("topLeaveTakingEmployees", topLeaveEmployees);
            
            // Summary Metrics
            Map<String, Object> summaryMetrics = new HashMap<>();
            summaryMetrics.put("totalDepartments", departmentCosts.size());
            summaryMetrics.put("totalEmployeesInPayroll", payrollSummary.getTotalEmployees());
            summaryMetrics.put("averageSalaryPerEmployee", payrollSummary.getAverageSalaryPerEmployee());
            summaryMetrics.put("payrollCompletionRate", payrollSummary.getCompletionRate());
            
            if (overallLeave != null && overallLeave.length >= 5) {
                summaryMetrics.put("totalLeaveRequests", overallLeave[0]);
                summaryMetrics.put("leaveApprovalRate", 
                    ((Number) overallLeave[0]).longValue() > 0 ? 
                    (((Number) overallLeave[1]).doubleValue() / ((Number) overallLeave[0]).doubleValue()) * 100.0 : 0.0);
            }
            
            dashboard.put("summaryMetrics", summaryMetrics);
            dashboard.put("analysisYear", year);
            dashboard.put("generatedAt", LocalDate.now());
            
        } catch (Exception e) {
            dashboard.put("error", "Failed to generate dashboard: " + e.getMessage());
        }
        
        return dashboard;
    }
}