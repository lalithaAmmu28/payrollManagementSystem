import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Form, Alert, Badge } from 'react-bootstrap';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, LineChart, Line, PieChart, Pie, Cell } from 'recharts';
import { reportsAPI } from '../../api/apiService';
import { useToast } from '../../components/common/ToastNotification';
import { LoadingSpinner, SkeletonLoader } from '../../components/common';
import './styles/ReportsPage.css';

const ReportsPage = () => {
  const { showToast } = useToast();
  const [loading, setLoading] = useState(true);
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
  const [selectedMonth, setSelectedMonth] = useState('');
  
  // Data states
  const [dashboardData, setDashboardData] = useState(null);
  const [departmentCosts, setDepartmentCosts] = useState([]);
  const [topSpendingDepartments, setTopSpendingDepartments] = useState([]);
  const [monthlyLeaveStats, setMonthlyLeaveStats] = useState([]);
  const [topLeaveTakers, setTopLeaveTakers] = useState([]);
  const [payrollSummary, setPayrollSummary] = useState([]);

  useEffect(() => {
    fetchAllReportsData();
  }, [selectedYear, selectedMonth]);

  const fetchAllReportsData = async () => {
    try {
      setLoading(true);
      
      // 1. Fetch analytics dashboard (requires year parameter)
      const dashboardResponse = await reportsAPI.getAnalyticsDashboard({ year: selectedYear });
      console.log('Dashboard Response:', dashboardResponse.data);
      setDashboardData(dashboardResponse.data.data);
      
      // 2. Fetch department costs (supports year and month)
      const departmentParams = { year: selectedYear };
      if (selectedMonth) {
        departmentParams.month = parseInt(selectedMonth);
      }
      const departmentResponse = await reportsAPI.getDepartmentCosts(departmentParams);
      console.log('Department Costs Response:', departmentResponse.data);
      console.log('Department Costs Data:', departmentResponse.data.data);
      setDepartmentCosts(departmentResponse.data.data || []);
      
      // 3. Fetch top spending departments (requires year parameter)
      const topSpendingResponse = await reportsAPI.getTopSpendingDepartments({ 
        year: selectedYear, 
        limit: 10 
      });
      console.log('Top Spending Response:', topSpendingResponse.data);
      console.log('Top Spending Data:', topSpendingResponse.data.data);
      setTopSpendingDepartments(topSpendingResponse.data.data || []);
      
      // 4. Fetch monthly leave statistics (requires year parameter)
      const monthlyLeaveResponse = await reportsAPI.getMonthlyLeaveStats({ year: selectedYear });
      console.log('Monthly Leave Response:', monthlyLeaveResponse.data);
      setMonthlyLeaveStats(monthlyLeaveResponse.data.data || []);
      
      // 5. Fetch top leave takers (requires year parameter)
      const topLeaveResponse = await reportsAPI.getTopLeaveTakers({ 
        year: selectedYear, 
        limit: 10 
      });
      console.log('Top Leave Takers Response:', topLeaveResponse.data);
      setTopLeaveTakers(topLeaveResponse.data.data || []);
      
      // 6. Fetch payroll summary (requires startYear and endYear)
      const payrollResponse = await reportsAPI.getPayrollSummary({ 
        startYear: selectedYear, 
        endYear: selectedYear 
      });
      console.log('Payroll Summary Response:', payrollResponse.data);
      setPayrollSummary(payrollResponse.data.data || []);
      
    } catch (error) {
      showToast('Failed to fetch reports data', 'error');
      console.error('Fetch reports error:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    if (amount === null || amount === undefined || isNaN(amount)) {
      return '₹0';
    }
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(Number(amount));
  };

  const COLORS = ['#4C51BF', '#38A169', '#E53E3E', '#D69E2E', '#805AD5', '#DD6B20'];

  const renderDashboardCards = () => {
    if (loading || !dashboardData) {
      return (
        <Row className="mb-4">
          {[...Array(3)].map((_, index) => (
            <Col key={index} lg={4} md={6} className="mb-3">
              <Card className="stats-card">
                <Card.Body>
                  <SkeletonLoader height="80px" />
                </Card.Body>
              </Card>
            </Col>
          ))}
        </Row>
      );
    }

    const summaryMetrics = dashboardData.summaryMetrics || {};
    const overallLeave = dashboardData.overallLeaveStatistics || [];
    
    const stats = [
      {
        title: 'Total Employees',
        value: summaryMetrics.totalEmployeesInPayroll || 0,
        icon: 'fas fa-users',
        color: 'primary'
      },
      {
        title: 'Total Leave Requests',
        value: overallLeave[0] || 0, // Total leave requests
        icon: 'fas fa-calendar-alt',
        color: 'warning'
      },
      {
        title: 'Total Departments',
        value: summaryMetrics.totalDepartments || 0,
        icon: 'fas fa-building',
        color: 'info'
      }
    ];

    return (
      <Row className="mb-4">
        {stats.map((stat, index) => (
          <Col key={index} lg={4} md={6} className="mb-3">
            <Card className={`stats-card stats-card-${stat.color}`}>
              <Card.Body>
                <div className="stats-content">
                  <div className="stats-icon">
                    <i className={stat.icon}></i>
                  </div>
                  <div className="stats-info">
                    <h3 className="stats-value">{stat.value}</h3>
                    <p className="stats-title">{stat.title}</p>
                  </div>
                </div>
              </Card.Body>
            </Card>
          </Col>
        ))}
      </Row>
    );
  };

  const renderDepartmentCostChart = () => {
    if (loading) {
      return <SkeletonLoader height="300px" />;
    }

    if (!departmentCosts.length) {
      return (
        <div className="no-data-chart">
          <i className="fas fa-chart-bar fa-3x text-muted mb-3"></i>
          <p className="text-muted">No department cost data available for {selectedYear}</p>
        </div>
      );
    }

    // Transform the data to calculate total cost for each department
    const chartData = departmentCosts.map(dept => ({
      ...dept,
      totalCost: Number(dept.totalNetSalary || 0) // Use totalNetSalary as it's the final amount paid
    }));

    console.log('Transformed chart data:', chartData);

    return (
      <ResponsiveContainer width="100%" height={300}>
        <BarChart data={chartData}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="departmentName" />
          <YAxis tickFormatter={(value) => formatCurrency(value)} />
          <Tooltip 
            formatter={(value) => [formatCurrency(value), 'Total Cost']}
            labelStyle={{ color: '#333' }}
          />
          <Legend />
          <Bar dataKey="totalCost" fill="#4C51BF" name="Department Cost" />
        </BarChart>
      </ResponsiveContainer>
    );
  };

  const renderMonthlyLeaveChart = () => {
    if (loading) {
      return <SkeletonLoader height="300px" />;
    }

    if (!monthlyLeaveStats.length) {
      return (
        <div className="no-data-chart">
          <i className="fas fa-chart-line fa-3x text-muted mb-3"></i>
          <p className="text-muted">No leave usage data available for {selectedYear}</p>
        </div>
      );
    }

    // Transform the monthly leave statistics data for the chart
    const chartData = monthlyLeaveStats.map((item, index) => {
      const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 
                         'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
      return {
        month: monthNames[index] || `Month ${index + 1}`,
        totalRequests: item[0] || 0, // Total requests in that month
        approved: item[1] || 0, // Approved requests
        pending: item[2] || 0, // Pending requests
        rejected: item[3] || 0 // Rejected requests
      };
    });

    return (
      <ResponsiveContainer width="100%" height={300}>
        <LineChart data={chartData}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="month" />
          <YAxis />
          <Tooltip />
          <Legend />
          <Line type="monotone" dataKey="totalRequests" stroke="#4C51BF" strokeWidth={2} name="Total Requests" />
          <Line type="monotone" dataKey="approved" stroke="#38A169" strokeWidth={2} name="Approved" />
          <Line type="monotone" dataKey="pending" stroke="#D69E2E" strokeWidth={2} name="Pending" />
          <Line type="monotone" dataKey="rejected" stroke="#E53E3E" strokeWidth={2} name="Rejected" />
        </LineChart>
      </ResponsiveContainer>
    );
  };

  const renderPayrollSummaryChart = () => {
    if (loading) {
      return <SkeletonLoader height="300px" />;
    }

    console.log('Payroll Summary data for chart:', payrollSummary);

    if (!payrollSummary.length) {
      return (
        <div className="no-data-chart">
          <i className="fas fa-chart-pie fa-3x text-muted mb-3"></i>
          <p className="text-muted">No payroll summary data available for {selectedYear}</p>
        </div>
      );
    }

    // Use the first payroll summary item for the pie chart
    const summaryData = payrollSummary[0] || {};
    console.log('Summary data for chart:', summaryData);
    
    const chartData = [
      { name: 'Base Salary', value: Number(summaryData.totalBaseSalary || 0) },
      { name: 'Bonuses', value: Number(summaryData.totalBonus || 0) },
      { name: 'Deductions', value: Number(summaryData.totalDeductions || 0) }
    ].filter(item => item.value > 0);

    console.log('Chart data after transformation:', chartData);

    if (chartData.length === 0) {
      return (
        <div className="no-data-chart">
          <i className="fas fa-chart-pie fa-3x text-muted mb-3"></i>
          <p className="text-muted">No payroll data available for {selectedYear}</p>
        </div>
      );
    }

    return (
      <ResponsiveContainer width="100%" height={300}>
        <PieChart>
          <Pie
            data={chartData}
            cx="50%"
            cy="50%"
            labelLine={false}
            label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
            outerRadius={80}
            fill="#8884d8"
            dataKey="value"
          >
            {chartData.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
            ))}
          </Pie>
          <Tooltip formatter={(value) => [formatCurrency(value), 'Amount']} />
        </PieChart>
      </ResponsiveContainer>
    );
  };

  const renderTopSpendingDepartments = () => {
    if (loading || !topSpendingDepartments.length) {
      return <SkeletonLoader height="150px" />;
    }

    return (
      <div className="top-departments">
        {topSpendingDepartments.slice(0, 5).map((dept, index) => {
          // Use totalNetSalary as the total cost (matches backend sorting logic)
          const totalCost = Number(dept.totalNetSalary || 0);
          console.log(`Dept ${dept.departmentName}: netSalary=${dept.totalNetSalary}, total=${totalCost}`);
          
          return (
            <div key={index} className="department-item d-flex justify-content-between align-items-center mb-2 p-2 bg-light rounded">
              <div>
                <strong>{dept.departmentName}</strong>
                <div className="text-muted small">{dept.employeeCount} employees</div>
              </div>
              <div className="text-end">
                <div className="h6 mb-0">{formatCurrency(totalCost)}</div>
              </div>
            </div>
          );
        })}
      </div>
    );
  };

  return (
    <div className="reports-page">
      <Container fluid>
        <div className="page-header">
          <div className="d-flex justify-content-between align-items-center mb-4 header-flex-container">
            <div className="flex-grow-1">
              <h1 className="page-title">Reports & Analytics</h1>
              <p className="page-subtitle text-muted">
                Comprehensive insights into your organization's performance for {selectedYear}
              </p>
            </div>
            <div className="controls-section d-flex align-items-center flex-shrink-0">
              <Form.Group className="me-3 mb-0">
                <Form.Label className="small text-muted mb-1">Select Year</Form.Label>
                <Form.Select 
                  value={selectedYear} 
                  onChange={(e) => setSelectedYear(parseInt(e.target.value))}
                  style={{ width: '120px' }}
                >
                  {Array.from({ length: 5 }, (_, i) => {
                    const year = new Date().getFullYear() - i;
                    return (
                      <option key={year} value={year}>
                        {year}
                      </option>
                    );
                  })}
                </Form.Select>
              </Form.Group>
              <Form.Group className="me-3 mb-0">
                <Form.Label className="small text-muted mb-1">Select Month (Optional)</Form.Label>
                <Form.Select 
                  value={selectedMonth} 
                  onChange={(e) => setSelectedMonth(e.target.value)}
                  style={{ width: '140px' }}
                >
                  <option value="">All Months</option>
                  {Array.from({ length: 12 }, (_, i) => {
                    const monthNames = ['January', 'February', 'March', 'April', 'May', 'June',
                                      'July', 'August', 'September', 'October', 'November', 'December'];
                    return (
                      <option key={i + 1} value={i + 1}>
                        {monthNames[i]}
                      </option>
                    );
                  })}
                </Form.Select>
              </Form.Group>
            </div>
          </div>
        </div>

        {/* Dashboard Cards */}
        {renderDashboardCards()}

        {/* Charts Section */}
        <Row>
          {/* Department Costs */}
          <Col lg={6} className="mb-4">
            <Card className="chart-card">
              <Card.Header>
                <h5 className="chart-title">
                  <i className="fas fa-building me-2"></i>
                  Department Costs
                </h5>
                <p className="chart-subtitle text-muted">
                  Total payroll costs by department {selectedMonth && `for month ${selectedMonth}`}
                </p>
              </Card.Header>
              <Card.Body>
                {renderDepartmentCostChart()}
              </Card.Body>
            </Card>
          </Col>

          {/* Monthly Leave Statistics */}
          <Col lg={6} className="mb-4">
            <Card className="chart-card">
              <Card.Header>
                <h5 className="chart-title">
                  <i className="fas fa-chart-line me-2"></i>
                  Monthly Leave Statistics
                </h5>
                <p className="chart-subtitle text-muted">
                  Leave request trends throughout {selectedYear}
                </p>
              </Card.Header>
              <Card.Body>
                {renderMonthlyLeaveChart()}
              </Card.Body>
            </Card>
          </Col>
        </Row>

        <Row>
          {/* Payroll Summary */}
          <Col lg={8} className="mb-4">
            <Card className="chart-card">
              <Card.Header>
                <h5 className="chart-title">
                  <i className="fas fa-chart-pie me-2"></i>
                  Payroll Distribution
                </h5>
                <p className="chart-subtitle text-muted">
                  Payroll breakdown for {selectedYear}
                </p>
              </Card.Header>
              <Card.Body>
                {renderPayrollSummaryChart()}
              </Card.Body>
            </Card>
          </Col>

          {/* Top Spending Departments */}
          <Col lg={4} className="mb-4">
            <Card className="chart-card">
              <Card.Header>
                <h5 className="chart-title">
                  <i className="fas fa-trophy me-2"></i>
                  Top Spending Departments
                </h5>
                <p className="chart-subtitle text-muted">
                  Highest payroll costs for {selectedYear}
                </p>
              </Card.Header>
              <Card.Body>
                {renderTopSpendingDepartments()}
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
    </div>
  );
};

export default ReportsPage;
