package com.pms.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import com.pms.backend.entity.enums.PayrollStatus;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_runs")
public class PayrollRun {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "run_id", length = 36)
    private String runId;
    
    @Column(name = "run_month", nullable = false)
    private Integer runMonth;
    
    @Column(name = "run_year", nullable = false)
    private Integer runYear;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayrollStatus status = PayrollStatus.Draft;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public PayrollRun() {}
    
    // Getters and Setters
    public String getRunId() {
        return runId;
    }
    
    public void setRunId(String runId) {
        this.runId = runId;
    }
    
    public Integer getRunMonth() {
        return runMonth;
    }
    
    public void setRunMonth(Integer runMonth) {
        this.runMonth = runMonth;
    }
    
    public Integer getRunYear() {
        return runYear;
    }
    
    public void setRunYear(Integer runYear) {
        this.runYear = runYear;
    }
    
    public PayrollStatus getStatus() {
        return status;
    }
    
    public void setStatus(PayrollStatus status) {
        this.status = status;
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
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}