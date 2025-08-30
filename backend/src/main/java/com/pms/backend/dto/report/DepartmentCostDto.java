package com.pms.backend.dto.report;

import java.math.BigDecimal;

/**
 * DTO for Department Cost Report
 * Represents total salary costs per department
 */
public class DepartmentCostDto {
    
    private String departmentId;
    private String departmentName;
    private Long employeeCount;
    private BigDecimal totalBaseSalary;
    private BigDecimal totalBonus;
    private BigDecimal totalDeductions;
    private BigDecimal totalNetSalary;
    private BigDecimal averageSalaryPerEmployee;
    
    // Default constructor
    public DepartmentCostDto() {}
    
    // Constructor for repository queries
    public DepartmentCostDto(String departmentId, String departmentName, Long employeeCount, 
                            BigDecimal totalBaseSalary, BigDecimal totalBonus, 
                            BigDecimal totalDeductions, BigDecimal totalNetSalary) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.employeeCount = employeeCount;
        this.totalBaseSalary = totalBaseSalary;
        this.totalBonus = totalBonus;
        this.totalDeductions = totalDeductions;
        this.totalNetSalary = totalNetSalary;
        
        // Calculate average salary per employee
        if (employeeCount != null && employeeCount > 0 && totalNetSalary != null) {
            this.averageSalaryPerEmployee = totalNetSalary.divide(BigDecimal.valueOf(employeeCount), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            this.averageSalaryPerEmployee = BigDecimal.ZERO;
        }
    }
    
    // Getters and Setters
    public String getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }
    
    public String getDepartmentName() {
        return departmentName;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
    
    public Long getEmployeeCount() {
        return employeeCount;
    }
    
    public void setEmployeeCount(Long employeeCount) {
        this.employeeCount = employeeCount;
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
    
    // Helper method to calculate total gross salary
    public BigDecimal getTotalGrossSalary() {
        BigDecimal base = totalBaseSalary != null ? totalBaseSalary : BigDecimal.ZERO;
        BigDecimal bonus = totalBonus != null ? totalBonus : BigDecimal.ZERO;
        return base.add(bonus);
    }
}
