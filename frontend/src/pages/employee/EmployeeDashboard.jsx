import React from 'react';
import { Row, Col, Card, Button } from 'react-bootstrap';
import { useAuth } from '../../context/AuthContext';

const EmployeeDashboard = () => {
  const { user, getUserDisplayName } = useAuth();

  return (
    <div className="content-card fade-in">
      <div className="mb-4">
        <h1>Welcome back, {getUserDisplayName()}!</h1>
        <p className="text-muted-custom mb-0">
          Access your personal information, leave requests, and payslips.
        </p>
      </div>

        <Row>
          <Col md={6} lg={4} className="mb-4">
            <Card className="h-100">
              <Card.Body>
                <h3>My Profile</h3>
                <p className="text-muted-custom">
                  View and update your personal information and contact details.
                </p>
                <Button variant="primary" disabled>
                  View Profile
                </Button>
              </Card.Body>
            </Card>
          </Col>

          <Col md={6} lg={4} className="mb-4">
            <Card className="h-100">
              <Card.Body>
                <h3>Leave Requests</h3>
                <p className="text-muted-custom">
                  Apply for leave, view request status, and manage your leave balance.
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
                <h3>My Payslips</h3>
                <p className="text-muted-custom">
                  View and download your salary payslips and payment history.
                </p>
                <Button variant="primary" disabled>
                  View Payslips
                </Button>
              </Card.Body>
            </Card>
          </Col>

          <Col md={6} lg={4} className="mb-4">
            <Card className="h-100">
              <Card.Body>
                <h3>Change Password</h3>
                <p className="text-muted-custom">
                  Update your account password for security.
                </p>
                <Button variant="outline-primary" disabled>
                  Change Password
                </Button>
              </Card.Body>
            </Card>
          </Col>
      </Row>

      <div className="mt-4 p-3 bg-light-custom rounded">
        <h3>Employee Information</h3>
        <p><strong>User ID:</strong> {user?.userId}</p>
        <p><strong>Username:</strong> {user?.username}</p>
        <p><strong>Email:</strong> {user?.email}</p>
        <p><strong>Role:</strong> {user?.role}</p>
      </div>
    </div>
  );
};

export default EmployeeDashboard;
