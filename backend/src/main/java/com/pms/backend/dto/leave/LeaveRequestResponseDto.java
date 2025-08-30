package com.pms.backend.dto.leave;

import com.pms.backend.entity.enums.LeaveStatus;
import com.pms.backend.entity.enums.LeaveType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeaveRequestResponseDto {
    
    private String leaveId;
    private String employeeId;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveStatus status;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long durationInDays;
    
    // Employee information (for admin view)
    private String employeeName;
    private String employeeEmail;
    private String departmentName;
    
    // Constructors
    public LeaveRequestResponseDto() {}
    
    public LeaveRequestResponseDto(String leaveId, String employeeId, LeaveType leaveType,
                                 LocalDate startDate, LocalDate endDate, LeaveStatus status,
                                 String reason, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.leaveId = leaveId;
        this.employeeId = employeeId;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.durationInDays = calculateDuration();
    }
    
    // Getters and Setters
    public String getLeaveId() {
        return leaveId;
    }
    
    public void setLeaveId(String leaveId) {
        this.leaveId = leaveId;
    }
    
    public String getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
    
    public LeaveType getLeaveType() {
        return leaveType;
    }
    
    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        this.durationInDays = calculateDuration();
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        this.durationInDays = calculateDuration();
    }
    
    public LeaveStatus getStatus() {
        return status;
    }
    
    public void setStatus(LeaveStatus status) {
        this.status = status;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
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
    
    public long getDurationInDays() {
        return durationInDays;
    }
    
    public void setDurationInDays(long durationInDays) {
        this.durationInDays = durationInDays;
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
    
    // Helper methods
    private long calculateDuration() {
        if (startDate != null && endDate != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        }
        return 0;
    }
    
    public boolean isPending() {
        return LeaveStatus.Pending.equals(status);
    }
    
    public boolean isApproved() {
        return LeaveStatus.Approved.equals(status);
    }
    
    public boolean isRejected() {
        return LeaveStatus.Rejected.equals(status);
    }
    
    public boolean isPaidLeave() {
        return LeaveType.Paid.equals(leaveType);
    }
    
    public String getStatusDisplay() {
        return status != null ? status.name() : "Unknown";
    }
    
    public String getLeaveTypeDisplay() {
        return leaveType != null ? leaveType.name() : "Unknown";
    }
    
    public String getPeriodDescription() {
        if (startDate != null && endDate != null) {
            if (startDate.equals(endDate)) {
                return startDate.toString() + " (1 day)";
            } else {
                return startDate + " to " + endDate + " (" + durationInDays + " days)";
            }
        }
        return "Invalid period";
    }
}
