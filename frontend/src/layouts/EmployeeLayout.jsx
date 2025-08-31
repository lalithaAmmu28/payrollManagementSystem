import React from 'react';
import { Navbar, Nav, NavDropdown, Container } from 'react-bootstrap';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import './styles/EmployeeLayout.css';

const EmployeeLayout = ({ children }) => {
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
    <div className="employee-layout-container">
      {/* Stable Sidebar - Always Expanded */}
      <div className="employee-sidebar">
        {/* Sidebar Header */}
        <div className="sidebar-header">
          <div className="sidebar-logo">
            <i className="fas fa-user-tie"></i>
            <span>PMS Employee</span>
          </div>
        </div>

        {/* Sidebar Navigation */}
        <nav className="sidebar-nav">
          <ul className="nav-list">
            <li className="nav-item">
              <button 
                className="nav-link"
                onClick={() => handleNavigation('/employee/dashboard')}
              >
                <i className="fas fa-tachometer-alt nav-icon"></i>
                <span className="nav-label">Dashboard</span>
              </button>
            </li>
            
            <li className="nav-item">
              <button 
                className="nav-link"
                onClick={() => handleNavigation('/employee/profile')}
              >
                <i className="fas fa-user nav-icon"></i>
                <span className="nav-label">My Profile</span>
              </button>
            </li>
            
            <li className="nav-item">
              <button 
                className="nav-link"
                onClick={() => handleNavigation('/employee/leave')}
              >
                <i className="fas fa-calendar-alt nav-icon"></i>
                <span className="nav-label">Leave Management</span>
              </button>
            </li>
            
            <li className="nav-item">
              <button 
                className="nav-link"
                onClick={() => handleNavigation('/employee/payslips')}
              >
                <i className="fas fa-money-bill-wave nav-icon"></i>
                <span className="nav-label">My Payslips</span>
              </button>
            </li>
            
            <li className="nav-item">
              <button 
                className="nav-link"
                onClick={() => handleNavigation('/employee/salary')}
              >
                <i className="fas fa-chart-line nav-icon"></i>
                <span className="nav-label">Salary Structure</span>
              </button>
            </li>
          </ul>
        </nav>

        {/* Sidebar Footer */}
        <div className="sidebar-footer">
          <div className="sidebar-user-info">
            <i className="fas fa-user-circle"></i>
            <span>{user?.firstName || 'Employee'}</span>
          </div>
        </div>
      </div>

      {/* Main Content Area */}
      <div className="main-layout">
        {/* Header */}
        <Navbar 
          bg="light" 
          expand="lg" 
          className="employee-header"
        >
          <Container fluid>
            <Navbar.Brand className="page-title">
              Payroll Management System
            </Navbar.Brand>
            
            <Nav className="ms-auto">
              <NavDropdown 
                title={user?.firstName || 'Employee'} 
                id="employee-nav-dropdown"
                className="user-dropdown"
              >
                <NavDropdown.Item onClick={() => handleNavigation('/employee/profile')}>
                  <i className="fas fa-user me-2"></i>
                  Profile
                </NavDropdown.Item>
                <NavDropdown.Item onClick={() => handleNavigation('/employee/settings')}>
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

export default EmployeeLayout;
