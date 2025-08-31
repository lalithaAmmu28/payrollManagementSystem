import React from 'react';
import { Row, Col, Card, Button } from 'react-bootstrap';
import { useAuth } from '../../context/AuthContext';

const AdminDashboard = () => {
  const { user, getUserDisplayName } = useAuth();

  return (
    <div className="content-card fade-in">
      <div className="mb-4">
        <h1>Welcome back, {getUserDisplayName()}!</h1>
        <p className="text-muted-custom mb-0">
          Manage your organization from this central administration panel.
        </p>
      </div>

        <Row>
          <Col md={6} lg={4} className="mb-4">
            <Card className="h-100">
              <Card.Body>
                <h3>Employee Management</h3>
                <p className="text-muted-custom">
                  Manage employee records, salary structures, and assignments.
                </p>
                <Button variant="primary" disabled>
                  Manage Employees
                </Button>
              </Card.Body>
            </Card>
          </Col>

          <Col md={6} lg={4} className="mb-4">
            <Card className="h-100">
              <Card.Body>
                <h3>Payroll Processing</h3>
                <p className="text-muted-custom">
                  Create and process payroll runs, generate payslips.
                </p>
                <Button variant="primary" disabled>
                  Process Payroll
                </Button>
              </Card.Body>
            </Card>
          </Col>

          <Col md={6} lg={4} className="mb-4">
            <Card className="h-100">
              <Card.Body>
                <h3>Reports & Analytics</h3>
                <p className="text-muted-custom">
                  View comprehensive reports and analytics dashboard.
                </p>
                <Button variant="primary" disabled>
                  View Reports
                </Button>
              </Card.Body>
            </Card>
          </Col>

          <Col md={6} lg={4} className="mb-4">
            <Card className="h-100">
              <Card.Body>
                <h3>Leave Management</h3>
                <p className="text-muted-custom">
                  Review and approve employee leave requests.
                </p>
                <Button variant="primary" disabled>
                  Manage Leaves
                </Button>
              </Card.Body>
            </Card>
          </Col>

          <Col md={6} lg={4} className="mb-4">
            <Card className="h-100">
              <Card.Body>
                <h3>Organization Setup</h3>
                <p className="text-muted-custom">
                  Manage departments, job roles, and organizational structure.
                </p>
                <Button variant="primary" disabled>
                  Setup Organization
                </Button>
              </Card.Body>
            </Card>
          </Col>
      </Row>

      <div className="mt-4 p-3 bg-light-custom rounded">
        <h3>Admin Information</h3>
        <p><strong>User ID:</strong> {user?.userId}</p>
        <p><strong>Username:</strong> {user?.username}</p>
        <p><strong>Email:</strong> {user?.email}</p>
        <p><strong>Role:</strong> {user?.role}</p>
      </div>
    </div>
  );
};

export default AdminDashboard;
