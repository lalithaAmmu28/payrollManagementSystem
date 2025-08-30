package com.pms.backend.repository;

import com.pms.backend.entity.PayrollRun;
import com.pms.backend.entity.enums.PayrollStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PayrollRunRepository extends JpaRepository<PayrollRun, String> {
    
    /**
     * Find payroll run by year and month
     */
    Optional<PayrollRun> findByRunYearAndRunMonth(Integer runYear, Integer runMonth);
    
    /**
     * Check if a payroll run exists for a specific year and month
     */
    boolean existsByRunYearAndRunMonth(Integer runYear, Integer runMonth);
    
    /**
     * Find all payroll runs by status
     */
    List<PayrollRun> findByStatusOrderByRunYearDescRunMonthDesc(PayrollStatus status);
    
    /**
     * Find all payroll runs ordered by year and month (most recent first)
     */
    List<PayrollRun> findAllByOrderByRunYearDescRunMonthDesc();
    
    /**
     * Find payroll runs for a specific year
     */
    List<PayrollRun> findByRunYearOrderByRunMonthDesc(Integer runYear);
    
    /**
     * Count payroll runs by status
     */
    long countByStatus(PayrollStatus status);
    
    /**
     * Find the most recent payroll run
     */
    @Query("SELECT pr FROM PayrollRun pr ORDER BY pr.runYear DESC, pr.runMonth DESC")
    Optional<PayrollRun> findMostRecentPayrollRun();
    
    /**
     * Find all locked payroll runs for a specific employee
     */
    @Query("SELECT pr FROM PayrollRun pr WHERE pr.status = 'Locked' " +
           "AND EXISTS (SELECT pi FROM PayrollItem pi WHERE pi.runId = pr.runId AND pi.employeeId = :employeeId) " +
           "ORDER BY pr.runYear DESC, pr.runMonth DESC")
    List<PayrollRun> findLockedPayrollRunsForEmployee(@Param("employeeId") String employeeId);
    
    /**
     * Find payroll runs that need processing (draft status and older than current month)
     */
    @Query("SELECT pr FROM PayrollRun pr WHERE pr.status = 'Draft' " +
           "AND (pr.runYear < :currentYear OR (pr.runYear = :currentYear AND pr.runMonth < :currentMonth)) " +
           "ORDER BY pr.runYear ASC, pr.runMonth ASC")
    List<PayrollRun> findDraftRunsReadyForProcessing(@Param("currentYear") Integer currentYear, 
                                                    @Param("currentMonth") Integer currentMonth);
}