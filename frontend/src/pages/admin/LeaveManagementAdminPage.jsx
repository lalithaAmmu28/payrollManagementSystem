import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Table, Tabs, Tab, Badge, Modal, Alert, Form } from 'react-bootstrap';
import apiService from '../../api/apiService';
import { useToast } from '../../components/common/ToastNotification';
import { LoadingSpinner, SkeletonLoader } from '../../components/common';
import './styles/LeaveManagementAdminPage.css';

const LeaveManagementAdminPage = () => {
  const { showToast } = useToast();
  const [activeTab, setActiveTab] = useState('Pending');
  const [leaveRequests, setLeaveRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [updatingStatus, setUpdatingStatus] = useState(false);
  const [actionReason, setActionReason] = useState('');

  useEffect(() => {
    fetchLeaveRequests(activeTab);
  }, [activeTab]);

  const fetchLeaveRequests = async (status = 'Pending') => {
    try {
      setLoading(true);
      const response = await apiService.get(`/leave-requests?status=${status}`);
      setLeaveRequests(response.data.data);
    } catch (error) {
      showToast('Failed to fetch leave requests', 'error');
      console.error('Fetch leave requests error:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleStatusUpdate = async (leaveId, newStatus, reason = '') => {
    try {
      setUpdatingStatus(true);
      await apiService.patch(`/leave-requests/${leaveId}/status`, {
        status: newStatus,
        reason: reason
      });
      
      // Remove from current list if status changed
      setLeaveRequests(prev => prev.filter(req => req.leaveId !== leaveId));
      
      // Update selected request if it's the same one
      if (selectedRequest?.leaveId === leaveId) {
        setSelectedRequest(prev => ({ ...prev, status: newStatus }));
      }
      
      setShowDetailsModal(false);
      setActionReason('');
      showToast(`Leave request ${newStatus.toLowerCase()} successfully!`, 'success');
    } catch (error) {
      showToast('Failed to update leave status', 'error');
      console.error('Update leave status error:', error);
    } finally {
      setUpdatingStatus(false);
    }
  };

  const handleQuickAction = async (request, action) => {
    await handleStatusUpdate(request.leaveId, action);
  };

  const openDetailsModal = (request) => {
    setSelectedRequest(request);
    setShowDetailsModal(true);
    setActionReason('');
  };

  const getStatusBadge = (status) => {
    const statusConfig = {
      'Pending': { bg: 'warning', text: 'Pending', icon: 'fas fa-clock' },
      'Approved': { bg: 'success', text: 'Approved', icon: 'fas fa-check-circle' },
      'Rejected': { bg: 'danger', text: 'Rejected', icon: 'fas fa-times-circle' }
    };
    
    const config = statusConfig[status] || { bg: 'secondary', text: status, icon: 'fas fa-question' };
    return (
      <Badge bg={config.bg} className="status-badge">
        <i className={`${config.icon} me-1`}></i>
        {config.text}
      </Badge>
    );
  };

  const getLeaveTypeBadge = (type) => {
    const typeConfig = {
      'SICK': { bg: 'info', icon: 'fas fa-thermometer-half' },
      'CASUAL': { bg: 'primary', icon: 'fas fa-coffee' },
      'PAID': { bg: 'success', icon: 'fas fa-umbrella-beach' },
      'Sick': { bg: 'info', icon: 'fas fa-thermometer-half' },
      'Casual': { bg: 'primary', icon: 'fas fa-coffee' },
      'Paid': { bg: 'success', icon: 'fas fa-umbrella-beach' }
    };
    
    const config = typeConfig[type] || { bg: 'secondary', icon: 'fas fa-calendar' };
    return (
      <Badge bg={config.bg} className="leave-type-badge">
        <i className={`${config.icon} me-1`}></i>
        {type}
      </Badge>
    );
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString();
  };

  const calculateDuration = (startDate, endDate) => {
    const start = new Date(startDate);
    const end = new Date(endDate);
    const diffTime = Math.abs(end - start);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
    return diffDays;
  };

  const getTabCounts = () => {
    // This would ideally come from the API, but for now we'll show the current count
    return {
      Pending: activeTab === 'Pending' ? leaveRequests.length : '...',
      Approved: activeTab === 'Approved' ? leaveRequests.length : '...',
      Rejected: activeTab === 'Rejected' ? leaveRequests.length : '...'
    };
  };

  const renderLeaveTable = () => {
    if (loading) {
      return (
        <div className="skeleton-table">
          <SkeletonLoader height="40px" className="mb-2" />
          {[...Array(5)].map((_, index) => (
            <SkeletonLoader key={index} height="60px" className="mb-2" />
          ))}
        </div>
      );
    }

    if (leaveRequests.length === 0) {
      return (
        <div className="no-requests">
          <div className="text-center py-5">
            <i className="fas fa-calendar-times fa-3x text-muted mb-3"></i>
            <h6 className="text-muted">No {activeTab.toLowerCase()} leave requests</h6>
            <p className="text-muted">
              {activeTab === 'Pending' 
                ? 'No leave requests are currently pending approval.'
                : `No ${activeTab.toLowerCase()} leave requests found.`
              }
            </p>
          </div>
        </div>
      );
    }

    return (
      <Table hover responsive className="leave-table">
        <thead>
          <tr>
            <th>Employee</th>
            <th>Leave Type</th>
            <th>Duration</th>
            <th>Dates</th>
            <th>Applied On</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {leaveRequests.map((request) => (
            <tr key={request.leaveId} className="leave-row">
              <td>
                <div className="employee-info">
                  <div className="employee-name">
                    {request.employeeName || 'Unknown Employee'}
                  </div>
                  <small className="text-muted">ID: {request.employeeId}</small>
                </div>
              </td>
              <td>{getLeaveTypeBadge(request.leaveType)}</td>
              <td>
                <span className="duration-badge">
                  {calculateDuration(request.startDate, request.endDate)} day{calculateDuration(request.startDate, request.endDate) > 1 ? 's' : ''}
                </span>
              </td>
              <td>
                <div className="date-range">
                  <div>{formatDate(request.startDate)}</div>
                  <small className="text-muted">to {formatDate(request.endDate)}</small>
                </div>
              </td>
              <td>
                <small className="text-muted">
                  {formatDate(request.createdAt)}
                </small>
              </td>
              <td>{getStatusBadge(request.status)}</td>
              <td>
                <div className="action-buttons">
                  <Button
                    variant="outline-primary"
                    size="sm"
                    onClick={() => openDetailsModal(request)}
                    className="me-2"
                  >
                    <i className="fas fa-eye me-1"></i>
                    Details
                  </Button>
                  {request.status === 'Pending' && (
                    <>
                      <Button
                        variant="success"
                        size="sm"
                        onClick={() => handleQuickAction(request, 'Approved')}
                        className="me-2"
                        disabled={updatingStatus}
                      >
                        {updatingStatus ? <LoadingSpinner size="sm" /> : <i className="fas fa-check me-1"></i>}
                        Approve
                      </Button>
                      <Button
                        variant="danger"
                        size="sm"
                        onClick={() => handleQuickAction(request, 'Rejected')}
                        disabled={updatingStatus}
                      >
                        {updatingStatus ? <LoadingSpinner size="sm" /> : <i className="fas fa-times me-1"></i>}
                        Reject
                      </Button>
                    </>
                  )}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </Table>
    );
  };

  const tabCounts = getTabCounts();

  return (
    <div className="leave-management-admin-page">
      <Container fluid>
        <div className="page-header">
          <div className="d-flex justify-content-between align-items-center mb-4">
            <div>
              <h1 className="page-title">Leave Management</h1>
              <p className="page-subtitle text-muted">
                Review and manage employee leave requests
              </p>
            </div>
          </div>
        </div>

        <Card className="leave-management-card">
          <Card.Body>
            <Tabs
              activeKey={activeTab}
              onSelect={(tab) => setActiveTab(tab)}
              className="leave-tabs"
            >
              <Tab 
                eventKey="Pending" 
                title={
                  <span>
                    <i className="fas fa-clock me-2"></i>
                    Pending
                    <Badge bg="warning" className="ms-2">{tabCounts.Pending}</Badge>
                  </span>
                }
              >
                <div className="tab-content-wrapper">
                  <div className="d-flex justify-content-between align-items-center mb-3">
                    <h5 className="tab-title">Pending Requests</h5>
                    <Button
                      variant="outline-secondary"
                      size="sm"
                      onClick={async () => { await fetchLeaveRequests(activeTab); showToast('Requests refreshed', 'success'); }}
                      disabled={loading}
                    >
                      <i className="fas fa-sync-alt me-2"></i>
                      Refresh
                    </Button>
                  </div>
                  {renderLeaveTable()}
                </div>
              </Tab>

              <Tab 
                eventKey="Approved" 
                title={
                  <span>
                    <i className="fas fa-check-circle me-2"></i>
                    Approved
                    <Badge bg="success" className="ms-2">{tabCounts.Approved}</Badge>
                  </span>
                }
              >
                <div className="tab-content-wrapper">
                  <div className="d-flex justify-content-between align-items-center mb-3">
                    <h5 className="tab-title">Approved Requests</h5>
                    <Button
                      variant="outline-secondary"
                      size="sm"
                      onClick={async () => { await fetchLeaveRequests(activeTab); showToast('Approved list refreshed', 'success'); }}
                      disabled={loading}
                    >
                      <i className="fas fa-sync-alt me-2"></i>
                      Refresh
                    </Button>
                  </div>
                  {renderLeaveTable()}
                </div>
              </Tab>

              <Tab 
                eventKey="Rejected" 
                title={
                  <span>
                    <i className="fas fa-times-circle me-2"></i>
                    Rejected
                    <Badge bg="danger" className="ms-2">{tabCounts.Rejected}</Badge>
                  </span>
                }
              >
                <div className="tab-content-wrapper">
                  <div className="d-flex justify-content-between align-items-center mb-3">
                    <h5 className="tab-title">Rejected Requests</h5>
                    <Button
                      variant="outline-secondary"
                      size="sm"
                      onClick={async () => { await fetchLeaveRequests(activeTab); showToast('Rejected list refreshed', 'success'); }}
                      disabled={loading}
                    >
                      <i className="fas fa-sync-alt me-2"></i>
                      Refresh
                    </Button>
                  </div>
                  {renderLeaveTable()}
                </div>
              </Tab>
            </Tabs>
          </Card.Body>
        </Card>

        {/* Leave Details Modal */}
        {selectedRequest && (
          <Modal show={showDetailsModal} onHide={() => setShowDetailsModal(false)} size="lg">
            <Modal.Header closeButton>
              <Modal.Title>
                <i className="fas fa-calendar-alt me-2"></i>
                Leave Request Details
              </Modal.Title>
            </Modal.Header>
            
            <Modal.Body>
              <div className="leave-details">
                <div className="row mb-4">
                  <div className="col-md-6">
                    <div className="detail-group">
                      <label className="detail-label">Employee</label>
                      <div className="detail-value">
                        {selectedRequest.employeeName || 'Unknown Employee'}
                        <small className="text-muted d-block">ID: {selectedRequest.employeeId}</small>
                      </div>
                    </div>
                  </div>
                  <div className="col-md-6">
                    <div className="detail-group">
                      <label className="detail-label">Leave Type</label>
                      <div className="detail-value">
                        {getLeaveTypeBadge(selectedRequest.leaveType)}
                      </div>
                    </div>
                  </div>
                  <div className="col-md-6">
                    <div className="detail-group">
                      <label className="detail-label">Start Date</label>
                      <div className="detail-value">{formatDate(selectedRequest.startDate)}</div>
                    </div>
                  </div>
                  <div className="col-md-6">
                    <div className="detail-group">
                      <label className="detail-label">End Date</label>
                      <div className="detail-value">{formatDate(selectedRequest.endDate)}</div>
                    </div>
                  </div>
                  <div className="col-md-6">
                    <div className="detail-group">
                      <label className="detail-label">Duration</label>
                      <div className="detail-value">
                        {calculateDuration(selectedRequest.startDate, selectedRequest.endDate)} day{calculateDuration(selectedRequest.startDate, selectedRequest.endDate) > 1 ? 's' : ''}
                      </div>
                    </div>
                  </div>
                  <div className="col-md-6">
                    <div className="detail-group">
                      <label className="detail-label">Status</label>
                      <div className="detail-value">
                        {getStatusBadge(selectedRequest.status)}
                      </div>
                    </div>
                  </div>
                </div>

                {selectedRequest.reason && (
                  <div className="row mb-4">
                    <div className="col-12">
                      <div className="detail-group">
                        <label className="detail-label">Reason</label>
                        <div className="detail-value reason-text">
                          {selectedRequest.reason}
                        </div>
                      </div>
                    </div>
                  </div>
                )}

                {selectedRequest.status === 'Pending' && (
                  <div className="row">
                    <div className="col-12">
                      <div className="detail-group">
                        <label className="detail-label">Action Reason (Optional)</label>
                        <Form.Control
                          as="textarea"
                          rows={3}
                          value={actionReason}
                          onChange={(e) => setActionReason(e.target.value)}
                          placeholder="Enter reason for approval/rejection (optional)..."
                        />
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </Modal.Body>
            
            <Modal.Footer>
              <Button
                variant="secondary"
                onClick={() => setShowDetailsModal(false)}
                disabled={updatingStatus}
              >
                Close
              </Button>
              
              {selectedRequest.status === 'Pending' && (
                <>
                  <Button
                    variant="danger"
                    onClick={() => handleStatusUpdate(selectedRequest.leaveId, 'Rejected', actionReason)}
                    disabled={updatingStatus}
                    className="me-2"
                  >
                    {updatingStatus ? <LoadingSpinner size="sm" /> : <i className="fas fa-times me-1"></i>}
                    Reject
                  </Button>
                  <Button
                    variant="success"
                    onClick={() => handleStatusUpdate(selectedRequest.leaveId, 'Approved', actionReason)}
                    disabled={updatingStatus}
                  >
                    {updatingStatus ? <LoadingSpinner size="sm" /> : <i className="fas fa-check me-1"></i>}
                    Approve
                  </Button>
                </>
              )}
            </Modal.Footer>
          </Modal>
        )}
      </Container>
    </div>
  );
};

export default LeaveManagementAdminPage;
