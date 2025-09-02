import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Badge } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../components/common/ToastNotification';
import { LoadingSpinner } from '../../components/common';
import { apiService } from '../../api';
import './styles/EmployeeDashboard.css';

const EmployeeDashboard = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { showToast } = useToast();
  const [dashboardData, setDashboardData] = useState({
    leaveBalance: 0,
    recentPayslip: null,
    pendingLeaveRequests: 0,
    upcomingLeaves: []
  });
  const [employeeData, setEmployeeData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      
      // Fetch dashboard data from multiple endpoints
      const [userResponse, payslipsResponse, leaveRequestsResponse] = await Promise.all([
        apiService.get('/users/me'),
        apiService.get('/payroll/payslips').catch(() => ({ data: { data: [] } })),
        apiService.get('/leave-requests/my').catch(() => ({ data: { data: [] } }))
      ]);

      const userData = userResponse.data.data;
      const payslips = payslipsResponse.data.data || [];
      const leaveRequests = leaveRequestsResponse.data.data || [];

      // Store employee data for display
      setEmployeeData(userData);

      // Process the data
      const recentPayslip = payslips.length > 0 ? payslips[0] : null;
      const pendingRequests = leaveRequests.filter(req => req.status === 'Pending').length;
      const upcomingLeaves = leaveRequests
        .filter(req => req.status === 'Approved' && new Date(req.startDate) > new Date())
        .slice(0, 3);

      setDashboardData({
        leaveBalance: userData.leaveBalance || 0,
        recentPayslip,
        pendingLeaveRequests: pendingRequests,
        upcomingLeaves
      });
    } catch (error) {
      showToast('Failed to load dashboard data', 'error');
      console.error('Dashboard data fetch error:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2
    }).format(amount || 0);
  };

  const formatPayPeriod = (payslip) => {
    if (payslip?.runMonth && payslip?.runYear) {
      const monthNames = [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'
      ];
      return `${monthNames[payslip.runMonth - 1]} ${payslip.runYear}`;
    }
    return 'Unknown Period';
  };

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 18) return 'Good afternoon';
    return 'Good evening';
  };

  if (loading) {
    return (
      <Container className="employee-dashboard">
        <div className="d-flex justify-content-center align-items-center" style={{ height: '400px' }}>
          <LoadingSpinner />
        </div>
      </Container>
    );
  }

  return (
    <Container className="employee-dashboard">
      {/* Welcome Section */}
      <div className="welcome-section">
        <div className="welcome-content">
          <h1>{getGreeting()}, {employeeData?.firstName || 'Employee'}!</h1>
          <p className="welcome-subtitle">Here's what's happening with your account today</p>
        </div>
        <div className="welcome-icon">
          <i className="fas fa-user-circle"></i>
        </div>
      </div>

      {/* Quick Stats Cards */}
      <Row className="stats-row">
        <Col lg={3} md={6} className="mb-4">
          <Card className="stat-card leave-balance-card">
            <Card.Body>
              <div className="stat-content">
                <div className="stat-icon">
                  <i className="fas fa-calendar-check"></i>
                </div>
                <div className="stat-info">
                  <h3>{dashboardData.leaveBalance}</h3>
                  <p>Available Leave Days</p>
                </div>
              </div>
            </Card.Body>
          </Card>
        </Col>

        <Col lg={3} md={6} className="mb-4">
          <Card className="stat-card pending-requests-card">
            <Card.Body>
              <div className="stat-content">
                <div className="stat-icon">
                  <i className="fas fa-clock"></i>
                </div>
                <div className="stat-info">
                  <h3>{dashboardData.pendingLeaveRequests}</h3>
                  <p>Pending Requests</p>
                </div>
              </div>
            </Card.Body>
          </Card>
        </Col>

        <Col lg={3} md={6} className="mb-4">
          <Card className="stat-card recent-payslip-card">
            <Card.Body>
              <div className="stat-content">
                <div className="stat-icon">
                  <i className="fas fa-money-bill-wave"></i>
                </div>
                <div className="stat-info">
                  <h3>{dashboardData.recentPayslip ? formatCurrency(dashboardData.recentPayslip.netSalary) : 'N/A'}</h3>
                  <p>Latest Payslip</p>
                </div>
              </div>
            </Card.Body>
          </Card>
        </Col>

        <Col lg={3} md={6} className="mb-4">
          <Card className="stat-card upcoming-leaves-card">
            <Card.Body>
              <div className="stat-content">
                <div className="stat-icon">
                  <i className="fas fa-plane"></i>
                </div>
                <div className="stat-info">
                  <h3>{dashboardData.upcomingLeaves.length}</h3>
                  <p>Upcoming Leaves</p>
                </div>
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Quick Actions and Recent Activity */}
      <Row>
        <Col lg={6} className="mb-4">
          <Card className="quick-actions-card">
            <Card.Header>
              <h4>Quick Actions</h4>
              <p className="text-muted">Common tasks you might want to perform</p>
            </Card.Header>
            <Card.Body>
              <div className="action-buttons">
                <Button
                  variant="primary"
                  size="lg"
                  className="action-btn"
                  onClick={() => navigate('/employee/leave')}
                >
                  <i className="fas fa-plus me-2"></i>
                  Apply for Leave
                </Button>
                
                <Button
                  variant="outline-primary"
                  size="lg"
                  className="action-btn"
                  onClick={() => navigate('/employee/payslips')}
                >
                  <i className="fas fa-file-invoice-dollar me-2"></i>
                  View Payslips
                </Button>
                
                <Button
                  variant="outline-secondary"
                  size="lg"
                  className="action-btn"
                  onClick={() => navigate('/employee/profile')}
                >
                  <i className="fas fa-user-edit me-2"></i>
                  Update Profile
                </Button>
              </div>
            </Card.Body>
          </Card>
        </Col>

        <Col lg={6} className="mb-4">
          <Card className="recent-activity-card">
            <Card.Header>
              <h4>Recent Activity</h4>
              <p className="text-muted">Your latest transactions and updates</p>
            </Card.Header>
            <Card.Body>
              {/* Recent Payslip */}
              {dashboardData.recentPayslip && (
                <div className="activity-item payslip-activity">
                  <div className="activity-icon">
                    <i className="fas fa-money-bill-wave text-success"></i>
                  </div>
                  <div className="activity-content">
                    <h6>Latest Payslip Available</h6>
                    <p className="text-muted">
                      {formatPayPeriod(dashboardData.recentPayslip)} - {formatCurrency(dashboardData.recentPayslip.netSalary)}
                    </p>
                    <Button
                      variant="link"
                      size="sm"
                      className="p-0"
                      onClick={() => navigate('/employee/payslips')}
                    >
                      View Details <i className="fas fa-arrow-right ms-1"></i>
                    </Button>
                  </div>
                </div>
              )}

              {/* Pending Leave Requests */}
              {dashboardData.pendingLeaveRequests > 0 && (
                <div className="activity-item leave-activity">
                  <div className="activity-icon">
                    <i className="fas fa-clock text-warning"></i>
                  </div>
                  <div className="activity-content">
                    <h6>Pending Leave Requests</h6>
                    <p className="text-muted">
                      You have {dashboardData.pendingLeaveRequests} leave request{dashboardData.pendingLeaveRequests > 1 ? 's' : ''} awaiting approval
                    </p>
                    <Button
                      variant="link"
                      size="sm"
                      className="p-0"
                      onClick={() => navigate('/employee/leave')}
                    >
                      View Requests <i className="fas fa-arrow-right ms-1"></i>
                    </Button>
                  </div>
                </div>
              )}

              {/* Upcoming Leaves */}
              {dashboardData.upcomingLeaves.length > 0 && (
                <div className="activity-item upcoming-activity">
                  <div className="activity-icon">
                    <i className="fas fa-plane text-info"></i>
                  </div>
                  <div className="activity-content">
                    <h6>Upcoming Approved Leaves</h6>
                    <div className="upcoming-leaves-list">
                      {dashboardData.upcomingLeaves.slice(0, 2).map((leave, index) => (
                        <div key={index} className="upcoming-leave">
                          <Badge bg="info" className="me-2">{leave.leaveType}</Badge>
                          <span>{new Date(leave.startDate).toLocaleDateString()}</span>
                        </div>
                      ))}
                    </div>
                    <Button
                      variant="link"
                      size="sm"
                      className="p-0"
                      onClick={() => navigate('/employee/leave')}
                    >
                      View All <i className="fas fa-arrow-right ms-1"></i>
                    </Button>
                  </div>
                </div>
              )}

              {/* Empty State */}
              {!dashboardData.recentPayslip && dashboardData.pendingLeaveRequests === 0 && dashboardData.upcomingLeaves.length === 0 && (
                <div className="empty-activity">
                  <i className="fas fa-clipboard-list"></i>
                  <p>No recent activity to display</p>
                </div>
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>

      
    </Container>
  );
};

export default EmployeeDashboard;
