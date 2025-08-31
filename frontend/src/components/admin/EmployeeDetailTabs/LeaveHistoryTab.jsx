import React, { useState, useEffect } from 'react';
import { Table, Badge, Button, Modal, Alert } from 'react-bootstrap';
import apiService from '../../../api/apiService';
import { useToast } from '../../common/ToastNotification';
import { SkeletonLoader, LoadingSpinner } from '../../common';
import '../styles/EmployeeDetailTabs.css';

const LeaveHistoryTab = ({ employeeId }) => {
  const { showToast } = useToast();
  const [leaveRequests, setLeaveRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [updatingStatus, setUpdatingStatus] = useState(false);

  useEffect(() => {
    fetchLeaveHistory();
  }, [employeeId]);

  const fetchLeaveHistory = async () => {
    try {
      setLoading(true);
      // Fetch all leave requests and filter by employee
      const response = await apiService.get('/leave-requests');
      const employeeLeaves = response.data.data.filter(
        request => request.employeeId === employeeId
      );
      setLeaveRequests(employeeLeaves);
    } catch (error) {
      showToast('Failed to fetch leave history', 'error');
      console.error('Fetch leave history error:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleStatusUpdate = async (leaveId, newStatus) => {
    try {
      setUpdatingStatus(true);
      await apiService.patch(`/leave-requests/${leaveId}/status`, {
        status: newStatus
      });
      
      // Update local state
      setLeaveRequests(prev => 
        prev.map(request => 
          request.leaveId === leaveId 
            ? { ...request, status: newStatus }
            : request
        )
      );
      
      if (selectedRequest?.leaveId === leaveId) {
        setSelectedRequest(prev => ({ ...prev, status: newStatus }));
      }
      
      setShowDetailsModal(false);
      showToast(`Leave request ${newStatus.toLowerCase()} successfully!`, 'success');
    } catch (error) {
      showToast('Failed to update leave status', 'error');
      console.error('Update leave status error:', error);
    } finally {
      setUpdatingStatus(false);
    }
  };

  const getStatusBadge = (status) => {
    const statusConfig = {
      'PENDING': { bg: 'warning', text: 'Pending' },
      'APPROVED': { bg: 'success', text: 'Approved' },
      'REJECTED': { bg: 'danger', text: 'Rejected' }
    };
    
    const config = statusConfig[status] || { bg: 'secondary', text: status };
    return <Badge bg={config.bg}>{config.text}</Badge>;
  };

  const getLeaveTypeBadge = (type) => {
    const typeConfig = {
      'SICK': { bg: 'info', icon: 'fas fa-thermometer-half' },
      'CASUAL': { bg: 'primary', icon: 'fas fa-coffee' },
      'PAID': { bg: 'success', icon: 'fas fa-umbrella-beach' }
    };
    
    const config = typeConfig[type] || { bg: 'secondary', icon: 'fas fa-calendar' };
    return (
      <Badge bg={config.bg}>
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
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1; // +1 to include both start and end dates
    return diffDays;
  };

  const openDetailsModal = (request) => {
    setSelectedRequest(request);
    setShowDetailsModal(true);
  };

  if (loading) {
    return (
      <div className="leave-history-tab">
        <div className="mb-4">
          <h6 className="tab-title">Leave History</h6>
        </div>
        <div className="table-skeleton">
          <SkeletonLoader height="40px" className="mb-2" />
          {[...Array(5)].map((_, index) => (
            <SkeletonLoader key={index} height="50px" className="mb-2" />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="leave-history-tab">
      <div className="mb-4">
        <h6 className="tab-title">Leave History</h6>
        <p className="text-muted">
          View and manage all leave requests for this employee.
        </p>
      </div>

      {leaveRequests.length === 0 ? (
        <div className="no-leave-history">
          <div className="text-center py-5">
            <i className="fas fa-calendar-times fa-3x text-muted mb-3"></i>
            <h6 className="text-muted">No Leave History</h6>
            <p className="text-muted">
              This employee hasn't submitted any leave requests yet.
            </p>
          </div>
        </div>
      ) : (
        <div className="leave-table-container">
          <Table hover responsive className="leave-history-table">
            <thead>
              <tr>
                <th>Type</th>
                <th>Duration</th>
                <th>Dates</th>
                <th>Status</th>
                <th>Applied On</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {leaveRequests.map((request) => (
                <tr key={request.leaveId} className="leave-row">
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
                  <td>{getStatusBadge(request.status)}</td>
                  <td>
                    <small className="text-muted">
                      {formatDate(request.createdAt)}
                    </small>
                  </td>
                  <td>
                    <Button
                      variant="outline-primary"
                      size="sm"
                      onClick={() => openDetailsModal(request)}
                      className="view-details-btn"
                    >
                      <i className="fas fa-eye me-1"></i>
                      Details
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        </div>
      )}

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
                    <label className="detail-label">Leave Type</label>
                    <div className="detail-value">
                      {getLeaveTypeBadge(selectedRequest.leaveType)}
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
                    <label className="detail-label">Applied On</label>
                    <div className="detail-value">{formatDate(selectedRequest.createdAt)}</div>
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

              {selectedRequest.status === 'PENDING' && (
                <Alert variant="info">
                  <i className="fas fa-info-circle me-2"></i>
                  You can approve or reject this leave request using the buttons below.
                </Alert>
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
            
            {selectedRequest.status === 'PENDING' && (
              <>
                <Button
                  variant="danger"
                  onClick={() => handleStatusUpdate(selectedRequest.leaveId, 'REJECTED')}
                  disabled={updatingStatus}
                  className="me-2"
                >
                  {updatingStatus ? <LoadingSpinner size="sm" /> : <i className="fas fa-times me-1"></i>}
                  Reject
                </Button>
                <Button
                  variant="success"
                  onClick={() => handleStatusUpdate(selectedRequest.leaveId, 'APPROVED')}
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
    </div>
  );
};

export default LeaveHistoryTab;
