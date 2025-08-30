package com.pms.backend.repository;

import com.pms.backend.entity.PayrollItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollItemRepository extends JpaRepository<PayrollItem, Long> {
}
