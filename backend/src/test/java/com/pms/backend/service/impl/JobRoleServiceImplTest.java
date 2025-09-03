package com.pms.backend.service.impl;

import com.pms.backend.dto.jobrole.JobRoleCreateRequest;
import com.pms.backend.dto.jobrole.JobRoleUpdateRequest;
import com.pms.backend.entity.JobRole;
import com.pms.backend.exception.BadRequestException;
import com.pms.backend.exception.ConstraintViolationException;
import com.pms.backend.exception.ResourceNotFoundException;
import com.pms.backend.repository.EmployeeRepository;
import com.pms.backend.repository.JobRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobRoleServiceImplTest {

    @Mock private JobRoleRepository jobRoleRepository;
    @Mock private EmployeeRepository employeeRepository;
    @InjectMocks private JobRoleServiceImpl jobRoleService;

    @Test
    void createJobRole_ShouldThrow_WhenTitleExists() {
        JobRoleCreateRequest req = new JobRoleCreateRequest();
        req.setJobTitle("Engineer");
        req.setBaseSalary(new BigDecimal("600000"));
        when(jobRoleRepository.existsByJobTitle("Engineer")).thenReturn(true);

        assertThatThrownBy(() -> jobRoleService.createJobRole(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void updateJobRole_ShouldThrow_WhenNewTitleConflicts() {
        JobRole role = new JobRole();
        role.setJobId("j1");
        role.setJobTitle("Old");
        when(jobRoleRepository.findById("j1")).thenReturn(Optional.of(role));

        JobRoleUpdateRequest req = new JobRoleUpdateRequest();
        req.setJobTitle("Engineer");
        req.setBaseSalary(new BigDecimal("700000"));
        when(jobRoleRepository.existsByJobTitle("Engineer")).thenReturn(true);

        assertThatThrownBy(() -> jobRoleService.updateJobRole("j1", req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void deleteJobRole_ShouldThrow_WhenAssignedToEmployees() {
        when(jobRoleRepository.existsById("j1")).thenReturn(true);
        when(employeeRepository.countByJobId("j1")).thenReturn(5L);

        assertThatThrownBy(() -> jobRoleService.deleteJobRole("j1"))
                .isInstanceOf(ConstraintViolationException.class);
        verify(jobRoleRepository, never()).deleteById(any());
    }

    @Test
    void deleteJobRole_ShouldThrow_WhenNotFound() {
        when(jobRoleRepository.existsById("missing")).thenReturn(false);
        assertThatThrownBy(() -> jobRoleService.deleteJobRole("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}


