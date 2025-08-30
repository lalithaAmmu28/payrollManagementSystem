package com.pms.backend.repository;

import com.pms.backend.entity.JobRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRoleRepository extends JpaRepository<JobRole, String> {
    boolean existsByJobTitle(String jobTitle);
}
