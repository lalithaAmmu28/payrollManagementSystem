package com.pms.backend.dto.department;

import java.time.LocalDateTime;

public class DepartmentResponse {
    
    private String departmentId;
    private String departmentName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public DepartmentResponse() {}
    
    public DepartmentResponse(String departmentId, String departmentName, 
                            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
