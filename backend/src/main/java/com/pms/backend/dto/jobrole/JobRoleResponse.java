package com.pms.backend.dto.jobrole;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class JobRoleResponse {
    
    private String jobId;
    private String jobTitle;
    private BigDecimal baseSalary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public JobRoleResponse() {}
    
    public JobRoleResponse(String jobId, String jobTitle, BigDecimal baseSalary,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.baseSalary = baseSalary;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public String getJobId() {
        return jobId;
    }
    
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
    
    public String getJobTitle() {
        return jobTitle;
    }
    
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
    
    public BigDecimal getBaseSalary() {
        return baseSalary;
    }
    
    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
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
}
