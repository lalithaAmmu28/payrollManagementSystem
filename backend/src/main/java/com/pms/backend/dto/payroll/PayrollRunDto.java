package com.pms.backend.dto.payroll;

import java.util.List;

public class PayrollRunDto {
    private Long id;
    private String period;
    private List<PayrollItemDto> items;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public List<PayrollItemDto> getItems() { return items; }
    public void setItems(List<PayrollItemDto> items) { this.items = items; }
}
