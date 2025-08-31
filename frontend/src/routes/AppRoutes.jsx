import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

// Import page components
import LoginPage from '../pages/LoginPage';
import AdminDashboard from '../pages/admin/AdminDashboard';
import EmployeeDashboard from '../pages/employee/EmployeeDashboard';
import EmployeeManagementPage from '../pages/admin/EmployeeManagementPage';

// Import employee pages
import ProfilePage from '../pages/employee/ProfilePage';
import LeaveRequestPage from '../pages/employee/LeaveRequestPage';
import PayslipsPage from '../pages/employee/PayslipsPage';

// Import layout components
import AdminLayout from '../layouts/AdminLayout';
import EmployeeLayout from '../layouts/EmployeeLayout';

// Import route protection components
import ProtectedRoute, { AdminRoute, EmployeeRoute } from './ProtectedRoute';

const AppRoutes = () => {
  const { isAuthenticated, role } = useAuth();

  // Root redirect logic
  const RootRedirect = () => {
    if (!isAuthenticated()) {
      return <Navigate to="/login" replace />;
    }
    
    // Redirect based on user role
    const redirectPath = role === 'Admin' ? '/admin/dashboard' : '/employee/dashboard';
    return <Navigate to={redirectPath} replace />;
  };

  return (
    <Routes>
      {/* Root route - redirect based on auth status and role */}
      <Route path="/" element={<RootRedirect />} />
      
      {/* Public routes */}
      <Route path="/login" element={<LoginPage />} />
      
      {/* Admin routes - Only accessible by Admin users */}
      <Route path="/admin">
        <Route
          path="dashboard"
          element={
            <AdminRoute>
              <AdminLayout pageTitle="Admin Dashboard">
                <AdminDashboard />
              </AdminLayout>
            </AdminRoute>
          }
        />
        
        {/* Employee Management Routes (Admin only) */}
        <Route
          path="employees"
          element={
            <AdminRoute>
              <AdminLayout pageTitle="Employee Management">
                <EmployeeManagementPage />
              </AdminLayout>
            </AdminRoute>
          }
        />
        
        {/* Payroll Management Routes (Admin only) */}
        <Route
          path="payroll"
          element={
            <AdminRoute>
              <AdminLayout pageTitle="Payroll Management">
                <div className="content-card fade-in">
                  <h1>Payroll Management</h1>
                  <p className="text-muted-custom">Payroll management module coming soon...</p>
                </div>
              </AdminLayout>
            </AdminRoute>
          }
        />
        
        {/* Leave Management Routes (Admin only) */}
        <Route
          path="leaves"
          element={
            <AdminRoute>
              <AdminLayout pageTitle="Leave Management">
                <div className="content-card fade-in">
                  <h1>Leave Management</h1>
                  <p className="text-muted-custom">Leave management module coming soon...</p>
                </div>
              </AdminLayout>
            </AdminRoute>
          }
        />
        
        {/* Reports & Analytics Routes (Admin only) */}
        <Route
          path="reports"
          element={
            <AdminRoute>
              <AdminLayout pageTitle="Reports & Analytics">
                <div className="content-card fade-in">
                  <h1>Reports & Analytics</h1>
                  <p className="text-muted-custom">Reports and analytics module coming soon...</p>
                </div>
              </AdminLayout>
            </AdminRoute>
          }
        />
        
        {/* Organization Setup Routes (Admin only) */}
        <Route
          path="organization"
          element={
            <AdminRoute>
              <AdminLayout pageTitle="Organization Setup">
                <div className="content-card fade-in">
                  <h1>Organization Setup</h1>
                  <p className="text-muted-custom">Organization setup module coming soon...</p>
                </div>
              </AdminLayout>
            </AdminRoute>
          }
        />
        
        {/* Admin wildcard - redirect to dashboard */}
        <Route
          path="*"
          element={
            <AdminRoute>
              <Navigate to="/admin/dashboard" replace />
            </AdminRoute>
          }
        />
      </Route>
      
      {/* Employee routes - Accessible by both Admin and Employee users */}
      <Route path="/employee">
        <Route
          path="dashboard"
          element={
            <EmployeeRoute>
              <EmployeeLayout>
                <EmployeeDashboard />
              </EmployeeLayout>
            </EmployeeRoute>
          }
        />
        
        {/* Profile Management */}
        <Route
          path="profile"
          element={
            <EmployeeRoute>
              <EmployeeLayout>
                <ProfilePage />
              </EmployeeLayout>
            </EmployeeRoute>
          }
        />
        
        {/* Leave Requests */}
        <Route
          path="leave"
          element={
            <EmployeeRoute>
              <EmployeeLayout>
                <LeaveRequestPage />
              </EmployeeLayout>
            </EmployeeRoute>
          }
        />
        
        {/* Payslips */}
        <Route
          path="payslips"
          element={
            <EmployeeRoute>
              <EmployeeLayout>
                <PayslipsPage />
              </EmployeeLayout>
            </EmployeeRoute>
          }
        />
        
        {/* Salary Structure */}
        <Route
          path="salary"
          element={
            <EmployeeRoute>
              <EmployeeLayout>
                <div className="content-card fade-in">
                  <h1>My Salary Structure</h1>
                  <p className="text-muted-custom">Salary structure module coming soon...</p>
                </div>
              </EmployeeLayout>
            </EmployeeRoute>
          }
        />
        
        {/* Employee wildcard - redirect to dashboard */}
        <Route
          path="*"
          element={
            <EmployeeRoute>
              <Navigate to="/employee/dashboard" replace />
            </EmployeeRoute>
          }
        />
      </Route>
      
      {/* Catch-all route - redirect unauthorized users to login */}
      <Route
        path="*"
        element={
          <ProtectedRoute>
            <div className="main-content">
              <div className="content-card text-center">
                <h1>404 - Page Not Found</h1>
                <p className="text-muted-custom">
                  The page you're looking for doesn't exist.
                </p>
                <button
                  className="btn btn-primary"
                  onClick={() => {
                    const redirectPath = role === 'Admin' ? '/admin/dashboard' : '/employee/dashboard';
                    window.location.href = redirectPath;
                  }}
                >
                  Go to Dashboard
                </button>
              </div>
            </div>
          </ProtectedRoute>
        }
      />
    </Routes>
  );
};

export default AppRoutes;
