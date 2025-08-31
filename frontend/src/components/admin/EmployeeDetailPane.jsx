import React, { useState, useEffect } from 'react';
import { Card, Button, Tabs, Tab, Badge, Alert } from 'react-bootstrap';
import apiService from '../../api/apiService';
import { useToast } from '../common/ToastNotification';
import { LoadingSpinner, SkeletonLoader, ConfirmationModal } from '../common';
import ProfileTab from './EmployeeDetailTabs/ProfileTab';
import SalaryStructureTab from './EmployeeDetailTabs/SalaryStructureTab';
import PayslipsTab from './EmployeeDetailTabs/PayslipsTab';
import LeaveHistoryTab from './EmployeeDetailTabs/LeaveHistoryTab';
import './styles/EmployeeDetailPane.css';

const EmployeeDetailPane = ({ 
  employee, 
  onClose, 
  onEmployeeUpdated, 
  onEmployeeDeleted 
}) => {
  const { showToast } = useToast();
  const [detailedEmployee, setDetailedEmployee] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('profile');
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    if (employee?.employeeId) {
      fetchEmployeeDetails(employee.employeeId);
    }
  }, [employee]);

  const fetchEmployeeDetails = async (employeeId) => {
    try {
      setLoading(true);
      const response = await apiService.get(`/employees/${employeeId}`);
      setDetailedEmployee(response.data.data);
    } catch (error) {
      showToast('Failed to fetch employee details', 'error');
      console.error('Fetch employee details error:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteEmployee = async () => {
    try {
      setDeleting(true);
      await apiService.delete(`/employees/${employee.employeeId}`);
      onEmployeeDeleted(employee.employeeId);
      setShowDeleteConfirm(false);
    } catch (error) {
      showToast('Failed to delete employee', 'error');
      console.error('Delete employee error:', error);
    } finally {
      setDeleting(false);
    }
  };

  const handleEmployeeUpdate = (updatedEmployee) => {
    setDetailedEmployee(updatedEmployee);
    onEmployeeUpdated(updatedEmployee);
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString();
  };

  const getStatusBadge = (isActive) => {
    return (
      <Badge bg={isActive ? 'success' : 'secondary'} className="status-badge">
        {isActive ? 'Active' : 'Inactive'}
      </Badge>
    );
  };

  if (loading) {
    return (
      <div className="employee-detail-pane">
        <Card className="detail-card">
          <Card.Header className="detail-header">
            <div className="d-flex justify-content-between align-items-center">
              <SkeletonLoader width="200px" height="24px" />
              <Button variant="link" onClick={onClose} className="close-btn">
                <i className="fas fa-times"></i>
              </Button>
            </div>
          </Card.Header>
          <Card.Body>
            <div className="loading-content">
              <SkeletonLoader height="60px" className="mb-3" />
              <SkeletonLoader height="40px" className="mb-3" />
              <SkeletonLoader height="300px" />
            </div>
          </Card.Body>
        </Card>
      </div>
    );
  }

  if (!detailedEmployee) {
    return (
      <div className="employee-detail-pane">
        <Card className="detail-card">
          <Card.Header className="detail-header">
            <div className="d-flex justify-content-between align-items-center">
              <h5 className="mb-0">Employee Details</h5>
              <Button variant="link" onClick={onClose} className="close-btn">
                <i className="fas fa-times"></i>
              </Button>
            </div>
          </Card.Header>
          <Card.Body>
            <Alert variant="warning">
              <i className="fas fa-exclamation-triangle me-2"></i>
              Employee details could not be loaded.
            </Alert>
          </Card.Body>
        </Card>
      </div>
    );
  }

  return (
    <div className="employee-detail-pane">
      <Card className="detail-card">
        <Card.Header className="detail-header">
          <div className="d-flex justify-content-between align-items-center">
            <div className="employee-header-info">
              <h5 className="employee-title mb-1">
                {detailedEmployee.firstName} {detailedEmployee.lastName}
              </h5>
              <div className="employee-meta">
                <span className="employee-id">ID: {detailedEmployee.employeeId}</span>
                {getStatusBadge(detailedEmployee.isActive)}
              </div>
            </div>
            <Button variant="link" onClick={onClose} className="close-btn">
              <i className="fas fa-times"></i>
            </Button>
          </div>
        </Card.Header>

        <Card.Body className="detail-body">
          {/* Quick Info Section */}
          <div className="quick-info-section mb-4">
            <div className="row g-3">
              <div className="col-6">
                <div className="info-item">
                  <small className="text-muted">Department</small>
                  <div className="info-value">{detailedEmployee.departmentName}</div>
                </div>
              </div>
              <div className="col-6">
                <div className="info-item">
                  <small className="text-muted">Job Title</small>
                  <div className="info-value">{detailedEmployee.jobTitle}</div>
                </div>
              </div>
              <div className="col-6">
                <div className="info-item">
                  <small className="text-muted">Email</small>
                  <div className="info-value">{detailedEmployee.email}</div>
                </div>
              </div>
              <div className="col-6">
                <div className="info-item">
                  <small className="text-muted">Joined</small>
                  <div className="info-value">{formatDate(detailedEmployee.createdAt)}</div>
                </div>
              </div>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="action-buttons mb-4">
            <Button
              variant="outline-danger"
              size="sm"
              onClick={() => setShowDeleteConfirm(true)}
              className="delete-btn"
            >
              <i className="fas fa-trash me-1"></i>
              Delete Employee
            </Button>
          </div>

          {/* Tabs Section */}
          <Tabs
            activeKey={activeTab}
            onSelect={(tab) => setActiveTab(tab)}
            className="detail-tabs"
          >
            <Tab eventKey="profile" title={
              <span><i className="fas fa-user me-2"></i>Profile</span>
            }>
              <ProfileTab
                employee={detailedEmployee}
                onEmployeeUpdated={handleEmployeeUpdate}
              />
            </Tab>

            <Tab eventKey="salary" title={
              <span><i className="fas fa-dollar-sign me-2"></i>Salary Structure</span>
            }>
              <SalaryStructureTab
                employeeId={detailedEmployee.employeeId}
              />
            </Tab>

            <Tab eventKey="payslips" title={
              <span><i className="fas fa-file-invoice-dollar me-2"></i>Payslips</span>
            }>
              <PayslipsTab
                employeeId={detailedEmployee.employeeId}
              />
            </Tab>

            <Tab eventKey="leave" title={
              <span><i className="fas fa-calendar-alt me-2"></i>Leave History</span>
            }>
              <LeaveHistoryTab
                employeeId={detailedEmployee.employeeId}
              />
            </Tab>
          </Tabs>
        </Card.Body>
      </Card>

      {/* Delete Confirmation Modal */}
      <ConfirmationModal
        show={showDeleteConfirm}
        onHide={() => setShowDeleteConfirm(false)}
        onConfirm={handleDeleteEmployee}
        title="Delete Employee"
        message={
          <div>
            <p>Are you sure you want to delete <strong>{detailedEmployee.firstName} {detailedEmployee.lastName}</strong>?</p>
            <Alert variant="warning" className="mt-3">
              <small>
                <i className="fas fa-exclamation-triangle me-2"></i>
                This action cannot be undone. All associated data including payslips and leave history will be permanently removed.
              </small>
            </Alert>
          </div>
        }
        confirmText={deleting ? <LoadingSpinner size="sm" /> : "Delete Employee"}
        confirmVariant="danger"
        isLoading={deleting}
      />
    </div>
  );
};

export default EmployeeDetailPane;
