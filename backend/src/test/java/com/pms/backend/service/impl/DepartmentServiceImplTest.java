package com.pms.backend.service.impl;

import com.pms.backend.dto.department.DepartmentCreateRequest;
import com.pms.backend.dto.department.DepartmentUpdateRequest;
import com.pms.backend.entity.Department;
import com.pms.backend.exception.BadRequestException;
import com.pms.backend.exception.ConstraintViolationException;
import com.pms.backend.exception.ResourceNotFoundException;
import com.pms.backend.repository.DepartmentRepository;
import com.pms.backend.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock private DepartmentRepository departmentRepository;
    @Mock private EmployeeRepository employeeRepository;
    @InjectMocks private DepartmentServiceImpl departmentService;

    @Test
    void createDepartment_ShouldThrow_WhenNameExists() {
        DepartmentCreateRequest req = new DepartmentCreateRequest();
        req.setDepartmentName("HR");
        when(departmentRepository.existsByDepartmentName("HR")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.createDepartment(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void updateDepartment_ShouldThrow_WhenConflictingName() {
        Department dept = new Department();
        dept.setDepartmentId("d1");
        dept.setDepartmentName("Finance");
        when(departmentRepository.findById("d1")).thenReturn(Optional.of(dept));

        DepartmentUpdateRequest req = new DepartmentUpdateRequest();
        req.setDepartmentName("HR");
        when(departmentRepository.existsByDepartmentName("HR")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.updateDepartment("d1", req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void deleteDepartment_ShouldThrow_WhenEmployeesExist() {
        when(departmentRepository.existsById("d1")).thenReturn(true);
        when(employeeRepository.countByDepartmentId("d1")).thenReturn(3L);

        assertThatThrownBy(() -> departmentService.deleteDepartment("d1"))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("employee(s)");
        verify(departmentRepository, never()).deleteById(any());
    }

    @Test
    void deleteDepartment_ShouldThrow_WhenNotFound() {
        when(departmentRepository.existsById("missing")).thenReturn(false);
        assertThatThrownBy(() -> departmentService.deleteDepartment("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}


