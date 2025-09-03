package com.pms.backend.service.impl;

import com.pms.backend.dto.salary.SalaryStructureRequest;
import com.pms.backend.entity.SalaryStructure;
import com.pms.backend.exception.BadRequestException;
import com.pms.backend.exception.ResourceNotFoundException;
import com.pms.backend.repository.EmployeeRepository;
import com.pms.backend.repository.SalaryStructureRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaryStructureServiceImplTest {

    @Mock private SalaryStructureRepository salaryStructureRepository;
    @Mock private EmployeeRepository employeeRepository;
    @InjectMocks private SalaryStructureServiceImpl salaryService;

    @Test
    void assignNewStructure_ShouldThrow_WhenEmployeeMissing() {
        when(employeeRepository.existsById("emp-x")).thenReturn(false);
        SalaryStructureRequest req = new SalaryStructureRequest();
        req.setBaseSalary(new BigDecimal("100000"));
        req.setEffectiveFrom(LocalDate.now());

        assertThatThrownBy(() -> salaryService.assignNewStructure("emp-x", req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void assignNewStructure_ShouldThrow_WhenEffectiveToBeforeFrom() {
        when(employeeRepository.existsById("emp-1")).thenReturn(true);
        SalaryStructureRequest req = new SalaryStructureRequest();
        req.setBaseSalary(new BigDecimal("100000"));
        req.setEffectiveFrom(LocalDate.of(2025, 8, 10));
        req.setEffectiveTo(LocalDate.of(2025, 8, 9));

        assertThatThrownBy(() -> salaryService.assignNewStructure("emp-1", req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void updateStructure_ShouldThrow_WhenOverlapDetected() {
        SalaryStructure existing = new SalaryStructure();
        existing.setStructureId("s1");
        existing.setEmployeeId("emp-1");
        when(salaryStructureRepository.findById("s1")).thenReturn(Optional.of(existing));
        when(salaryStructureRepository.hasOverlapWithExistingStructures(eq("emp-1"), eq("s1"), any(), any()))
                .thenReturn(true);

        SalaryStructureRequest req = new SalaryStructureRequest();
        req.setBaseSalary(new BigDecimal("120000"));
        req.setEffectiveFrom(LocalDate.of(2025, 8, 1));
        req.setEffectiveTo(LocalDate.of(2025, 12, 31));

        assertThatThrownBy(() -> salaryService.updateStructure("s1", req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("overlap");
    }

    @Test
    void deleteStructure_ShouldThrow_WhenNotFound() {
        when(salaryStructureRepository.existsById("missing")).thenReturn(false);
        assertThatThrownBy(() -> salaryService.deleteStructure("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}


