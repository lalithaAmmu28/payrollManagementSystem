package com.pms.backend.service.impl;

import com.pms.backend.entity.LeaveRequest;
import com.pms.backend.entity.SalaryStructure;
import com.pms.backend.entity.enums.LeaveStatus;
import com.pms.backend.entity.enums.LeaveType;
import com.pms.backend.repository.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PayrollCalculationHelper {
    
    private final LeaveRequestRepository leaveRequestRepository;
    
    @Autowired
    public PayrollCalculationHelper(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }
    
    /**
     * Calculate bonus based on salary structure's bonus details JSON
     */
    public BigDecimal calculateBonus(SalaryStructure salaryStructure, BigDecimal baseSalary) {
        Map<String, Object> bonusDetails = salaryStructure.getBonusDetails();
        
        if (bonusDetails == null || bonusDetails.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Convert annual CTC to monthly base salary
        BigDecimal monthlyBaseSalary = baseSalary.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        
        try {
            // Check for percentage-based bonus
            if (bonusDetails.containsKey("percentage")) {
                Object percentageObj = bonusDetails.get("percentage");
                BigDecimal percentage = new BigDecimal(percentageObj.toString());
                return monthlyBaseSalary.multiply(percentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
            
            // Check for fixed amount bonus
            if (bonusDetails.containsKey("amount")) {
                Object amountObj = bonusDetails.get("amount");
                return new BigDecimal(amountObj.toString());
            }
            
            // Check for fixed bonus (alternative key)
            if (bonusDetails.containsKey("fixed")) {
                Object fixedObj = bonusDetails.get("fixed");
                return new BigDecimal(fixedObj.toString());
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing bonus details: " + e.getMessage());
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * Calculate Loss of Pay deduction for unpaid leave days
     */
    public BigDecimal calculateLossOfPayDeduction(String employeeId, LocalDate startDate, 
                                                 LocalDate endDate, BigDecimal baseSalary, int daysInMonth) {
        
        // Convert annual CTC to monthly base salary
        BigDecimal monthlyBaseSalary = baseSalary.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        
        // Query for approved unpaid leaves that overlap with payroll month
        List<LeaveRequest> unpaidLeaves = leaveRequestRepository.findAll().stream()
                .filter(leave -> employeeId.equals(leave.getEmployeeId()))
                .filter(leave -> LeaveStatus.Approved.equals(leave.getStatus()))
                .filter(leave -> LeaveType.Sick.equals(leave.getLeaveType()) || 
                               LeaveType.Casual.equals(leave.getLeaveType()))
                .filter(leave -> leavesOverlap(leave.getStartDate(), leave.getEndDate(), startDate, endDate))
                .collect(Collectors.toList());
        
        if (unpaidLeaves.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Calculate total unpaid days within the payroll month
        long totalUnpaidDays = 0;
        for (LeaveRequest leave : unpaidLeaves) {
            LocalDate leaveStart = leave.getStartDate().isBefore(startDate) ? startDate : leave.getStartDate();
            LocalDate leaveEnd = leave.getEndDate().isAfter(endDate) ? endDate : leave.getEndDate();
            
            long daysInPeriod = java.time.temporal.ChronoUnit.DAYS.between(leaveStart, leaveEnd) + 1;
            totalUnpaidDays += daysInPeriod;
            
            System.out.println(String.format("    Unpaid leave: %s to %s (%d days in payroll period)", 
                                           leave.getStartDate(), leave.getEndDate(), daysInPeriod));
        }
        
        if (totalUnpaidDays == 0) {
            return BigDecimal.ZERO;
        }
        
        // Calculate per-day salary and total deduction using monthly base salary
        BigDecimal perDaySalary = monthlyBaseSalary.divide(BigDecimal.valueOf(daysInMonth), 2, RoundingMode.HALF_UP);
        BigDecimal totalDeduction = perDaySalary.multiply(BigDecimal.valueOf(totalUnpaidDays));
        
        System.out.println(String.format("    Total unpaid days: %d, Per-day salary: %s, Total deduction: %s", 
                                       totalUnpaidDays, perDaySalary, totalDeduction));
        
        return totalDeduction;
    }
    
    /**
     * Check if two date ranges overlap
     */
    private boolean leavesOverlap(LocalDate leaveStart, LocalDate leaveEnd, 
                                LocalDate periodStart, LocalDate periodEnd) {
        return leaveStart.compareTo(periodEnd) <= 0 && leaveEnd.compareTo(periodStart) >= 0;
    }
}
