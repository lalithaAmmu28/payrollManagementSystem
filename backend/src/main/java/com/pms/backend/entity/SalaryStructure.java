package com.pms.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "salary_structures")
public class SalaryStructure {
    
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "structure_id", length = 36)
    private String structureId;
    
    @Column(name = "employee_id", length = 36, nullable = false)
    private String employeeId;
    
    @Column(name = "base_salary", precision = 12, scale = 2, nullable = false)
    private BigDecimal baseSalary;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bonus_details", columnDefinition = "json")
    private Map<String, Object> bonusDetails;
    
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;
    
    @Column(name = "effective_to")
    private LocalDate effectiveTo;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // JPA Relationship (for easier data access)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private Employee employee;
    
    // Constructors
    public SalaryStructure() {}
    
    public SalaryStructure(String employeeId, BigDecimal baseSalary, Map<String, Object> bonusDetails, LocalDate effectiveFrom) {
        this.employeeId = employeeId;
        this.baseSalary = baseSalary;
        this.bonusDetails = bonusDetails;
        this.effectiveFrom = effectiveFrom;
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
    }
    
    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }
    
    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
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
    
    public Employee getEmployee() {
        return employee;
    }
    
    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods
    public boolean isActive(LocalDate date) {
        return !effectiveFrom.isAfter(date) && 
               (effectiveTo == null || !effectiveTo.isBefore(date));
    }
    
    public boolean isCurrentlyActive() {
        return isActive(LocalDate.now());
    }
}
