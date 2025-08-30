package com.pms.backend.dto.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DepartmentCreateRequest {
    
    @NotBlank(message = "Department name is required")
    @Size(min = 2, max = 100, message = "Department name must be between 2 and 100 characters")
    private String departmentName;
    
    // Constructors
    public DepartmentCreateRequest() {}
    
    public DepartmentCreateRequest(String departmentName) {
        this.departmentName = departmentName;
    }
    
    // Getters and Setters
    public String getDepartmentName() {
        return departmentName;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
}
