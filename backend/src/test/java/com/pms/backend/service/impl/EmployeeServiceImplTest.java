package com.pms.backend.service.impl;

import com.pms.backend.dto.employee.EmployeeCreateRequest;
import com.pms.backend.entity.Employee;
import com.pms.backend.entity.User;
import com.pms.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private UserRepository userRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private JobRoleRepository jobRoleRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Test
    void testCreateEmployee_Success_SavesUserAndEmployee() {
        // Given
        EmployeeCreateRequest req = new EmployeeCreateRequest();
        req.setUsername("jdoe");
        req.setEmail("jdoe@example.com");
        req.setPassword("secret");
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setDateOfBirth(LocalDate.of(1990, 1, 1));
        req.setPhone("9999999999");
        req.setAddress("Somewhere");
        req.setJobId("job-1");
        req.setDepartmentId("dept-1");

        when(userRepository.existsByUsername("jdoe")).thenReturn(false);
        when(userRepository.existsByEmail("jdoe@example.com")).thenReturn(false);
        when(departmentRepository.existsById("dept-1")).thenReturn(true);
        when(jobRoleRepository.existsById("job-1")).thenReturn(true);

        when(passwordEncoder.encode("secret")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setUserId("user-1");
            return u;
        });

        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        employeeService.createEmployee(req);

        // Then
        verify(passwordEncoder, times(1)).encode("secret");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("ENCODED");

        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_Failure_WhenUsernameExists() {
        // Given
        EmployeeCreateRequest req = new EmployeeCreateRequest();
        req.setUsername("dup");
        req.setEmail("dup@example.com");
        when(userRepository.existsByUsername("dup")).thenReturn(true);

        // When / Then
        org.junit.jupiter.api.Assertions.assertThrows(
                com.pms.backend.exception.BadRequestException.class,
                () -> employeeService.createEmployee(req)
        );
        verify(userRepository, never()).save(any());
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void testCreateEmployee_Failure_WhenDepartmentMissing() {
        // Given
        EmployeeCreateRequest req = new EmployeeCreateRequest();
        req.setUsername("ok");
        req.setEmail("ok@example.com");
        req.setPassword("secret");
        req.setFirstName("F");
        req.setLastName("L");
        req.setDateOfBirth(LocalDate.of(1990,1,1));
        req.setJobId("job-1");
        req.setDepartmentId("dept-x");

        when(userRepository.existsByUsername("ok")).thenReturn(false);
        when(userRepository.existsByEmail("ok@example.com")).thenReturn(false);
        when(departmentRepository.existsById("dept-x")).thenReturn(false);

        // When / Then
        org.junit.jupiter.api.Assertions.assertThrows(
                com.pms.backend.exception.BadRequestException.class,
                () -> employeeService.createEmployee(req)
        );
    }
}


