package com.pms.backend.repository;

import com.pms.backend.entity.LeaveRequest;
import com.pms.backend.entity.enums.LeaveStatus;
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
}