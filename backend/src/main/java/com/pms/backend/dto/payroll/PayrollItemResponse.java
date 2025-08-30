package com.pms.backend.dto.payroll;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PayrollItemResponse {
    
    private String itemId;
    private String runId;
    private String employeeId;
    private BigDecimal baseSalary;
    private BigDecimal bonus;
    private BigDecimal deductions;
    private BigDecimal netSalary;
    private LocalDate payDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Employee information
    private String employeeName;
    private String employeeEmail;
    private String departmentName;
    private String jobTitle;
    
    // Payroll run information
    private Integer runYear;
    private Integer runMonth;
    private String runStatus;
    
    // Constructors
    public PayrollItemResponse() {}
    
    // Getters and Setters
    public String getItemId() {
        return itemId;
    }
    
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
    
    public String getRunId() {
        return runId;
    }
    
    public void setRunId(String runId) {
        this.runId = runId;
    }
    
    public String getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
    
    public BigDecimal getBaseSalary() {
        return baseSalary;
    }
    
    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }
    
    public BigDecimal getBonus() {
        return bonus;
    }
    
    public void setBonus(BigDecimal bonus) {
        this.bonus = bonus;
    }
    
    public BigDecimal getDeductions() {
        return deductions;
    }
    
    public void setDeductions(BigDecimal deductions) {
        this.deductions = deductions;
    }
    
    public BigDecimal getNetSalary() {
        return netSalary;
    }
    
    public void setNetSalary(BigDecimal netSalary) {
        this.netSalary = netSalary;
    }
    
    public LocalDate getPayDate() {
        return payDate;
    }
    
    public void setPayDate(LocalDate payDate) {
        this.payDate = payDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getEmployeeName() {
        return employeeName;
    }
    
    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }
    
    public String getEmployeeEmail() {
        return employeeEmail;
    }
    
    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }
    
    public String getDepartmentName() {
        return departmentName;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
    
    public String getJobTitle() {
        return jobTitle;
    }
    
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
    
    public Integer getRunYear() {
        return runYear;
    }
    
    public void setRunYear(Integer runYear) {
        this.runYear = runYear;
    }
    
    public Integer getRunMonth() {
        return runMonth;
    }
    
    public void setRunMonth(Integer runMonth) {
        this.runMonth = runMonth;
    }
    
    public String getRunStatus() {
        return runStatus;
    }
    
    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }
    
    // Helper methods
    public BigDecimal getGrossSalary() {
        BigDecimal base = baseSalary != null ? baseSalary : BigDecimal.ZERO;
        BigDecimal bonusAmount = bonus != null ? bonus : BigDecimal.ZERO;
        return base.add(bonusAmount);
    }
    
    public BigDecimal getTotalDeductions() {
        return deductions != null ? deductions : BigDecimal.ZERO;
    }
    
    public String getPayrollPeriod() {
        return String.format("%04d-%02d", runYear, runMonth);
    }
    
    public String getDisplayName() {
        return employeeName != null ? employeeName : "Unknown Employee";
    }
    
    public boolean isPayslipAvailable() {
        return "Locked".equals(runStatus);
    }
}
