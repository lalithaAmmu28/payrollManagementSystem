package com.pms.backend.service.impl;

import com.pms.backend.dto.leave.LeaveRequestCreateDto;
import com.pms.backend.dto.leave.LeaveStatusUpdateDto;
import com.pms.backend.entity.Employee;
import com.pms.backend.entity.LeaveRequest;
import com.pms.backend.entity.enums.LeaveStatus;
import com.pms.backend.entity.enums.LeaveType;
import com.pms.backend.exception.BadRequestException;
import com.pms.backend.repository.EmployeeRepository;
import com.pms.backend.repository.LeaveRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LeaveRequestServiceImplTest {

    @Mock private LeaveRequestRepository leaveRequestRepository;
    @Mock private EmployeeRepository employeeRepository;

    @InjectMocks
    private LeaveRequestServiceImpl leaveService;

    @Test
    void testApplyForLeave_Failure_WhenPaidDaysExceedBalance() {
        // Given
        String empId = "emp-1";
        Employee emp = new Employee();
        emp.setEmployeeId(empId);
        emp.setLeaveBalance(new BigDecimal("3"));
        when(employeeRepository.findById(empId)).thenReturn(Optional.of(emp));

        LeaveRequestCreateDto req = new LeaveRequestCreateDto(LeaveType.Paid,
                LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 5), "Family");

        // When / Then
        assertThrows(BadRequestException.class, () -> leaveService.applyForLeave(empId, req));
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    void testApplyForLeave_Success_PendingCreated() {
        // Given
        String empId = "emp-2";
        Employee emp = new Employee();
        emp.setEmployeeId(empId);
        emp.setLeaveBalance(new BigDecimal("10"));
        when(employeeRepository.findById(empId)).thenReturn(Optional.of(emp));

        LeaveRequestCreateDto req = new LeaveRequestCreateDto(LeaveType.Paid,
                LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 2), "Trip");

        when(leaveRequestRepository.findOverlappingLeaveRequests(eq(empId), any(), any(), isNull()))
                .thenReturn(java.util.Collections.emptyList());
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        var result = leaveService.applyForLeave(empId, req);

        // Then
        assertThat(result.getStatus()).isEqualTo(LeaveStatus.Pending);
        assertThat(result.getDurationInDays()).isEqualTo(2);
    }

    @Test
    void testUpdateLeaveStatus_Failure_WhenNotPending() {
        // Given
        LeaveRequest lr = new LeaveRequest();
        lr.setLeaveId("lr-3");
        lr.setEmployeeId("emp-3");
        lr.setLeaveType(LeaveType.Paid);
        lr.setStartDate(LocalDate.of(2025, 8, 1));
        lr.setEndDate(LocalDate.of(2025, 8, 2));
        lr.setStatus(LeaveStatus.Approved); // not pending
        when(leaveRequestRepository.findById("lr-3")).thenReturn(Optional.of(lr));

        LeaveStatusUpdateDto update = new LeaveStatusUpdateDto();
        update.setStatus(LeaveStatus.Rejected);

        // When / Then
        assertThrows(BadRequestException.class, () -> leaveService.updateLeaveStatus("lr-3", update));
    }

    @Test
    void testCancelLeaveRequest_Failure_WhenNotPending() {
        // Given
        LeaveRequest lr = new LeaveRequest();
        lr.setLeaveId("lr-4");
        lr.setEmployeeId("emp-4");
        lr.setStatus(LeaveStatus.Approved);
        when(leaveRequestRepository.findById("lr-4")).thenReturn(Optional.of(lr));

        // When / Then
        assertThrows(BadRequestException.class, () -> leaveService.cancelLeaveRequest("lr-4", "emp-4"));
    }

    @Test
    void testUpdateLeaveStatus_ApprovePaidLeave_DeductsBalance() {
        // Given
        String empId = "emp-1";
        Employee emp = new Employee();
        emp.setEmployeeId(empId);
        emp.setLeaveBalance(new BigDecimal("10"));
        when(employeeRepository.findById(empId)).thenReturn(Optional.of(emp));

        LeaveRequest lr = new LeaveRequest();
        lr.setLeaveId("lr-1");
        lr.setEmployeeId(empId);
        lr.setLeaveType(LeaveType.Paid);
        lr.setStartDate(LocalDate.of(2025, 8, 1));
        lr.setEndDate(LocalDate.of(2025, 8, 5));
        lr.setStatus(LeaveStatus.Pending);
        when(leaveRequestRepository.findById("lr-1")).thenReturn(Optional.of(lr));

        LeaveStatusUpdateDto update = new LeaveStatusUpdateDto();
        update.setStatus(LeaveStatus.Approved);

        // When
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        leaveService.updateLeaveStatus("lr-1", update);

        // Then
        ArgumentCaptor<Employee> empCaptor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(empCaptor.capture());
        assertThat(empCaptor.getValue().getLeaveBalance()).isEqualByComparingTo(new BigDecimal("5"));
    }

    @Test
    void testUpdateLeaveStatus_ApproveSickLeave_NoBalanceChange() {
        // Given
        String empId = "emp-1";
        Employee emp = new Employee();
        emp.setEmployeeId(empId);
        emp.setLeaveBalance(new BigDecimal("10"));
        when(employeeRepository.findById(empId)).thenReturn(Optional.of(emp));

        LeaveRequest lr = new LeaveRequest();
        lr.setLeaveId("lr-2");
        lr.setEmployeeId(empId);
        lr.setLeaveType(LeaveType.Sick);
        lr.setStartDate(LocalDate.of(2025, 8, 1));
        lr.setEndDate(LocalDate.of(2025, 8, 1));
        lr.setStatus(LeaveStatus.Pending);
        when(leaveRequestRepository.findById("lr-2")).thenReturn(Optional.of(lr));

        LeaveStatusUpdateDto update = new LeaveStatusUpdateDto();
        update.setStatus(LeaveStatus.Approved);

        // When
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        leaveService.updateLeaveStatus("lr-2", update);

        // Then
        verify(employeeRepository, never()).save(any(Employee.class));
    }
}


