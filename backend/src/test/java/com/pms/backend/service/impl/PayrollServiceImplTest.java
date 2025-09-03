package com.pms.backend.service.impl;

import com.pms.backend.dto.payroll.PayrollRunCreateRequest;
import com.pms.backend.entity.Employee;
import com.pms.backend.entity.PayrollItem;
import com.pms.backend.entity.PayrollRun;
import com.pms.backend.entity.SalaryStructure;
import com.pms.backend.entity.enums.PayrollStatus;
import com.pms.backend.exception.BadRequestException;
import com.pms.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PayrollServiceImplTest {

    @Mock private PayrollRunRepository payrollRunRepository;
    @Mock private PayrollItemRepository payrollItemRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private SalaryStructureRepository salaryStructureRepository;
    @Mock private LeaveRequestRepository leaveRequestRepository;
    @Mock private PayrollCalculationHelper calculationHelper;

    @InjectMocks
    private PayrollServiceImpl payrollService;

    private Employee employee;
    private SalaryStructure salaryStructure;

    @BeforeEach
    void setup() {
        employee = new Employee();
        employee.setEmployeeId("emp-1");
        employee.setFirstName("John");
        employee.setLastName("Doe");

        salaryStructure = new SalaryStructure();
        salaryStructure.setEmployeeId("emp-1");
        salaryStructure.setBaseSalary(new BigDecimal("120000")); // annual
        salaryStructure.setEffectiveFrom(LocalDate.of(2025, 1, 1));

        when(payrollItemRepository.save(any(PayrollItem.class))).thenAnswer(returnsFirstArg());
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(returnsFirstArg());
        when(payrollItemRepository.getPayrollSummaryForRun(anyString())).thenReturn(new Object[]{0L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
    }

    @Test
    void testProcessPayrollRun_SkipsEmployee_WhenNoActiveStructure() {
        // Given
        PayrollRun run = createRun("run-7", 2025, 8, PayrollStatus.Draft);
        when(payrollRunRepository.findById("run-7")).thenReturn(Optional.of(run));
        when(employeeRepository.findAll()).thenReturn(Collections.singletonList(employee));
        when(salaryStructureRepository.findActiveStructureForEmployee(eq("emp-1"), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        // When
        payrollService.processPayrollRun("run-7");

        // Then: no payroll item saved, but run marked processed
        verify(payrollItemRepository, never()).save(any(PayrollItem.class));
        verify(payrollRunRepository).save(argThat(updated -> updated.getStatus() == PayrollStatus.Processed));
    }

    @Test
    void testLockPayrollRun_Success_SetsLockedAndPayDates() {
        // Given
        PayrollRun run = createRun("run-8", 2025, 8, PayrollStatus.Processed);
        when(payrollRunRepository.findById("run-8")).thenReturn(Optional.of(run));
        when(payrollRunRepository.save(any(PayrollRun.class))).thenAnswer(returnsFirstArg());
        when(payrollItemRepository.countByRunId("run-8")).thenReturn(3L);

        // When
        payrollService.lockPayrollRun("run-8");

        // Then
        verify(payrollItemRepository).updatePayDateForRun(eq("run-8"), any(LocalDate.class));
        verify(payrollRunRepository).save(argThat(saved -> saved.getStatus() == PayrollStatus.Locked));
    }

    @Test
    void testGetEmployeePayslips_Failure_WhenEmployeeNotFound() {
        // Given
        when(employeeRepository.existsById("emp-x")).thenReturn(false);

        // When / Then
        assertThrows(com.pms.backend.exception.ResourceNotFoundException.class,
                () -> payrollService.getEmployeePayslips("emp-x"));
    }

    @Test
    void testGetPayrollItemsForRun_Failure_WhenRunMissing() {
        // Given
        when(payrollRunRepository.existsById("run-missing")).thenReturn(false);

        // When / Then
        assertThrows(com.pms.backend.exception.ResourceNotFoundException.class,
                () -> payrollService.getPayrollItemsForRun("run-missing"));
    }

    @Test
    void testGetPayrollItemsForRun_Success_DelegatesToRepo() {
        // Given
        when(payrollRunRepository.existsById("run-10")).thenReturn(true);
        when(payrollItemRepository.findByRunIdWithEmployeeDetails("run-10")).thenReturn(java.util.Collections.emptyList());

        // When
        var result = payrollService.getPayrollItemsForRun("run-10");

        // Then
        assertThat(result).isEmpty();
        verify(payrollItemRepository).findByRunIdWithEmployeeDetails("run-10");
    }

    @Test
    void testGetEmployeePayrollItemForAdmin_Failure_WhenDraft() {
        // Given
        PayrollRun run = createRun("run-11", 2025, 8, PayrollStatus.Draft);
        when(payrollRunRepository.findById("run-11")).thenReturn(Optional.of(run));

        // When / Then
        assertThrows(BadRequestException.class,
                () -> payrollService.getEmployeePayrollItemForAdmin("run-11", "emp-1"));
    }

    @Test
    void testGetEmployeePayslip_Failure_WhenNotLocked() {
        // Given
        PayrollRun run = createRun("run-12", 2025, 8, PayrollStatus.Processed);
        when(payrollRunRepository.findById("run-12")).thenReturn(Optional.of(run));

        // When / Then
        assertThrows(BadRequestException.class,
                () -> payrollService.getEmployeePayslip("run-12", "emp-1"));
    }

    @Test
    void testProcessPayrollRun_SetsProcessedAtTimestamp() {
        // Given
        PayrollRun run = createRun("run-13", 2025, 8, PayrollStatus.Draft);
        when(payrollRunRepository.findById("run-13")).thenReturn(Optional.of(run));
        when(employeeRepository.findAll()).thenReturn(Collections.singletonList(employee));
        when(salaryStructureRepository.findActiveStructureForEmployee(eq("emp-1"), any(LocalDate.class)))
                .thenReturn(Optional.of(salaryStructure));
        when(calculationHelper.calculateBonus(eq(salaryStructure), any(BigDecimal.class)))
                .thenReturn(BigDecimal.ZERO);
        when(calculationHelper.calculateLossOfPayDeduction(eq("emp-1"), any(), any(), any(BigDecimal.class), anyInt()))
                .thenReturn(BigDecimal.ZERO);

        // When
        payrollService.processPayrollRun("run-13");

        // Then
        ArgumentCaptor<PayrollRun> runCaptor = ArgumentCaptor.forClass(PayrollRun.class);
        verify(payrollRunRepository).save(runCaptor.capture());
        assertThat(runCaptor.getValue().getProcessedAt()).isNotNull();
        assertThat(runCaptor.getValue().getStatus()).isEqualTo(PayrollStatus.Processed);
    }

    @Test
    void testGetPayrollRunById_MapsSummary_FromRepository() {
        // Given
        PayrollRun run = createRun("run-14", 2025, 8, PayrollStatus.Processed);
        when(payrollRunRepository.findById("run-14")).thenReturn(Optional.of(run));
        when(payrollItemRepository.getPayrollSummaryForRun("run-14")).thenReturn(new Object[]{
                2L,
                new BigDecimal("20000.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("500.00"),
                new BigDecimal("20500.00")
        });

        // When
        var response = payrollService.getPayrollRunById("run-14");

        // Then
        assertThat(response.getEmployeeCount()).isEqualTo(2L);
        assertThat(response.getTotalBaseSalary()).isEqualByComparingTo("20000.00");
        assertThat(response.getTotalBonus()).isEqualByComparingTo("1000.00");
        assertThat(response.getTotalDeductions()).isEqualByComparingTo("500.00");
        assertThat(response.getTotalNetSalary()).isEqualByComparingTo("20500.00");
    }

    @Test
    void testGetPayrollRunById_MapsSummary_NestedArrayShape() {
        // Given
        PayrollRun run = createRun("run-15", 2025, 8, PayrollStatus.Processed);
        when(payrollRunRepository.findById("run-15")).thenReturn(Optional.of(run));
        Object[] inner = new Object[]{
                3L,
                new BigDecimal("30000"),
                new BigDecimal("1500"),
                new BigDecimal("700"),
                new BigDecimal("30800")
        };
        when(payrollItemRepository.getPayrollSummaryForRun("run-15")).thenReturn(new Object[]{ inner });

        // When
        var response = payrollService.getPayrollRunById("run-15");

        // Then
        assertThat(response.getEmployeeCount()).isEqualTo(3L);
        assertThat(response.getTotalNetSalary()).isEqualByComparingTo("30800");
    }

    @Test
    void testGetAllPayrollRuns_DelegatesAndMaps() {
        // Given
        PayrollRun r1 = createRun("r1", 2025, 8, PayrollStatus.Draft);
        PayrollRun r2 = createRun("r2", 2025, 7, PayrollStatus.Processed);
        when(payrollRunRepository.findAllByOrderByRunYearDescRunMonthDesc()).thenReturn(java.util.List.of(r1, r2));

        // When
        var list = payrollService.getAllPayrollRuns();

        // Then
        assertThat(list).hasSize(2);
        assertThat(list.get(0).getRunId()).isEqualTo("r1");
        assertThat(list.get(1).getRunId()).isEqualTo("r2");
    }

    @Test
    void testPayrollRunExists_Delegates() {
        // Given
        when(payrollRunRepository.existsByRunYearAndRunMonth(2025, 8)).thenReturn(true);

        // When / Then
        assertThat(payrollService.payrollRunExists(2025, 8)).isTrue();
    }

    private PayrollRun createRun(String id, int year, int month, PayrollStatus status) {
        PayrollRun run = new PayrollRun();
        run.setRunId(id);
        run.setRunYear(year);
        run.setRunMonth(month);
        run.setStatus(status);
        return run;
    }

    @Test
    void testProcessPayrollRun_HappyPath_NoBonus_NoLeave() {
        // Given
        PayrollRun run = createRun("run-1", 2025, 8, PayrollStatus.Draft);
        when(payrollRunRepository.findById("run-1")).thenReturn(Optional.of(run));
        when(employeeRepository.findAll()).thenReturn(Collections.singletonList(employee));
        when(salaryStructureRepository.findActiveStructureForEmployee(eq("emp-1"), any(LocalDate.class)))
                .thenReturn(Optional.of(salaryStructure));
        when(calculationHelper.calculateBonus(eq(salaryStructure), any(BigDecimal.class)))
                .thenReturn(BigDecimal.ZERO);
        when(calculationHelper.calculateLossOfPayDeduction(eq("emp-1"), any(), any(), any(BigDecimal.class), anyInt()))
                .thenReturn(BigDecimal.ZERO);

        // When
        payrollService.processPayrollRun("run-1");

        // Then
        ArgumentCaptor<PayrollItem> captor = ArgumentCaptor.forClass(PayrollItem.class);
        verify(payrollItemRepository, atLeastOnce()).save(captor.capture());
        PayrollItem saved = captor.getValue();

        BigDecimal expectedMonthly = new BigDecimal("120000").divide(new BigDecimal("12"), 2, java.math.RoundingMode.HALF_UP);
        assertThat(saved.getBaseSalary()).isEqualByComparingTo(expectedMonthly);
        assertThat(saved.getBonus()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saved.getDeductions()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saved.getNetSalary()).isEqualByComparingTo(expectedMonthly);

        verify(payrollRunRepository).save(argThat(updated -> updated.getStatus() == PayrollStatus.Processed));
    }

    @Test
    void testProcessPayrollRun_PercentageBonus_IsApplied() {
        // Given
        PayrollRun run = createRun("run-2", 2025, 8, PayrollStatus.Draft);
        when(payrollRunRepository.findById("run-2")).thenReturn(Optional.of(run));
        when(employeeRepository.findAll()).thenReturn(Collections.singletonList(employee));
        when(salaryStructureRepository.findActiveStructureForEmployee(eq("emp-1"), any(LocalDate.class)))
                .thenReturn(Optional.of(salaryStructure));

        // 10% of monthly base (120000/12=10000) => 1000
        when(calculationHelper.calculateBonus(eq(salaryStructure), any(BigDecimal.class)))
                .thenReturn(new BigDecimal("1000.00"));
        when(calculationHelper.calculateLossOfPayDeduction(eq("emp-1"), any(), any(), any(BigDecimal.class), anyInt()))
                .thenReturn(BigDecimal.ZERO);

        // When
        payrollService.processPayrollRun("run-2");

        // Then
        ArgumentCaptor<PayrollItem> captor = ArgumentCaptor.forClass(PayrollItem.class);
        verify(payrollItemRepository, atLeastOnce()).save(captor.capture());
        PayrollItem saved = captor.getValue();

        BigDecimal monthly = new BigDecimal("10000.00");
        assertThat(saved.getBonus()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(saved.getNetSalary()).isEqualByComparingTo(monthly.add(new BigDecimal("1000.00")));
    }

    @Test
    void testProcessPayrollRun_LossOfPayDeduction_ForUnpaidLeave() {
        // Given
        PayrollRun run = createRun("run-3", 2025, 8, PayrollStatus.Draft);
        when(payrollRunRepository.findById("run-3")).thenReturn(Optional.of(run));
        when(employeeRepository.findAll()).thenReturn(Collections.singletonList(employee));
        when(salaryStructureRepository.findActiveStructureForEmployee(eq("emp-1"), any(LocalDate.class)))
                .thenReturn(Optional.of(salaryStructure));

        when(calculationHelper.calculateBonus(eq(salaryStructure), any(BigDecimal.class)))
                .thenReturn(BigDecimal.ZERO);
        // 2 days in a 30-day month: monthly base 10000 => 10000/30*2 = 666.67
        when(calculationHelper.calculateLossOfPayDeduction(eq("emp-1"), any(), any(), any(BigDecimal.class), anyInt()))
                .thenReturn(new BigDecimal("666.67"));

        // When
        payrollService.processPayrollRun("run-3");

        // Then
        ArgumentCaptor<PayrollItem> captor = ArgumentCaptor.forClass(PayrollItem.class);
        verify(payrollItemRepository, atLeastOnce()).save(captor.capture());
        PayrollItem saved = captor.getValue();

        BigDecimal monthly = new BigDecimal("10000.00");
        assertThat(saved.getDeductions()).isEqualByComparingTo(new BigDecimal("666.67"));
        assertThat(saved.getNetSalary()).isEqualByComparingTo(monthly.subtract(new BigDecimal("666.67")));
    }

    @Test
    void testProcessPayrollRun_Reprocessing_DeletesExistingItemsFirst() {
        // Given
        PayrollRun run = createRun("run-4", 2025, 8, PayrollStatus.Processed);
        when(payrollRunRepository.findById("run-4")).thenReturn(Optional.of(run));
        when(employeeRepository.findAll()).thenReturn(Collections.singletonList(employee));
        when(salaryStructureRepository.findActiveStructureForEmployee(eq("emp-1"), any(LocalDate.class)))
                .thenReturn(Optional.of(salaryStructure));
        when(calculationHelper.calculateBonus(eq(salaryStructure), any(BigDecimal.class)))
                .thenReturn(BigDecimal.ZERO);
        when(calculationHelper.calculateLossOfPayDeduction(eq("emp-1"), any(), any(), any(BigDecimal.class), anyInt()))
                .thenReturn(BigDecimal.ZERO);

        // When
        payrollService.processPayrollRun("run-4");

        // Then - verify order: deleteByRunId happens before any save
        InOrder inOrder = inOrder(payrollItemRepository);
        inOrder.verify(payrollItemRepository).deleteByRunId("run-4");
        inOrder.verify(payrollItemRepository, atLeastOnce()).save(any(PayrollItem.class));
    }

    @Test
    void testProcessPayrollRun_Failure_WhenRunIsLocked() {
        // Given
        PayrollRun run = createRun("run-5", 2025, 8, PayrollStatus.Locked);
        when(payrollRunRepository.findById("run-5")).thenReturn(Optional.of(run));

        // When / Then
        assertThrows(BadRequestException.class, () -> payrollService.processPayrollRun("run-5"));
    }

    @Test
    void testLockPayrollRun_Failure_WhenRunIsDraft() {
        // Given
        PayrollRun run = createRun("run-6", 2025, 8, PayrollStatus.Draft);
        when(payrollRunRepository.findById("run-6")).thenReturn(Optional.of(run));

        // When / Then
        assertThrows(BadRequestException.class, () -> payrollService.lockPayrollRun("run-6"));
    }

    @Test
    void testCreatePayrollRun_Failure_WhenDuplicate() {
        // Given
        PayrollRunCreateRequest req = new PayrollRunCreateRequest(2025, 8);
        when(payrollRunRepository.existsByRunYearAndRunMonth(2025, 8)).thenReturn(true);

        // When / Then
        assertThrows(BadRequestException.class, () -> payrollService.createPayrollRun(req));
    }
}


