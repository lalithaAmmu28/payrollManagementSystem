package com.pms.backend.dto.leave;

import com.pms.backend.entity.enums.LeaveStatus;
import jakarta.validation.constraints.NotNull;

public class LeaveStatusUpdateDto {
    
    @NotNull(message = "Status is required")
    private LeaveStatus status;
    
    private String adminComments;
    
    // Constructors
    public LeaveStatusUpdateDto() {}
    
    public LeaveStatusUpdateDto(LeaveStatus status) {
        this.status = status;
    }
    
    public LeaveStatusUpdateDto(LeaveStatus status, String adminComments) {
        this.status = status;
        this.adminComments = adminComments;
    }
    
    // Getters and Setters
    public LeaveStatus getStatus() {
        return status;
    }
    
    public void setStatus(LeaveStatus status) {
        this.status = status;
    }
    
    public String getAdminComments() {
        return adminComments;
    }
    
    public void setAdminComments(String adminComments) {
        this.adminComments = adminComments;
    }
    
    // Helper methods
    public boolean isApproval() {
        return LeaveStatus.Approved.equals(status);
    }
    
    public boolean isRejection() {
        return LeaveStatus.Rejected.equals(status);
    }
    
    public boolean isPending() {
        return LeaveStatus.Pending.equals(status);
    }
}
