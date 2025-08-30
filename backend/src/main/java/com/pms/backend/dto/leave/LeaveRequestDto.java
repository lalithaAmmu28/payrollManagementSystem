package com.pms.backend.dto.leave;

public class LeaveRequestDto {
    private Long id;
    private Long employeeId;
    private String fromDate;
    private String toDate;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getFromDate() { return fromDate; }
    public void setFromDate(String fromDate) { this.fromDate = fromDate; }
    public String getToDate() { return toDate; }
    public void setToDate(String toDate) { this.toDate = toDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
