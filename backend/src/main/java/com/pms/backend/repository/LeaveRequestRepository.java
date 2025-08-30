package com.pms.backend.repository;

import com.pms.backend.entity.LeaveRequest;
import com.pms.backend.entity.enums.LeaveStatus;
import com.pms.backend.entity.enums.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, String> {
    
    /**
     * Find all leave requests by employee ID, ordered by creation date (newest first)
     */
    List<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(String employeeId);
    
    /**
     * Find all leave requests by status, ordered by creation date (oldest first for pending)
     */
    List<LeaveRequest> findByStatusOrderByCreatedAtAsc(LeaveStatus status);
    
    /**
     * Find all leave requests ordered by creation date (newest first)
     */
    List<LeaveRequest> findAllByOrderByCreatedAtDesc();
    
    /**
     * Find leave requests by employee and status
     */
    List<LeaveRequest> findByEmployeeIdAndStatusOrderByCreatedAtDesc(String employeeId, LeaveStatus status);
    
    /**
     * Find overlapping leave requests for an employee within a date range
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employeeId = :employeeId " +
           "AND lr.status IN ('Pending', 'Approved') " +
           "AND lr.startDate <= :endDate AND lr.endDate >= :startDate " +
           "AND (:excludeLeaveId IS NULL OR lr.leaveId != :excludeLeaveId)")
    List<LeaveRequest> findOverlappingLeaveRequests(@Param("employeeId") String employeeId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate,
                                                   @Param("excludeLeaveId") String excludeLeaveId);
    
    /**
     * Count pending leave requests for an employee
     */
    long countByEmployeeIdAndStatus(String employeeId, LeaveStatus status);
    
    /**
     * Find recent leave requests for an employee (last 30 days)
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employeeId = :employeeId " +
           "AND lr.createdAt >= :fromDate ORDER BY lr.createdAt DESC")
    List<LeaveRequest> findRecentLeaveRequests(@Param("employeeId") String employeeId,
                                             @Param("fromDate") LocalDate fromDate);
    
    /**
     * Find all approved paid leave requests for an employee in a specific period
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employeeId = :employeeId " +
           "AND lr.status = 'Approved' AND lr.leaveType = 'Paid' " +
           "AND lr.startDate >= :fromDate AND lr.endDate <= :toDate " +
           "ORDER BY lr.startDate DESC")
    List<LeaveRequest> findApprovedPaidLeaveInPeriod(@Param("employeeId") String employeeId,
                                                    @Param("fromDate") LocalDate fromDate,
                                                    @Param("toDate") LocalDate toDate);
    
    /**
     * Check if employee has any pending leave requests
     */
    boolean existsByEmployeeIdAndStatus(String employeeId, LeaveStatus status);
    
    // ====== REPORTING QUERIES ======
    
    /**
     * Get leave trends by leave type for a specific period
     * Returns: leaveType, totalRequests, approvedRequests, rejectedRequests, 
     *         pendingRequests, totalApprovedDays
     */
    @Query("SELECT " +
           "lr.leaveType, " +
           "COUNT(lr), " +
           "COUNT(CASE WHEN lr.status = 'Approved' THEN 1 END), " +
           "COUNT(CASE WHEN lr.status = 'Rejected' THEN 1 END), " +
           "COUNT(CASE WHEN lr.status = 'Pending' THEN 1 END), " +
           "COALESCE(SUM(CASE WHEN lr.status = 'Approved' THEN " +
           "   (FUNCTION('DATEDIFF', lr.endDate, lr.startDate) + 1) ELSE 0 END), 0) " +
           "FROM LeaveRequest lr " +
           "WHERE (:startDate IS NULL OR lr.startDate >= :startDate) " +
           "AND (:endDate IS NULL OR lr.endDate <= :endDate) " +
           "AND (:year IS NULL OR FUNCTION('YEAR', lr.startDate) = :year) " +
           "GROUP BY lr.leaveType " +
           "ORDER BY lr.leaveType")
    List<Object[]> getLeaveTrendsReport(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate,
                                       @Param("year") Integer year);
    
    /**
     * Get leave trends by department for a specific period
     * Returns: departmentId, departmentName, leaveType, totalRequests, approvedRequests, totalApprovedDays
     */
    @Query("SELECT " +
           "d.departmentId, " +
           "d.departmentName, " +
           "lr.leaveType, " +
           "COUNT(lr), " +
           "COUNT(CASE WHEN lr.status = 'Approved' THEN 1 END), " +
           "COALESCE(SUM(CASE WHEN lr.status = 'Approved' THEN " +
           "   (FUNCTION('DATEDIFF', lr.endDate, lr.startDate) + 1) ELSE 0 END), 0) " +
           "FROM LeaveRequest lr " +
           "JOIN lr.employee e " +
           "JOIN e.department d " +
           "WHERE (:startDate IS NULL OR lr.startDate >= :startDate) " +
           "AND (:endDate IS NULL OR lr.endDate <= :endDate) " +
           "AND (:year IS NULL OR FUNCTION('YEAR', lr.startDate) = :year) " +
           "GROUP BY d.departmentId, d.departmentName, lr.leaveType " +
           "ORDER BY d.departmentName, lr.leaveType")
    List<Object[]> getLeaveTrendsByDepartment(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate,
                                            @Param("year") Integer year);
    
    /**
     * Get monthly leave statistics for a specific year
     * Returns: month, totalRequests, approvedRequests, rejectedRequests, pendingRequests, totalApprovedDays
     */
    @Query("SELECT " +
           "FUNCTION('MONTH', lr.startDate), " +
           "COUNT(lr), " +
           "COUNT(CASE WHEN lr.status = 'Approved' THEN 1 END), " +
           "COUNT(CASE WHEN lr.status = 'Rejected' THEN 1 END), " +
           "COUNT(CASE WHEN lr.status = 'Pending' THEN 1 END), " +
           "COALESCE(SUM(CASE WHEN lr.status = 'Approved' THEN " +
           "   (FUNCTION('DATEDIFF', lr.endDate, lr.startDate) + 1) ELSE 0 END), 0) " +
           "FROM LeaveRequest lr " +
           "WHERE FUNCTION('YEAR', lr.startDate) = :year " +
           "GROUP BY FUNCTION('MONTH', lr.startDate) " +
           "ORDER BY FUNCTION('MONTH', lr.startDate)")
    List<Object[]> getMonthlyLeaveStatistics(@Param("year") Integer year);
    
    /**
     * Get top leave-taking employees
     * Returns: employeeId, firstName, lastName, departmentName, totalApprovedDays
     */
    @Query("SELECT " +
           "e.employeeId, " +
           "e.firstName, " +
           "e.lastName, " +
           "d.departmentName, " +
           "COALESCE(SUM(CASE WHEN lr.status = 'Approved' THEN " +
           "   (FUNCTION('DATEDIFF', lr.endDate, lr.startDate) + 1) ELSE 0 END), 0) " +
           "FROM LeaveRequest lr " +
           "JOIN lr.employee e " +
           "JOIN e.department d " +
           "WHERE (:year IS NULL OR FUNCTION('YEAR', lr.startDate) = :year) " +
           "AND (:leaveType IS NULL OR lr.leaveType = :leaveType) " +
           "GROUP BY e.employeeId, e.firstName, e.lastName, d.departmentName " +
           "HAVING COALESCE(SUM(CASE WHEN lr.status = 'Approved' THEN " +
           "   (FUNCTION('DATEDIFF', lr.endDate, lr.startDate) + 1) ELSE 0 END), 0) > 0 " +
           "ORDER BY COALESCE(SUM(CASE WHEN lr.status = 'Approved' THEN " +
           "   (FUNCTION('DATEDIFF', lr.endDate, lr.startDate) + 1) ELSE 0 END), 0) DESC")
    List<Object[]> getTopLeaveTakingEmployees(@Param("year") Integer year,
                                            @Param("leaveType") LeaveType leaveType);
    
    /**
     * Get overall leave statistics
     * Returns: totalRequests, approvedRequests, rejectedRequests, pendingRequests, totalApprovedDays
     */
    @Query("SELECT " +
           "COUNT(lr), " +
           "COUNT(CASE WHEN lr.status = 'Approved' THEN 1 END), " +
           "COUNT(CASE WHEN lr.status = 'Rejected' THEN 1 END), " +
           "COUNT(CASE WHEN lr.status = 'Pending' THEN 1 END), " +
           "COALESCE(SUM(CASE WHEN lr.status = 'Approved' THEN " +
           "   (FUNCTION('DATEDIFF', lr.endDate, lr.startDate) + 1) ELSE 0 END), 0) " +
           "FROM LeaveRequest lr " +
           "WHERE (:startDate IS NULL OR lr.startDate >= :startDate) " +
           "AND (:endDate IS NULL OR lr.endDate <= :endDate) " +
           "AND (:year IS NULL OR FUNCTION('YEAR', lr.startDate) = :year)")
    Object[] getOverallLeaveStatistics(@Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate,
                                     @Param("year") Integer year);
}