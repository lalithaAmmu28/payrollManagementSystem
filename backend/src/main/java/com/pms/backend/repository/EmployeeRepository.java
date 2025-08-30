package com.pms.backend.repository;

import com.pms.backend.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, String> {
    long countByJobId(String jobId);
    long countByDepartmentId(String departmentId);
}
