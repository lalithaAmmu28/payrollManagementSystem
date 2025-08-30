package com.pms.backend.dto.salary;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class SalaryStructureRequest {
    
    @NotNull(message = "Base salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base salary must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Base salary must have at most 10 integer digits and 2 decimal places")
    private BigDecimal baseSalary;
    
    private Map<String, Object> bonusDetails;
    
    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;
    
    @Future(message = "Effective to date must be in the future")
    private LocalDate effectiveTo;
    
    // Constructors
    public SalaryStructureRequest() {}
    
    public SalaryStructureRequest(BigDecimal baseSalary, Map<String, Object> bonusDetails, LocalDate effectiveFrom) {
        this.baseSalary = baseSalary;
        this.bonusDetails = bonusDetails;
        this.effectiveFrom = effectiveFrom;
    }
    
    // Getters and Setters
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
    }
    
    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }
    
    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }
    
    // Custom validation method
    @AssertTrue(message = "Effective to date must be after effective from date")
    public boolean isEffectiveToAfterEffectiveFrom() {
        if (effectiveFrom == null || effectiveTo == null) {
            return true; // Let other validations handle null checks
        }
        return effectiveTo.isAfter(effectiveFrom);
    }
}
