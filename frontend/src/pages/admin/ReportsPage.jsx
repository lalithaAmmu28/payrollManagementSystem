import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Form, Alert, Badge } from 'react-bootstrap';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, LineChart, Line, PieChart, Pie, Cell } from 'recharts';
import apiService from '../../api/apiService';
import { useToast } from '../../components/common/ToastNotification';
import { LoadingSpinner, SkeletonLoader } from '../../components/common';
import './styles/ReportsPage.css';

const ReportsPage = () => {
  const { showToast } = useToast();
  const [loading, setLoading] = useState(true);
  const [dateRange, setDateRange] = useState({
    startDate: new Date(new Date().getFullYear(), 0, 1).toISOString().split('T')[0], // Start of current year
    endDate: new Date().toISOString().split('T')[0] // Today
  });
  
  // Dashboard data states
  const [dashboardData, setDashboardData] = useState(null);
  const [departmentCosts, setDepartmentCosts] = useState([]);
  const [leaveUsageTrends, setLeaveUsageTrends] = useState([]);
  const [payrollSummary, setPayrollSummary] = useState([]);
  const [overallStats, setOverallStats] = useState(null);

  useEffect(() => {
    fetchReportsData();
  }, [dateRange]);

  const fetchReportsData = async () => {
    try {
      setLoading(true);
      
      // Fetch all reports data in parallel
      const [
        dashboardResponse,
        departmentCostResponse,
        leaveTrendsResponse,
        payrollSummaryResponse,
        overallStatsResponse
      ] = await Promise.all([
        apiService.get('/reports/analytics/dashboard'),
        apiService.get('/reports/department-cost'),
        apiService.get('/reports/leave-trends'),
        apiService.get('/reports/payroll-summary'),
        apiService.get('/reports/leave-trends/overall')
      ]);

      setDashboardData(dashboardResponse.data.data);
      setDepartmentCosts(departmentCostResponse.data.data);
      setLeaveUsageTrends(leaveTrendsResponse.data.data);
      setPayrollSummary(payrollSummaryResponse.data.data);
      setOverallStats(overallStatsResponse.data.data);
      
    } catch (error) {
      showToast('Failed to fetch reports data', 'error');
      console.error('Fetch reports error:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(amount);
  };

  const COLORS = ['#4C51BF', '#38A169', '#E53E3E', '#D69E2E', '#805AD5', '#DD6B20'];

  const renderDashboardCards = () => {
    if (loading || !dashboardData) {
      return (
        <Row className="mb-4">
          {[...Array(4)].map((_, index) => (
            <Col key={index} lg={3} md={6} className="mb-3">
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

    const stats = [
      {
        title: 'Total Employees',
        value: dashboardData.totalEmployees || 0,
        icon: 'fas fa-users',
        color: 'primary',
        change: '+5%'
      },
      {
        title: 'Active Leave Requests',
        value: dashboardData.activeLeaveRequests || 0,
        icon: 'fas fa-calendar-alt',
        color: 'warning',
        change: '+12%'
      },
      {
        title: 'Monthly Payroll',
        value: formatCurrency(dashboardData.monthlyPayroll || 0),
        icon: 'fas fa-dollar-sign',
        color: 'success',
        change: '+8%'
      },
      {
        title: 'Departments',
        value: dashboardData.totalDepartments || 0,
        icon: 'fas fa-building',
        color: 'info',
        change: '0%'
      }
    ];

    return (
      <Row className="mb-4">
        {stats.map((stat, index) => (
          <Col key={index} lg={3} md={6} className="mb-3">
            <Card className={`stats-card stats-card-${stat.color}`}>
              <Card.Body>
                <div className="stats-content">
                  <div className="stats-icon">
                    <i className={stat.icon}></i>
                  </div>
                  <div className="stats-info">
                    <h3 className="stats-value">{stat.value}</h3>
                    <p className="stats-title">{stat.title}</p>
                    <Badge bg="light" text="dark" className="stats-change">
                      {stat.change}
                    </Badge>
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
          <p className="text-muted">No department cost data available</p>
        </div>
      );
    }

    return (
      <ResponsiveContainer width="100%" height={300}>
        <BarChart data={departmentCosts}>
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

  const renderLeaveUsageChart = () => {
    if (loading) {
      return <SkeletonLoader height="300px" />;
    }

    if (!leaveUsageTrends.length) {
      return (
        <div className="no-data-chart">
          <i className="fas fa-chart-line fa-3x text-muted mb-3"></i>
          <p className="text-muted">No leave usage data available</p>
        </div>
      );
    }

    return (
      <ResponsiveContainer width="100%" height={300}>
        <LineChart data={leaveUsageTrends}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="month" />
          <YAxis />
          <Tooltip />
          <Legend />
          <Line type="monotone" dataKey="totalLeaves" stroke="#38A169" strokeWidth={2} name="Total Leaves" />
          <Line type="monotone" dataKey="approvedLeaves" stroke="#4C51BF" strokeWidth={2} name="Approved" />
          <Line type="monotone" dataKey="rejectedLeaves" stroke="#E53E3E" strokeWidth={2} name="Rejected" />
        </LineChart>
      </ResponsiveContainer>
    );
  };

  const renderPayrollSummaryChart = () => {
    if (loading) {
      return <SkeletonLoader height="300px" />;
    }

    if (!payrollSummary.length) {
      return (
        <div className="no-data-chart">
          <i className="fas fa-chart-pie fa-3x text-muted mb-3"></i>
          <p className="text-muted">No payroll summary data available</p>
        </div>
      );
    }

    return (
      <ResponsiveContainer width="100%" height={300}>
        <PieChart>
          <Pie
            data={payrollSummary}
            cx="50%"
            cy="50%"
            labelLine={false}
            label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
            outerRadius={80}
            fill="#8884d8"
            dataKey="totalAmount"
          >
            {payrollSummary.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
            ))}
          </Pie>
          <Tooltip formatter={(value) => [formatCurrency(value), 'Amount']} />
        </PieChart>
      </ResponsiveContainer>
    );
  };

  const renderQuickStats = () => {
    if (loading || !overallStats) {
      return <SkeletonLoader height="150px" />;
    }

    return (
      <div className="quick-stats">
        <div className="row">
          <div className="col-md-4">
            <div className="stat-item">
              <div className="stat-icon text-success">
                <i className="fas fa-check-circle"></i>
              </div>
              <div className="stat-details">
                <h4>{overallStats.totalApprovedLeaves || 0}</h4>
                <p>Approved Leaves</p>
              </div>
            </div>
          </div>
          <div className="col-md-4">
            <div className="stat-item">
              <div className="stat-icon text-warning">
                <i className="fas fa-clock"></i>
              </div>
              <div className="stat-details">
                <h4>{overallStats.totalPendingLeaves || 0}</h4>
                <p>Pending Leaves</p>
              </div>
            </div>
          </div>
          <div className="col-md-4">
            <div className="stat-item">
              <div className="stat-icon text-danger">
                <i className="fas fa-times-circle"></i>
              </div>
              <div className="stat-details">
                <h4>{overallStats.totalRejectedLeaves || 0}</h4>
                <p>Rejected Leaves</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="reports-page">
      <Container fluid>
        <div className="page-header">
          <div className="d-flex justify-content-between align-items-center mb-4">
            <div>
              <h1 className="page-title">Reports & Analytics</h1>
              <p className="page-subtitle text-muted">
                Comprehensive insights into your organization's performance
              </p>
            </div>
            <div className="date-filters">
              <Button
                variant="outline-secondary"
                size="sm"
                onClick={fetchReportsData}
                disabled={loading}
                className="me-2"
              >
                <i className="fas fa-sync-alt me-2"></i>
                Refresh
              </Button>
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
                  Total payroll costs by department
                </p>
              </Card.Header>
              <Card.Body>
                {renderDepartmentCostChart()}
              </Card.Body>
            </Card>
          </Col>

          {/* Leave Usage Trends */}
          <Col lg={6} className="mb-4">
            <Card className="chart-card">
              <Card.Header>
                <h5 className="chart-title">
                  <i className="fas fa-chart-line me-2"></i>
                  Leave Usage Trends
                </h5>
                <p className="chart-subtitle text-muted">
                  Monthly leave application trends
                </p>
              </Card.Header>
              <Card.Body>
                {renderLeaveUsageChart()}
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
                  Payroll distribution across months
                </p>
              </Card.Header>
              <Card.Body>
                {renderPayrollSummaryChart()}
              </Card.Body>
            </Card>
          </Col>

          {/* Quick Stats */}
          <Col lg={4} className="mb-4">
            <Card className="chart-card">
              <Card.Header>
                <h5 className="chart-title">
                  <i className="fas fa-tachometer-alt me-2"></i>
                  Leave Overview
                </h5>
                <p className="chart-subtitle text-muted">
                  Current leave statistics
                </p>
              </Card.Header>
              <Card.Body>
                {renderQuickStats()}
              </Card.Body>
            </Card>
          </Col>
        </Row>

        {/* Additional Analytics */}
        <Row>
          <Col lg={12}>
            <Card className="analytics-summary">
              <Card.Header>
                <h5 className="chart-title">
                  <i className="fas fa-analytics me-2"></i>
                  Performance Insights
                </h5>
              </Card.Header>
              <Card.Body>
                <Alert variant="info" className="mb-0">
                  <div className="row">
                    <div className="col-md-4">
                      <strong>🎯 Key Finding:</strong> Department costs are well-distributed across the organization.
                    </div>
                    <div className="col-md-4">
                      <strong>📈 Trend:</strong> Leave usage patterns show seasonal variations.
                    </div>
                    <div className="col-md-4">
                      <strong>💡 Recommendation:</strong> Consider implementing automated approval workflows.
                    </div>
                  </div>
                </Alert>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
    </div>
  );
};

export default ReportsPage;
