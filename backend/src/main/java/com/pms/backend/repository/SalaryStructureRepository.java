package com.pms.backend.repository;

import com.pms.backend.entity.SalaryStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, String> {
    
    /**
     * Find all salary structures for a given employee, ordered by effective date (newest first)
     */
    List<SalaryStructure> findByEmployeeIdOrderByEffectiveFromDesc(String employeeId);
    
    /**
     * Find the currently active salary structure for an employee
     */
    @Query("SELECT ss FROM SalaryStructure ss WHERE ss.employeeId = :employeeId " +
           "AND ss.effectiveFrom <= :currentDate " +
           "AND (ss.effectiveTo IS NULL OR ss.effectiveTo >= :currentDate)")
    Optional<SalaryStructure> findActiveStructureForEmployee(@Param("employeeId") String employeeId, 
                                                            @Param("currentDate") LocalDate currentDate);
    
    /**
     * Find the most recent salary structure for an employee that doesn't have an end date
     */
    @Query("SELECT ss FROM SalaryStructure ss WHERE ss.employeeId = :employeeId " +
           "AND ss.effectiveTo IS NULL " +
           "ORDER BY ss.effectiveFrom DESC")
    Optional<SalaryStructure> findCurrentOpenStructureForEmployee(@Param("employeeId") String employeeId);
    
    /**
     * Find salary structures that need to be closed when a new structure is added
     */
    @Query("SELECT ss FROM SalaryStructure ss WHERE ss.employeeId = :employeeId " +
           "AND ss.effectiveTo IS NULL " +
           "AND ss.effectiveFrom < :newEffectiveFrom " +
           "ORDER BY ss.effectiveFrom DESC")
    List<SalaryStructure> findStructuresToCloseForEmployee(@Param("employeeId") String employeeId, 
                                                          @Param("newEffectiveFrom") LocalDate newEffectiveFrom);
    
    /**
     * Check if there's any overlap with existing structures
     */
    @Query("SELECT COUNT(ss) > 0 FROM SalaryStructure ss WHERE ss.employeeId = :employeeId " +
           "AND ss.structureId != :excludeStructureId " +
           "AND ss.effectiveFrom <= :effectiveTo " +
           "AND (ss.effectiveTo IS NULL OR ss.effectiveTo >= :effectiveFrom)")
    boolean hasOverlapWithExistingStructures(@Param("employeeId") String employeeId,
                                           @Param("excludeStructureId") String excludeStructureId,
                                           @Param("effectiveFrom") LocalDate effectiveFrom,
                                           @Param("effectiveTo") LocalDate effectiveTo);
    
    /**
     * Count salary structures for an employee
     */
    long countByEmployeeId(String employeeId);
}
