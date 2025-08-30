package com.pms.backend.dto.report;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * DTO for Payroll Summary Report
 * Represents monthly payroll totals and trends
 */
public class PayrollSummaryDto {
    
    private Integer year;
    private Integer month;
    private String periodDescription;
    private Long totalEmployees;
    private BigDecimal totalBaseSalary;
    private BigDecimal totalBonus;
    private BigDecimal totalDeductions;
    private BigDecimal totalNetSalary;
    private BigDecimal averageSalaryPerEmployee;
    private Long totalPayrollRuns;
    private Long lockedPayrollRuns;
    
    // Default constructor
    public PayrollSummaryDto() {}
    
    // Constructor for specific month
    public PayrollSummaryDto(Integer year, Integer month, Long totalEmployees, 
                            BigDecimal totalBaseSalary, BigDecimal totalBonus, 
                            BigDecimal totalDeductions, BigDecimal totalNetSalary,
                            Long totalPayrollRuns, Long lockedPayrollRuns) {
        this.year = year;
        this.month = month;
        this.totalEmployees = totalEmployees;
        this.totalBaseSalary = totalBaseSalary;
        this.totalBonus = totalBonus;
        this.totalDeductions = totalDeductions;
        this.totalNetSalary = totalNetSalary;
        this.totalPayrollRuns = totalPayrollRuns;
        this.lockedPayrollRuns = lockedPayrollRuns;
        
        // Generate period description
        if (year != null && month != null) {
            this.periodDescription = String.format("%04d-%02d", year, month);
        }
        
        // Calculate average salary per employee
        if (totalEmployees != null && totalEmployees > 0 && totalNetSalary != null) {
            this.averageSalaryPerEmployee = totalNetSalary.divide(BigDecimal.valueOf(totalEmployees), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            this.averageSalaryPerEmployee = BigDecimal.ZERO;
        }
    }
    
    // Constructor for aggregated summary (all periods)
    public PayrollSummaryDto(Long totalEmployees, BigDecimal totalBaseSalary, BigDecimal totalBonus, 
                            BigDecimal totalDeductions, BigDecimal totalNetSalary,
                            Long totalPayrollRuns, Long lockedPayrollRuns) {
        this(null, null, totalEmployees, totalBaseSalary, totalBonus, totalDeductions, 
             totalNetSalary, totalPayrollRuns, lockedPayrollRuns);
        this.periodDescription = "All Periods";
    }
    
    // Getters and Setters
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public Integer getMonth() {
        return month;
    }
    
    public void setMonth(Integer month) {
        this.month = month;
    }
    
    public String getPeriodDescription() {
        return periodDescription;
    }
    
    public void setPeriodDescription(String periodDescription) {
        this.periodDescription = periodDescription;
    }
    
    public Long getTotalEmployees() {
        return totalEmployees;
    }
    
    public void setTotalEmployees(Long totalEmployees) {
        this.totalEmployees = totalEmployees;
    }
    
    public BigDecimal getTotalBaseSalary() {
        return totalBaseSalary;
    }
    
    public void setTotalBaseSalary(BigDecimal totalBaseSalary) {
        this.totalBaseSalary = totalBaseSalary;
    }
    
    public BigDecimal getTotalBonus() {
        return totalBonus;
    }
    
    public void setTotalBonus(BigDecimal totalBonus) {
        this.totalBonus = totalBonus;
    }
    
    public BigDecimal getTotalDeductions() {
        return totalDeductions;
    }
    
    public void setTotalDeductions(BigDecimal totalDeductions) {
        this.totalDeductions = totalDeductions;
    }
    
    public BigDecimal getTotalNetSalary() {
        return totalNetSalary;
    }
    
    public void setTotalNetSalary(BigDecimal totalNetSalary) {
        this.totalNetSalary = totalNetSalary;
    }
    
    public BigDecimal getAverageSalaryPerEmployee() {
        return averageSalaryPerEmployee;
    }
    
    public void setAverageSalaryPerEmployee(BigDecimal averageSalaryPerEmployee) {
        this.averageSalaryPerEmployee = averageSalaryPerEmployee;
    }
    
    public Long getTotalPayrollRuns() {
        return totalPayrollRuns;
    }
    
    public void setTotalPayrollRuns(Long totalPayrollRuns) {
        this.totalPayrollRuns = totalPayrollRuns;
    }
    
    public Long getLockedPayrollRuns() {
        return lockedPayrollRuns;
    }
    
    public void setLockedPayrollRuns(Long lockedPayrollRuns) {
        this.lockedPayrollRuns = lockedPayrollRuns;
    }
    
    // Helper methods
    public BigDecimal getTotalGrossSalary() {
        BigDecimal base = totalBaseSalary != null ? totalBaseSalary : BigDecimal.ZERO;
        BigDecimal bonus = totalBonus != null ? totalBonus : BigDecimal.ZERO;
        return base.add(bonus);
    }
    
    public Double getCompletionRate() {
        if (totalPayrollRuns != null && totalPayrollRuns > 0 && lockedPayrollRuns != null) {
            return (lockedPayrollRuns.doubleValue() / totalPayrollRuns.doubleValue()) * 100.0;
        }
        return 0.0;
    }
}
