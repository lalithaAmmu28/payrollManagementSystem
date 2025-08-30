package com.pms.backend.dto.payroll;

import com.pms.backend.entity.enums.PayrollStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PayrollRunResponse {
    
    private String runId;
    private Integer runYear;
    private Integer runMonth;
    private PayrollStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime processedAt;
    private LocalDateTime lockedAt;
    
    // Summary fields
    private Long employeeCount;
    private BigDecimal totalBaseSalary;
    private BigDecimal totalBonus;
    private BigDecimal totalDeductions;
    private BigDecimal totalNetSalary;
    
    // Constructors
    public PayrollRunResponse() {}
    
    // Getters and Setters
    public String getRunId() {
        return runId;
    }
    
    public void setRunId(String runId) {
        this.runId = runId;
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
    
    public PayrollStatus getStatus() {
        return status;
    }
    
    public void setStatus(PayrollStatus status) {
        this.status = status;
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
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    public LocalDateTime getLockedAt() {
        return lockedAt;
    }
    
    public void setLockedAt(LocalDateTime lockedAt) {
        this.lockedAt = lockedAt;
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
    
    // Helper methods
    public String getPeriodDescription() {
        return String.format("%04d-%02d", runYear, runMonth);
    }
    
    public String getStatusDescription() {
        return status != null ? status.name() : "Unknown";
    }
    
    public boolean isDraft() {
        return PayrollStatus.Draft.equals(status);
    }
    
    public boolean isProcessed() {
        return PayrollStatus.Processed.equals(status);
    }
    
    public boolean isLocked() {
        return PayrollStatus.Locked.equals(status);
    }
    
    public boolean canBeProcessed() {
        return isDraft() || isProcessed();
    }
    
    public boolean canBeLocked() {
        return isProcessed();
    }
    
    public BigDecimal getTotalGrossSalary() {
        BigDecimal base = totalBaseSalary != null ? totalBaseSalary : BigDecimal.ZERO;
        BigDecimal bonus = totalBonus != null ? totalBonus : BigDecimal.ZERO;
        return base.add(bonus);
    }
}
