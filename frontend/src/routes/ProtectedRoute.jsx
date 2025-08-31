import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Container, Row, Col, Spinner, Alert } from 'react-bootstrap';

const ProtectedRoute = ({ children, requiredRole = null, fallbackPath = '/login' }) => {
  const { isAuthenticated, hasRole, role, loading } = useAuth();
  const location = useLocation();

  // Show loading spinner while checking authentication
  if (loading) {
    return (
      <Container fluid className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
        <div className="text-center">
          <Spinner
            animation="border"
            variant="primary"
            style={{ width: '3rem', height: '3rem' }}
          />
          <p className="mt-3 text-muted-custom">Loading...</p>
        </div>
      </Container>
    );
  }

  // If not authenticated, redirect to login with current location
  if (!isAuthenticated()) {
    return (
      <Navigate
        to="/login"
        state={{ from: location }}
        replace
      />
    );
  }

  // If a specific role is required
  if (requiredRole) {
    // Check if user has the required role
    if (!hasRole(requiredRole)) {
      // User is authenticated but doesn't have required role
      return (
        <Container fluid className="main-content">
          <Row className="justify-content-center">
            <Col md={8} lg={6}>
              <div className="content-card text-center">
                <Alert variant="warning" className="mb-4">
                  <Alert.Heading>Access Denied</Alert.Heading>
                  <p className="mb-3">
                    You don't have permission to access this page. 
                    This area is restricted to <strong>{requiredRole}</strong> users only.
                  </p>
                  <p className="mb-0">
                    Your current role: <strong>{role}</strong>
                  </p>
                </Alert>
                
                <div className="d-flex gap-3 justify-content-center">
                  <button
                    className="btn btn-primary"
                    onClick={() => {
                      // Redirect based on user's role
                      const redirectPath = role === 'Admin' ? '/admin/dashboard' : '/employee/dashboard';
                      window.location.href = redirectPath;
                    }}
                  >
                    Go to My Dashboard
                  </button>
                  
                  <button
                    className="btn btn-outline-secondary"
                    onClick={() => window.history.back()}
                  >
                    Go Back
                  </button>
                </div>
              </div>
            </Col>
          </Row>
        </Container>
      );
    }
  }

  // User is authenticated and has required role (if specified)
  return children;
};

// Higher-order component for admin-only routes
export const AdminRoute = ({ children }) => {
  return (
    <ProtectedRoute requiredRole="ADMIN">
      {children}
    </ProtectedRoute>
  );
};

// Higher-order component for employee routes (both admin and employee can access)
export const EmployeeRoute = ({ children }) => {
  const { isAuthenticated, isAdmin, isEmployee } = useAuth();
  
  // Allow access if user is authenticated and is either admin or employee
  if (isAuthenticated() && (isAdmin() || isEmployee())) {
    return children;
  }
  
  return (
    <ProtectedRoute>
      {children}
    </ProtectedRoute>
  );
};

// Higher-order component for employee-only routes (admin cannot access)
export const EmployeeOnlyRoute = ({ children }) => {
  return (
    <ProtectedRoute requiredRole="EMPLOYEE">
      {children}
    </ProtectedRoute>
  );
};

export default ProtectedRoute;
