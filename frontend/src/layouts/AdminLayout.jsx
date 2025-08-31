import React from 'react';
import { Navbar, Nav, NavDropdown, Container } from 'react-bootstrap';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import './styles/AdminLayout.css';

const AdminLayout = ({ children }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleNavigation = (path) => {
    navigate(path);
  };

  return (
    <div className="admin-layout-container">
      {/* Stable Sidebar - Always Expanded */}
      <div className="admin-sidebar">
        {/* Sidebar Header */}
        <div className="sidebar-header">
          <div className="sidebar-logo">
            <i className="fas fa-chart-line"></i>
            <span>PMS Admin</span>
          </div>
        </div>

        {/* Sidebar Navigation */}
        <nav className="sidebar-nav">
          <ul className="nav-list">
            <li className="nav-item">
              <button 
                className="nav-link"
                onClick={() => handleNavigation('/admin/dashboard')}
              >
                <i className="fas fa-tachometer-alt nav-icon"></i>
                <span className="nav-label">Dashboard</span>
              </button>
            </li>
            
            <li className="nav-item">
              <button 
                className="nav-link"
                onClick={() => handleNavigation('/admin/employees')}
              >
                <i className="fas fa-users nav-icon"></i>
                <span className="nav-label">Employee Management</span>
              </button>
            </li>
            
            <li className="nav-item">
              <button 
                className="nav-link"
                onClick={() => handleNavigation('/admin/departments')}
              >
                <i className="fas fa-building nav-icon"></i>
                <span className="nav-label">Departments</span>
              </button>
            </li>
            
            <li className="nav-item">
              <button 
                className="nav-link"
                onClick={() => handleNavigation('/admin/job-roles')}
              >
                <i className="fas fa-briefcase nav-icon"></i>
                <span className="nav-label">Job Roles</span>
              </button>
            </li>
            
            <li className="nav-item">
              <button 
                className="nav-link"
                onClick={() => handleNavigation('/admin/leave-requests')}
              >
                <i className="fas fa-calendar-alt nav-icon"></i>
                <span className="nav-label">Leave Requests</span>
              </button>
            </li>
            
            <li className="nav-item">
              <button 
                className="nav-link"
                onClick={() => handleNavigation('/admin/payroll')}
              >
                <i className="fas fa-money-bill-wave nav-icon"></i>
                <span className="nav-label">Payroll Management</span>
              </button>
            </li>
            
            <li className="nav-item">
              <button 
                className="nav-link"
                onClick={() => handleNavigation('/admin/reports')}
              >
                <i className="fas fa-chart-bar nav-icon"></i>
                <span className="nav-label">Reports & Analytics</span>
              </button>
            </li>
          </ul>
        </nav>

        {/* Sidebar Footer */}
        <div className="sidebar-footer">
          <div className="sidebar-user-info">
            <i className="fas fa-user-circle"></i>
            <span>{user?.firstName || 'Admin'}</span>
          </div>
        </div>
      </div>

      {/* Main Content Area */}
      <div className="main-layout">
        {/* Header */}
        <Navbar 
          bg="light" 
          expand="lg" 
          className="admin-header"
        >
          <Container fluid>
            <Navbar.Brand className="page-title">
              Payroll Management System
            </Navbar.Brand>
            
            <Nav className="ms-auto">
              <NavDropdown 
                title={user?.firstName || 'Admin'} 
                id="admin-nav-dropdown"
                className="user-dropdown"
              >
                <NavDropdown.Item onClick={() => handleNavigation('/admin/profile')}>
                  <i className="fas fa-user me-2"></i>
                  Profile
                </NavDropdown.Item>
                <NavDropdown.Item onClick={() => handleNavigation('/admin/settings')}>
                  <i className="fas fa-cog me-2"></i>
                  Settings
                </NavDropdown.Item>
                <NavDropdown.Divider />
                <NavDropdown.Item onClick={handleLogout}>
                  <i className="fas fa-sign-out-alt me-2"></i>
                  Logout
                </NavDropdown.Item>
              </NavDropdown>
            </Nav>
          </Container>
        </Navbar>

        {/* Page Content */}
        <div className="page-content">
          {children}
        </div>
      </div>
    </div>
  );
};

export default AdminLayout;
