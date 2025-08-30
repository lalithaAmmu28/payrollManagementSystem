package com.pms.backend.dto.leave;

import com.pms.backend.entity.enums.LeaveType;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class LeaveRequestCreateDto {
    
    @NotNull(message = "Leave type is required")
    private LeaveType leaveType;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;
    
    // Constructors
    public LeaveRequestCreateDto() {}
    
    public LeaveRequestCreateDto(LeaveType leaveType, LocalDate startDate, LocalDate endDate, String reason) {
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
    }
    
    // Getters and Setters
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
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    // Custom validation method
    @AssertTrue(message = "End date must be on or after start date")
    public boolean isEndDateValid() {
        if (startDate == null || endDate == null) {
            return true; // Let other validations handle null checks
        }
        return !endDate.isBefore(startDate);
    }
    
    // Helper method to get duration in days
    public long getDurationInDays() {
        if (startDate != null && endDate != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        }
        return 0;
    }
}
