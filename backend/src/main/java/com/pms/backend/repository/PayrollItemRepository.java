package com.pms.backend.repository;

import com.pms.backend.entity.PayrollItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PayrollItemRepository extends JpaRepository<PayrollItem, String> {
    
    /**
     * Delete all payroll items for a specific run
     * Critical for re-processing functionality
     */
    @Modifying
    @Query("DELETE FROM PayrollItem pi WHERE pi.runId = :runId")
    void deleteByRunId(@Param("runId") String runId);
    
    /**
     * Find all payroll items for a specific run
     */
    List<PayrollItem> findByRunIdOrderByEmployeeId(String runId);
    
    /**
     * Find payroll item for a specific run and employee
     */
    Optional<PayrollItem> findByRunIdAndEmployeeId(String runId, String employeeId);
    
    /**
     * Find all payroll items for a specific employee ordered by most recent first
     */
    @Query("SELECT pi FROM PayrollItem pi " +
           "JOIN pi.payrollRun pr " +
           "WHERE pi.employeeId = :employeeId " +
           "ORDER BY pr.runYear DESC, pr.runMonth DESC")
    List<PayrollItem> findByEmployeeIdOrderByPayrollRunDesc(@Param("employeeId") String employeeId);
    
    /**
     * Count payroll items for a specific run
     */
    long countByRunId(String runId);
    
    /**
     * Bulk update pay dates for all items in a run
     * Used when locking a payroll run
     */
    @Modifying
    @Query("UPDATE PayrollItem pi SET pi.payDate = :payDate WHERE pi.runId = :runId")
    void updatePayDateForRun(@Param("runId") String runId, @Param("payDate") LocalDate payDate);
    
    /**
     * Find payroll items by run ID with employee and run details
     */
    @Query("SELECT pi FROM PayrollItem pi " +
           "JOIN FETCH pi.employee e " +
           "JOIN FETCH pi.payrollRun pr " +
           "WHERE pi.runId = :runId " +
           "ORDER BY e.firstName ASC, e.lastName ASC")
    List<PayrollItem> findByRunIdWithEmployeeDetails(@Param("runId") String runId);
    
    /**
     * Find all payroll items for locked runs for a specific employee
     */
    @Query("SELECT pi FROM PayrollItem pi " +
           "JOIN pi.payrollRun pr " +
           "WHERE pi.employeeId = :employeeId AND pr.status = 'Locked' " +
           "ORDER BY pr.runYear DESC, pr.runMonth DESC")
    List<PayrollItem> findLockedPayrollItemsForEmployee(@Param("employeeId") String employeeId);
    
    /**
     * Calculate total payroll amount for a run
     */
    @Query("SELECT COALESCE(SUM(pi.netSalary), 0) FROM PayrollItem pi WHERE pi.runId = :runId")
    java.math.BigDecimal calculateTotalPayrollForRun(@Param("runId") String runId);
    
    /**
     * Get payroll summary statistics for a run
     */
    @Query("SELECT " +
           "COUNT(pi), " +
           "COALESCE(SUM(pi.baseSalary), 0), " +
           "COALESCE(SUM(pi.bonus), 0), " +
           "COALESCE(SUM(pi.deductions), 0), " +
           "COALESCE(SUM(pi.netSalary), 0) " +
           "FROM PayrollItem pi WHERE pi.runId = :runId")
    Object[] getPayrollSummaryForRun(@Param("runId") String runId);
    
    // ====== REPORTING QUERIES ======
    
    /**
     * Get department-wise payroll costs for a specific period
     * Returns: departmentId, departmentName, employeeCount, totalBaseSalary, 
     *         totalBonus, totalDeductions, totalNetSalary
     */
    @Query("SELECT " +
           "d.departmentId, " +
           "d.departmentName, " +
           "COUNT(DISTINCT pi.employeeId), " +
           "COALESCE(SUM(pi.baseSalary), 0), " +
           "COALESCE(SUM(pi.bonus), 0), " +
           "COALESCE(SUM(pi.deductions), 0), " +
           "COALESCE(SUM(pi.netSalary), 0) " +
           "FROM PayrollItem pi " +
           "JOIN pi.payrollRun pr " +
           "JOIN pi.employee e " +
           "JOIN e.department d " +
           "WHERE (:year IS NULL OR pr.runYear = :year) " +
           "AND (:month IS NULL OR pr.runMonth = :month) " +
           "AND (:startYear IS NULL OR pr.runYear >= :startYear) " +
           "AND (:endYear IS NULL OR pr.runYear <= :endYear) " +
           "AND pr.status = 'Locked' " +
           "GROUP BY d.departmentId, d.departmentName " +
           "ORDER BY COALESCE(SUM(pi.netSalary), 0) DESC")
    List<Object[]> getDepartmentCostReport(@Param("year") Integer year, 
                                         @Param("month") Integer month,
                                         @Param("startYear") Integer startYear,
                                         @Param("endYear") Integer endYear);
    
    /**
     * Get monthly payroll summary for specified year range
     * Returns: year, month, employeeCount, totalBaseSalary, totalBonus, 
     *         totalDeductions, totalNetSalary, totalRuns, lockedRuns
     */
    @Query("SELECT " +
           "pr.runYear, " +
           "pr.runMonth, " +
           "COUNT(DISTINCT pi.employeeId), " +
           "COALESCE(SUM(pi.baseSalary), 0), " +
           "COALESCE(SUM(pi.bonus), 0), " +
           "COALESCE(SUM(pi.deductions), 0), " +
           "COALESCE(SUM(pi.netSalary), 0), " +
           "COUNT(DISTINCT pr.runId), " +
           "COUNT(DISTINCT CASE WHEN pr.status = 'Locked' THEN pr.runId END) " +
           "FROM PayrollItem pi " +
           "JOIN pi.payrollRun pr " +
           "WHERE (:startYear IS NULL OR pr.runYear >= :startYear) " +
           "AND (:endYear IS NULL OR pr.runYear <= :endYear) " +
           "GROUP BY pr.runYear, pr.runMonth " +
           "ORDER BY pr.runYear DESC, pr.runMonth DESC")
    List<Object[]> getMonthlyPayrollSummary(@Param("startYear") Integer startYear, 
                                          @Param("endYear") Integer endYear);
    
    /**
     * Get overall payroll summary (aggregated across all periods)
     * Returns: totalEmployees, totalBaseSalary, totalBonus, 
     *         totalDeductions, totalNetSalary, totalRuns, lockedRuns
     */
    @Query("SELECT " +
           "COUNT(DISTINCT pi.employeeId), " +
           "COALESCE(SUM(pi.baseSalary), 0), " +
           "COALESCE(SUM(pi.bonus), 0), " +
           "COALESCE(SUM(pi.deductions), 0), " +
           "COALESCE(SUM(pi.netSalary), 0), " +
           "COUNT(DISTINCT pr.runId), " +
           "COUNT(DISTINCT CASE WHEN pr.status = 'Locked' THEN pr.runId END) " +
           "FROM PayrollItem pi " +
           "JOIN pi.payrollRun pr " +
           "WHERE (:startYear IS NULL OR pr.runYear >= :startYear) " +
           "AND (:endYear IS NULL OR pr.runYear <= :endYear)")
    Object[] getOverallPayrollSummary(@Param("startYear") Integer startYear, 
                                    @Param("endYear") Integer endYear);
    
    /**
     * Get top spending departments
     * Returns: departmentId, departmentName, totalNetSalary
     */
    @Query("SELECT " +
           "d.departmentId, " +
           "d.departmentName, " +
           "COALESCE(SUM(pi.netSalary), 0) " +
           "FROM PayrollItem pi " +
           "JOIN pi.payrollRun pr " +
           "JOIN pi.employee e " +
           "JOIN e.department d " +
           "WHERE pr.status = 'Locked' " +
           "AND (:year IS NULL OR pr.runYear = :year) " +
           "GROUP BY d.departmentId, d.departmentName " +
           "ORDER BY COALESCE(SUM(pi.netSalary), 0) DESC")
    List<Object[]> getTopSpendingDepartments(@Param("year") Integer year);
}