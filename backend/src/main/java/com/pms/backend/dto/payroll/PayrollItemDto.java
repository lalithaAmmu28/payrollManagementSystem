package com.pms.backend.dto.payroll;

public class PayrollItemDto {
    private Long id;
    private Long employeeId;
    private double amount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
