package com.pms.backend.dto.payroll;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PayrollRunCreateRequest {
    
    @NotNull(message = "Year is required")
    @Min(value = 2020, message = "Year must be 2020 or later")
    @Max(value = 2050, message = "Year cannot exceed 2050")
    private Integer year;
    
    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;
    
    // Constructors
    public PayrollRunCreateRequest() {}
    
    public PayrollRunCreateRequest(Integer year, Integer month) {
        this.year = year;
        this.month = month;
    }
    
    // Getters and Setters
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public Integer getMonth() {
        return month;
    }
    
    public void setMonth(Integer month) {
        this.month = month;
    }
    
    // Helper methods
    public String getPeriodDescription() {
        return String.format("%04d-%02d", year, month);
    }
    
    public java.time.YearMonth getYearMonth() {
        return java.time.YearMonth.of(year, month);
    }
}
