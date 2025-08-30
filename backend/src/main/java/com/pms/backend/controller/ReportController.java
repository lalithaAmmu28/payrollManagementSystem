package com.pms.backend.controller;

import com.pms.backend.dto.ApiResponse;
import com.pms.backend.dto.report.DepartmentCostDto;
import com.pms.backend.dto.report.LeaveTrendDto;
import com.pms.backend.dto.report.PayrollSummaryDto;
import com.pms.backend.entity.enums.LeaveType;
import com.pms.backend.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for generating reports and analytics
 * All endpoints are secured for Admin access only
 */
@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports & Analytics", description = "Admin-only endpoints for generating reports and analytics")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {
    
    private final ReportService reportService;
    
    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }
    
    // ====== PAYROLL REPORTS ======
    
    @GetMapping("/payroll-summary")
    @Operation(
        summary = "Get payroll summary report",
        description = "Get monthly payroll summaries for specified period. Shows total costs, employee counts, and completion rates."
    )
    public ResponseEntity<ApiResponse<List<PayrollSummaryDto>>> getPayrollSummary(
            @Parameter(description = "Start year for analysis")
            @RequestParam(required = false) Integer startYear,
            @Parameter(description = "End year for analysis")
            @RequestParam(required = false) Integer endYear) {
        
        List<PayrollSummaryDto> summary = reportService.getMonthlyPayrollSummary(startYear, endYear);
        return ResponseEntity.ok(new ApiResponse<>(true, "Payroll summary retrieved successfully", summary));
    }
    
    @GetMapping("/payroll-summary/overall")
    @Operation(
        summary = "Get overall payroll summary",
        description = "Get aggregated payroll summary across all periods for specified year range."
    )
    public ResponseEntity<ApiResponse<PayrollSummaryDto>> getOverallPayrollSummary(
            @Parameter(description = "Start year for analysis")
            @RequestParam(required = false) Integer startYear,
            @Parameter(description = "End year for analysis")
            @RequestParam(required = false) Integer endYear) {
        
        PayrollSummaryDto summary = reportService.getOverallPayrollSummary(startYear, endYear);
        return ResponseEntity.ok(new ApiResponse<>(true, "Overall payroll summary retrieved successfully", summary));
    }
    
    @GetMapping("/department-cost")
    @Operation(
        summary = "Get department cost analysis",
        description = "Get detailed breakdown of payroll costs by department. Supports filtering by year, month, or year range."
    )
    public ResponseEntity<ApiResponse<List<DepartmentCostDto>>> getDepartmentCostReport(
            @Parameter(description = "Specific year for analysis")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Specific month for analysis (1-12)")
            @RequestParam(required = false) Integer month,
            @Parameter(description = "Start year for range analysis")
            @RequestParam(required = false) Integer startYear,
            @Parameter(description = "End year for range analysis")
            @RequestParam(required = false) Integer endYear) {
        
        List<DepartmentCostDto> costs = reportService.getDepartmentCostReport(year, month, startYear, endYear);
        return ResponseEntity.ok(new ApiResponse<>(true, "Department cost report retrieved successfully", costs));
    }
    
    @GetMapping("/departments/top-spending")
    @Operation(
        summary = "Get top spending departments",
        description = "Get departments ranked by total payroll spending for specified year."
    )
    public ResponseEntity<ApiResponse<List<DepartmentCostDto>>> getTopSpendingDepartments(
            @Parameter(description = "Year for analysis (defaults to current year)")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Maximum number of departments to return (defaults to 10)")
            @RequestParam(defaultValue = "10") Integer limit) {
        
        List<DepartmentCostDto> topDepartments = reportService.getTopSpendingDepartments(year, limit);
        return ResponseEntity.ok(new ApiResponse<>(true, "Top spending departments retrieved successfully", topDepartments));
    }
    
    // ====== LEAVE REPORTS ======
    
    @GetMapping("/leave-trends")
    @Operation(
        summary = "Get leave usage trends",
        description = "Get detailed leave statistics grouped by leave type. Shows approval rates, total days, and trends."
    )
    public ResponseEntity<ApiResponse<List<LeaveTrendDto>>> getLeaveTrends(
            @Parameter(description = "Start date for filtering (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for filtering (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Specific year for analysis")
            @RequestParam(required = false) Integer year) {
        
        List<LeaveTrendDto> trends = reportService.getLeaveTrendsReport(startDate, endDate, year);
        return ResponseEntity.ok(new ApiResponse<>(true, "Leave trends retrieved successfully", trends));
    }
    
    @GetMapping("/leave-trends/by-department")
    @Operation(
        summary = "Get leave trends by department",
        description = "Get leave statistics grouped by department and leave type."
    )
    public ResponseEntity<ApiResponse<List<Object[]>>> getLeaveTrendsByDepartment(
            @Parameter(description = "Start date for filtering (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for filtering (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Specific year for analysis")
            @RequestParam(required = false) Integer year) {
        
        List<Object[]> trends = reportService.getLeaveTrendsByDepartment(startDate, endDate, year);
        return ResponseEntity.ok(new ApiResponse<>(true, "Leave trends by department retrieved successfully", trends));
    }
    
    @GetMapping("/leave-trends/monthly")
    @Operation(
        summary = "Get monthly leave statistics",
        description = "Get leave request statistics for each month of the specified year."
    )
    public ResponseEntity<ApiResponse<List<Object[]>>> getMonthlyLeaveStatistics(
            @Parameter(description = "Year for analysis (defaults to current year)")
            @RequestParam(required = false) Integer year) {
        
        if (year == null) {
            year = Year.now().getValue();
        }
        
        List<Object[]> stats = reportService.getMonthlyLeaveStatistics(year);
        return ResponseEntity.ok(new ApiResponse<>(true, "Monthly leave statistics retrieved successfully", stats));
    }
    
    @GetMapping("/employees/top-leave-takers")
    @Operation(
        summary = "Get top leave-taking employees",
        description = "Get employees ranked by total approved leave days taken."
    )
    public ResponseEntity<ApiResponse<List<Object[]>>> getTopLeaveTakingEmployees(
            @Parameter(description = "Year for analysis")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Filter by specific leave type")
            @RequestParam(required = false) LeaveType leaveType,
            @Parameter(description = "Maximum number of employees to return (defaults to 10)")
            @RequestParam(defaultValue = "10") Integer limit) {
        
        List<Object[]> employees = reportService.getTopLeaveTakingEmployees(year, leaveType, limit);
        return ResponseEntity.ok(new ApiResponse<>(true, "Top leave-taking employees retrieved successfully", employees));
    }
    
    @GetMapping("/leave-trends/overall")
    @Operation(
        summary = "Get overall leave statistics",
        description = "Get aggregated leave statistics for specified period."
    )
    public ResponseEntity<ApiResponse<Object[]>> getOverallLeaveStatistics(
            @Parameter(description = "Start date for filtering (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for filtering (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Specific year for analysis")
            @RequestParam(required = false) Integer year) {
        
        Object[] stats = reportService.getOverallLeaveStatistics(startDate, endDate, year);
        return ResponseEntity.ok(new ApiResponse<>(true, "Overall leave statistics retrieved successfully", stats));
    }
    
    // ====== ANALYTICS DASHBOARD ======
    
    @GetMapping("/analytics/dashboard")
    @Operation(
        summary = "Get comprehensive analytics dashboard",
        description = "Get combined analytics data including payroll summaries, department costs, leave trends, and key metrics."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAnalyticsDashboard(
            @Parameter(description = "Year for analysis (defaults to current year)")
            @RequestParam(required = false) Integer year) {
        
        Map<String, Object> dashboard = reportService.getAnalyticsDashboard(year);
        return ResponseEntity.ok(new ApiResponse<>(true, "Analytics dashboard retrieved successfully", dashboard));
    }
    
    // ====== UTILITY ENDPOINTS ======
    
    @GetMapping("/health")
    @Operation(
        summary = "Health check for reporting service",
        description = "Check if reporting service is functioning properly."
    )
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Reporting service is healthy", "OK"));
    }
}