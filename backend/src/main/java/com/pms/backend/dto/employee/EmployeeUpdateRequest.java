package com.pms.backend.dto.employee;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeeUpdateRequest {
    
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    private String lastName;
    
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    @Pattern(regexp = "^[\\+]?[1-9][\\d]{0,15}$", message = "Please provide a valid phone number")
    private String phone;
    
    private String address;
    
    @NotBlank(message = "Job ID is required")
    private String jobId;
    
    @NotBlank(message = "Department ID is required")
    private String departmentId;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Leave balance cannot be negative")
    @DecimalMax(value = "999.99", message = "Leave balance cannot exceed 999.99")
    private BigDecimal leaveBalance;
    
    // Constructors
    public EmployeeUpdateRequest() {}
    
    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getJobId() {
        return jobId;
    }
    
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
    
    public String getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }
    
    public BigDecimal getLeaveBalance() {
        return leaveBalance;
    }
    
    public void setLeaveBalance(BigDecimal leaveBalance) {
        this.leaveBalance = leaveBalance;
    }
}
