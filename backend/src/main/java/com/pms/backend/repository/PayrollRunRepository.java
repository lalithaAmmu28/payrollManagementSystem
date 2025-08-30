package com.pms.backend.repository;

import com.pms.backend.entity.PayrollRun;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollRunRepository extends JpaRepository<PayrollRun, Long> {
}
