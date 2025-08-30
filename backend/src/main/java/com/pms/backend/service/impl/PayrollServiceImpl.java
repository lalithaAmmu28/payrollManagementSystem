package com.pms.backend.service.impl;

import com.pms.backend.dto.payroll.PayrollItemResponse;
import com.pms.backend.dto.payroll.PayrollRunCreateRequest;
import com.pms.backend.dto.payroll.PayrollRunResponse;
import com.pms.backend.entity.*;
import com.pms.backend.entity.enums.LeaveStatus;
import com.pms.backend.entity.enums.LeaveType;
import com.pms.backend.entity.enums.PayrollStatus;
import com.pms.backend.exception.BadRequestException;
import com.pms.backend.exception.ResourceNotFoundException;
import com.pms.backend.repository.*;
import com.pms.backend.service.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PayrollServiceImpl implements PayrollService {
    
    private final PayrollRunRepository payrollRunRepository;
    private final PayrollItemRepository payrollItemRepository;
    private final EmployeeRepository employeeRepository;
    private final SalaryStructureRepository salaryStructureRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PayrollCalculationHelper calculationHelper;
    
    @Autowired
    public PayrollServiceImpl(PayrollRunRepository payrollRunRepository,
                            PayrollItemRepository payrollItemRepository,
                            EmployeeRepository employeeRepository,
                            SalaryStructureRepository salaryStructureRepository,
                            LeaveRequestRepository leaveRequestRepository,
                            PayrollCalculationHelper calculationHelper) {
        this.payrollRunRepository = payrollRunRepository;
        this.payrollItemRepository = payrollItemRepository;
        this.employeeRepository = employeeRepository;
        this.salaryStructureRepository = salaryStructureRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.calculationHelper = calculationHelper;
    }
    
    @Override
    public PayrollRunResponse createPayrollRun(PayrollRunCreateRequest request) {
        // 1. Check if a run for this period already exists
        if (payrollRunRepository.existsByRunYearAndRunMonth(request.getYear(), request.getMonth())) {
            throw new BadRequestException("A payroll run for this period already exists.");
        }
        
        // 2. Create new payroll run
        PayrollRun payrollRun = new PayrollRun();
        payrollRun.setRunYear(request.getYear());
        payrollRun.setRunMonth(request.getMonth());
        payrollRun.setStatus(PayrollStatus.Draft);
        
        PayrollRun savedRun = payrollRunRepository.save(payrollRun);
        
        System.out.println(String.format("Created payroll run for %s with ID: %s", 
                                       request.getPeriodDescription(), savedRun.getRunId()));
        
        return convertToPayrollRunResponse(savedRun);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PayrollRunResponse> getAllPayrollRuns() {
        List<PayrollRun> runs = payrollRunRepository.findAllByOrderByRunYearDescRunMonthDesc();
        return runs.stream()
                .map(this::convertToPayrollRunResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public PayrollRunResponse getPayrollRunById(String runId) {
        PayrollRun run = payrollRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found with ID: " + runId));
        
        return convertToPayrollRunResponseWithSummary(run);
    }
    
    @Override
    public PayrollRunResponse processPayrollRun(String runId) {
        System.out.println("=== PAYROLL PROCESSING STARTED ===");
        System.out.println("Processing payroll run ID: " + runId);
        
        // 1. Initial Validation
        PayrollRun payrollRun = payrollRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found with ID: " + runId));
        
        if (payrollRun.isLocked()) {
            throw new BadRequestException("Locked payrolls cannot be processed.");
        }
        
        // 2. Handle Re-processing
        if (payrollRun.isProcessed()) {
            System.out.println("Re-processing detected. Deleting existing payroll items...");
            payrollItemRepository.deleteByRunId(runId);
            System.out.println("Existing payroll items deleted successfully.");
        }
        
        // 3. Main Calculation Loop
        YearMonth payrollMonth = YearMonth.of(payrollRun.getRunYear(), payrollRun.getRunMonth());
        LocalDate startDate = payrollMonth.atDay(1);
        LocalDate endDate = payrollMonth.atEndOfMonth();
        int daysInMonth = payrollMonth.lengthOfMonth();
        
        System.out.println(String.format("Payroll period: %s to %s (%d days)", 
                                       startDate, endDate, daysInMonth));
        
        List<Employee> activeEmployees = employeeRepository.findAll();
        System.out.println(String.format("Processing payroll for %d employees", activeEmployees.size()));
        
        int processedCount = 0;
        int skippedCount = 0;
        
        for (Employee employee : activeEmployees) {
            try {
                processEmployeePayroll(employee, payrollRun, startDate, endDate, daysInMonth);
                processedCount++;
            } catch (Exception e) {
                System.err.println(String.format("Error processing employee %s (%s): %s", 
                                                employee.getEmployeeId(), 
                                                employee.getFirstName() + " " + employee.getLastName(), 
                                                e.getMessage()));
                skippedCount++;
            }
        }
        
        // 4. Finalize Processing
        payrollRun.setStatus(PayrollStatus.Processed);
        payrollRun.setProcessedAt(LocalDateTime.now());
        PayrollRun updatedRun = payrollRunRepository.save(payrollRun);
        
        System.out.println("=== PAYROLL PROCESSING COMPLETED ===");
        System.out.println(String.format("Processed: %d employees, Skipped: %d employees", 
                                       processedCount, skippedCount));
        
        return convertToPayrollRunResponseWithSummary(updatedRun);
    }
    
    /**
     * CORE PAYROLL CALCULATION LOGIC FOR INDIVIDUAL EMPLOYEE
     */
    private void processEmployeePayroll(Employee employee, PayrollRun payrollRun, 
                                      LocalDate startDate, LocalDate endDate, int daysInMonth) {
        
        String employeeId = employee.getEmployeeId();
        String employeeName = employee.getFirstName() + " " + employee.getLastName();
        
        System.out.println(String.format("Processing: %s (%s)", employeeName, employeeId));
        
        // i. Get Salary Structure
        Optional<SalaryStructure> salaryStructureOpt = salaryStructureRepository
                .findActiveStructureForEmployee(employeeId, startDate);
        
        if (salaryStructureOpt.isEmpty()) {
            System.out.println(String.format("WARNING: No active salary structure found for employee %s. Skipping.", employeeId));
            throw new RuntimeException("No active salary structure found");
        }
        
        SalaryStructure salaryStructure = salaryStructureOpt.get();
        BigDecimal baseSalary = salaryStructure.getBaseSalary();
        
        System.out.println(String.format("  Base Salary: %s", baseSalary));
        
        // ii. Calculate Bonus
        BigDecimal bonus = calculationHelper.calculateBonus(salaryStructure, baseSalary);
        System.out.println(String.format("  Bonus: %s", bonus));
        
        // iii. Calculate Loss of Pay Deduction
        BigDecimal lossOfPayDeduction = calculationHelper.calculateLossOfPayDeduction(
                employeeId, startDate, endDate, baseSalary, daysInMonth);
        System.out.println(String.format("  Loss of Pay Deduction: %s", lossOfPayDeduction));
        
        // iv. Compute Net Salary
        BigDecimal grossSalary = baseSalary.add(bonus);
        BigDecimal netSalary = grossSalary.subtract(lossOfPayDeduction);
        
        System.out.println(String.format("  Gross Salary: %s, Net Salary: %s", grossSalary, netSalary));
        
        // v. Persist Result
        PayrollItem payrollItem = new PayrollItem();
        payrollItem.setRunId(payrollRun.getRunId());
        payrollItem.setEmployeeId(employeeId);
        payrollItem.setBaseSalary(baseSalary);
        payrollItem.setBonus(bonus);
        payrollItem.setDeductions(lossOfPayDeduction);
        payrollItem.setNetSalary(netSalary);
        // Note: payDate will be set when the run is locked
        
        payrollItemRepository.save(payrollItem);
        
        System.out.println(String.format("  ✓ Payroll item created for %s", employeeName));
    }
    
    @Override
    public PayrollRunResponse lockPayrollRun(String runId) {
        System.out.println("=== PAYROLL LOCKING STARTED ===");
        System.out.println("Locking payroll run ID: " + runId);
        
        // 1. Find and validate payroll run
        PayrollRun payrollRun = payrollRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found with ID: " + runId));
        
        if (!payrollRun.isProcessed()) {
            throw new BadRequestException("Only processed payrolls can be locked.");
        }
        
        // 2. Update run status
        payrollRun.setStatus(PayrollStatus.Locked);
        payrollRun.setLockedAt(LocalDateTime.now());
        
        // 3. Set pay date for all items
        LocalDate payDate = LocalDate.now(); // Could be configurable
        payrollItemRepository.updatePayDateForRun(runId, payDate);
        
        PayrollRun lockedRun = payrollRunRepository.save(payrollRun);
        
        long itemCount = payrollItemRepository.countByRunId(runId);
        System.out.println(String.format("Payroll run locked successfully. Pay date set for %d items: %s", 
                                       itemCount, payDate));
        System.out.println("=== PAYROLL LOCKING COMPLETED ===");
        
        return convertToPayrollRunResponseWithSummary(lockedRun);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PayrollItemResponse> getPayrollItemsForRun(String runId) {
        if (!payrollRunRepository.existsById(runId)) {
            throw new ResourceNotFoundException("Payroll run not found with ID: " + runId);
        }
        
        List<PayrollItem> items = payrollItemRepository.findByRunIdWithEmployeeDetails(runId);
        return items.stream()
                .map(this::convertToPayrollItemResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public PayrollItemResponse getEmployeePayrollItemForAdmin(String runId, String employeeId) {
        // Validate that the run exists and is at least processed
        PayrollRun payrollRun = payrollRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found with ID: " + runId));
        
        if (payrollRun.isDraft()) {
            throw new BadRequestException("Payroll items not yet available. Payroll run must be processed first.");
        }
        
        // Find the payroll item for the employee
        PayrollItem item = payrollItemRepository.findByRunIdAndEmployeeId(runId, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Payroll item not found for employee " + employeeId + " in run " + runId));
        
        return convertToPayrollItemResponse(item);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PayrollItemResponse getEmployeePayslip(String runId, String employeeId) {
        // Validate that the run is locked
        PayrollRun payrollRun = payrollRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found with ID: " + runId));
        
        if (!payrollRun.isLocked()) {
            throw new BadRequestException("Payslip not yet available. Payroll run must be locked first.");
        }
        
        PayrollItem item = payrollItemRepository.findByRunIdAndEmployeeId(runId, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Payslip not found for employee " + employeeId + " in run " + runId));
        
        return convertToPayrollItemResponse(item);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PayrollItemResponse> getEmployeePayslips(String employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }
        
        List<PayrollItem> items = payrollItemRepository.findLockedPayrollItemsForEmployee(employeeId);
        return items.stream()
                .map(this::convertToPayrollItemResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean payrollRunExists(Integer year, Integer month) {
        return payrollRunRepository.existsByRunYearAndRunMonth(year, month);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PayrollRunResponse getPayrollStatistics(String runId) {
        return getPayrollRunById(runId);
    }
    
    /**
     * Convert PayrollRun entity to PayrollRunResponse DTO
     */
    private PayrollRunResponse convertToPayrollRunResponse(PayrollRun run) {
        PayrollRunResponse response = new PayrollRunResponse();
        
        response.setRunId(run.getRunId());
        response.setRunYear(run.getRunYear());
        response.setRunMonth(run.getRunMonth());
        response.setStatus(run.getStatus());
        response.setCreatedAt(run.getCreatedAt());
        response.setUpdatedAt(run.getUpdatedAt());
        response.setProcessedAt(run.getProcessedAt());
        response.setLockedAt(run.getLockedAt());
        
        return response;
    }
    
    /**
     * Convert PayrollRun entity to PayrollRunResponse DTO with summary statistics
     */
    private PayrollRunResponse convertToPayrollRunResponseWithSummary(PayrollRun run) {
        PayrollRunResponse response = convertToPayrollRunResponse(run);
        
        // Add summary statistics if the run has been processed
        if (run.isProcessed() || run.isLocked()) {
            Object[] summary = payrollItemRepository.getPayrollSummaryForRun(run.getRunId());
            
            // Handle nested array result from JPA query
            if (summary != null && summary.length > 0 && summary[0] instanceof Object[]) {
                summary = (Object[]) summary[0];
            }
            
            if (summary != null && summary.length >= 5) {
                response.setEmployeeCount(((Number) summary[0]).longValue());
                response.setTotalBaseSalary((BigDecimal) summary[1]);
                response.setTotalBonus((BigDecimal) summary[2]);
                response.setTotalDeductions((BigDecimal) summary[3]);
                response.setTotalNetSalary((BigDecimal) summary[4]);
            }
        }
        
        return response;
    }
    
    /**
     * Convert PayrollItem entity to PayrollItemResponse DTO
     */
    private PayrollItemResponse convertToPayrollItemResponse(PayrollItem item) {
        PayrollItemResponse response = new PayrollItemResponse();
        
        response.setItemId(item.getItemId());
        response.setRunId(item.getRunId());
        response.setEmployeeId(item.getEmployeeId());
        response.setBaseSalary(item.getBaseSalary());
        response.setBonus(item.getBonus());
        response.setDeductions(item.getDeductions());
        response.setNetSalary(item.getNetSalary());
        response.setPayDate(item.getPayDate());
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());
        
        // Add employee information if available
        if (item.getEmployee() != null) {
            Employee employee = item.getEmployee();
            response.setEmployeeName(employee.getFirstName() + " " + employee.getLastName());
            
            if (employee.getUser() != null) {
                response.setEmployeeEmail(employee.getUser().getEmail());
            }
            
            if (employee.getDepartment() != null) {
                response.setDepartmentName(employee.getDepartment().getDepartmentName());
            }
            
            if (employee.getJobRole() != null) {
                response.setJobTitle(employee.getJobRole().getJobTitle());
            }
        }
        
        // Add payroll run information if available
        if (item.getPayrollRun() != null) {
            PayrollRun run = item.getPayrollRun();
            response.setRunYear(run.getRunYear());
            response.setRunMonth(run.getRunMonth());
            response.setRunStatus(run.getStatus().name());
        }
        
        return response;
    }
}