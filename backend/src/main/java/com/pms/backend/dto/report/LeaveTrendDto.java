package com.pms.backend.dto.report;

import com.pms.backend.entity.enums.LeaveType;

/**
 * DTO for Leave Usage Trends Report
 * Represents leave statistics by type
 */
public class LeaveTrendDto {
    
    private LeaveType leaveType;
    private Long totalRequests;
    private Long approvedRequests;
    private Long rejectedRequests;
    private Long pendingRequests;
    private Long totalApprovedDays;
    private Double averageDaysPerRequest;
    private Double approvalRate; // Percentage of approved requests
    
    // Default constructor
    public LeaveTrendDto() {}
    
    // Constructor for repository queries
    public LeaveTrendDto(LeaveType leaveType, Long totalRequests, Long approvedRequests, 
                        Long rejectedRequests, Long pendingRequests, Long totalApprovedDays) {
        this.leaveType = leaveType;
        this.totalRequests = totalRequests;
        this.approvedRequests = approvedRequests;
        this.rejectedRequests = rejectedRequests;
        this.pendingRequests = pendingRequests;
        this.totalApprovedDays = totalApprovedDays;
        
        // Calculate derived metrics
        if (approvedRequests != null && approvedRequests > 0 && totalApprovedDays != null) {
            this.averageDaysPerRequest = totalApprovedDays.doubleValue() / approvedRequests.doubleValue();
        } else {
            this.averageDaysPerRequest = 0.0;
        }
        
        if (totalRequests != null && totalRequests > 0 && approvedRequests != null) {
            this.approvalRate = (approvedRequests.doubleValue() / totalRequests.doubleValue()) * 100.0;
        } else {
            this.approvalRate = 0.0;
        }
    }
    
    // Getters and Setters
    public LeaveType getLeaveType() {
        return leaveType;
    }
    
    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }
    
    public Long getTotalRequests() {
        return totalRequests;
    }
    
    public void setTotalRequests(Long totalRequests) {
        this.totalRequests = totalRequests;
    }
    
    public Long getApprovedRequests() {
        return approvedRequests;
    }
    
    public void setApprovedRequests(Long approvedRequests) {
        this.approvedRequests = approvedRequests;
    }
    
    public Long getRejectedRequests() {
        return rejectedRequests;
    }
    
    public void setRejectedRequests(Long rejectedRequests) {
        this.rejectedRequests = rejectedRequests;
    }
    
    public Long getPendingRequests() {
        return pendingRequests;
    }
    
    public void setPendingRequests(Long pendingRequests) {
        this.pendingRequests = pendingRequests;
    }
    
    public Long getTotalApprovedDays() {
        return totalApprovedDays;
    }
    
    public void setTotalApprovedDays(Long totalApprovedDays) {
        this.totalApprovedDays = totalApprovedDays;
    }
    
    public Double getAverageDaysPerRequest() {
        return averageDaysPerRequest;
    }
    
    public void setAverageDaysPerRequest(Double averageDaysPerRequest) {
        this.averageDaysPerRequest = averageDaysPerRequest;
    }
    
    public Double getApprovalRate() {
        return approvalRate;
    }
    
    public void setApprovalRate(Double approvalRate) {
        this.approvalRate = approvalRate;
    }
    
    // Helper method to get leave type display name
    public String getLeaveTypeName() {
        return leaveType != null ? leaveType.name() : "Unknown";
    }
}
