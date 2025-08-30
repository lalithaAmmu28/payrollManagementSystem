package com.pms.backend.dto.salary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class SalaryStructureResponse {
    
    private String structureId;
    private String employeeId;
    private BigDecimal baseSalary;
    private Map<String, Object> bonusDetails;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
    
    // Employee information (optional, for convenience)
    private String employeeName;
    private String employeeEmail;
    
    // Constructors
    public SalaryStructureResponse() {}
    
    public SalaryStructureResponse(String structureId, String employeeId, BigDecimal baseSalary,
                                 Map<String, Object> bonusDetails, LocalDate effectiveFrom,
                                 LocalDate effectiveTo, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.structureId = structureId;
        this.employeeId = employeeId;
        this.baseSalary = baseSalary;
        this.bonusDetails = bonusDetails;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isActive = calculateIsActive();
    }
    
    // Getters and Setters
    public String getStructureId() {
        return structureId;
    }
    
    public void setStructureId(String structureId) {
        this.structureId = structureId;
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
    
    public Map<String, Object> getBonusDetails() {
        return bonusDetails;
    }
    
    public void setBonusDetails(Map<String, Object> bonusDetails) {
        this.bonusDetails = bonusDetails;
    }
    
    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }
    
    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
        this.isActive = calculateIsActive();
    }
    
    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }
    
    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
        this.isActive = calculateIsActive();
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
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        this.isActive = active;
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
    
    // Helper method to calculate if structure is currently active
    private boolean calculateIsActive() {
        LocalDate today = LocalDate.now();
        return effectiveFrom != null && 
               !effectiveFrom.isAfter(today) && 
               (effectiveTo == null || !effectiveTo.isBefore(today));
    }
    
    // Helper method to get period description
    public String getPeriodDescription() {
        if (effectiveFrom == null) {
            return "Unknown period";
        }
        
        if (effectiveTo == null) {
            return "From " + effectiveFrom + " (ongoing)";
        }
        
        return "From " + effectiveFrom + " to " + effectiveTo;
    }
    
    // Helper method to calculate total compensation (if bonus details include amounts)
    public BigDecimal getTotalCompensation() {
        BigDecimal total = baseSalary != null ? baseSalary : BigDecimal.ZERO;
        
        if (bonusDetails != null) {
            for (Object value : bonusDetails.values()) {
                if (value instanceof Number) {
                    total = total.add(new BigDecimal(value.toString()));
                }
            }
        }
        
        return total;
    }
}
