import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Table, Badge, Modal } from 'react-bootstrap';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import { useToast } from '../../components/common/ToastNotification';
import { LoadingSpinner, ConfirmationModal } from '../../components/common';
import { apiService } from '../../api';
import './styles/LeaveRequestPage.css';

const LeaveRequestPage = () => {
  const { showToast } = useToast();
  const [leaveBalance, setLeaveBalance] = useState(null);
  const [leaveRequests, setLeaveRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showApplicationForm, setShowApplicationForm] = useState(false);
  const [confirmCancel, setConfirmCancel] = useState(null);

  // Leave request validation schema
  const leaveValidationSchema = Yup.object({
    leaveType: Yup.string().required('Leave type is required'),
    startDate: Yup.date()
      .required('Start date is required')
      .min(new Date(), 'Start date cannot be in the past'),
    endDate: Yup.date()
      .required('End date is required')
      .min(Yup.ref('startDate'), 'End date must be after start date'),
    reason: Yup.string()
      .min(10, 'Reason must be at least 10 characters')
      .required('Reason is required')
  });

  useEffect(() => {
    fetchLeaveData();
  }, []);

  const fetchLeaveData = async () => {
    try {
      setLoading(true);
      // Fetch both leave balance and leave requests
      const [balanceResponse, requestsResponse] = await Promise.all([
        apiService.get('/users/me'), // Assuming leave balance is in user profile
        apiService.get('/leave-requests/my')
      ]);
      
      setLeaveBalance(balanceResponse.data.data.leaveBalance || 0);
      setLeaveRequests(requestsResponse.data.data || []);
    } catch (error) {
      showToast('Failed to load leave data', 'error');
      console.error('Leave data fetch error:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleLeaveApplication = async (values, { resetForm, setSubmitting }) => {
    try {
      const response = await apiService.post('/leave-requests', {
        leaveType: values.leaveType,
        startDate: values.startDate,
        endDate: values.endDate,
        reason: values.reason
      });
      
      // Add new request to the list
      setLeaveRequests(prev => [response.data.data, ...prev]);
      
      // Close modal first, then show toast
      setShowApplicationForm(false);
      resetForm();
      
      // Show success toast after modal is closed
      setTimeout(() => {
        showToast('Leave request submitted successfully!', 'success');
      }, 100);
      
    } catch (error) {
      showToast('Failed to submit leave request', 'error');
      console.error('Leave application error:', error);
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancelRequest = async (requestId) => {
    try {
      await apiService.delete(`/leave-requests/${requestId}`);
      setLeaveRequests(prev => prev.filter(req => req.leaveId !== requestId));
      showToast('Leave request cancelled successfully!', 'success');
    } catch (error) {
      showToast('Failed to cancel leave request', 'error');
      console.error('Leave cancellation error:', error);
    } finally {
      setConfirmCancel(null);
    }
  };

  const getStatusBadge = (status) => {
    const statusConfig = {
      'Pending': { variant: 'warning', icon: 'clock' },
      'Approved': { variant: 'success', icon: 'check-circle' },
      'Rejected': { variant: 'danger', icon: 'times-circle' }
    };
    
    const config = statusConfig[status] || { variant: 'secondary', icon: 'question-circle' };
    
    return (
      <Badge bg={config.variant} className="status-badge">
        <i className={`fas fa-${config.icon} me-1`}></i>
        {status}
      </Badge>
    );
  };

  const calculateDays = (startDate, endDate) => {
    const start = new Date(startDate);
    const end = new Date(endDate);
    const diffTime = Math.abs(end - start);
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
  };

  if (loading) {
    return (
      <Container className="leave-page">
        <div className="d-flex justify-content-center align-items-center" style={{ height: '400px' }}>
          <LoadingSpinner />
        </div>
      </Container>
    );
  }

  return (
    <Container className="leave-page">
      <div className="page-header">
        <h1>Leave Management</h1>
        <p className="text-muted">Apply for leave and track your requests</p>
      </div>

      {/* Leave Balance Card */}
      <Row className="mb-4">
        <Col lg={4}>
          <Card className="leave-balance-card">
            <Card.Body className="text-center">
              <div className="balance-icon">
                <i className="fas fa-calendar-check"></i>
              </div>
              <h3 className="balance-number">{leaveBalance}</h3>
              <p className="balance-label">Available Leave Days</p>
            </Card.Body>
          </Card>
        </Col>
        <Col lg={8} className="d-flex align-items-center">
          <div className="quick-actions">
            <Button
              variant="primary"
              size="lg"
              onClick={() => setShowApplicationForm(true)}
              className="apply-leave-btn"
            >
              <i className="fas fa-plus me-2"></i>
              Apply for Leave
            </Button>
          </div>
        </Col>
      </Row>

      {/* Leave Requests History */}
      <Card className="leave-history-card">
        <Card.Header>
          <h3>Leave Request History</h3>
        </Card.Header>
        <Card.Body>
          {leaveRequests.length === 0 ? (
            <div className="empty-state">
              <i className="fas fa-calendar-alt"></i>
              <h4>No Leave Requests</h4>
              <p>You haven't submitted any leave requests yet.</p>
              <Button
                variant="primary"
                onClick={() => setShowApplicationForm(true)}
              >
                Apply for Your First Leave
              </Button>
            </div>
          ) : (
            <Table responsive hover className="leave-table">
              <thead>
                <tr>
                  <th>Leave Type</th>
                  <th>Start Date</th>
                  <th>End Date</th>
                  <th>Days</th>
                  <th>Status</th>
                  <th>Reason</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {leaveRequests.map((request) => (
                  <tr key={request.leaveId}>
                    <td>
                      <span className="leave-type">{request.leaveType}</span>
                    </td>
                    <td>{new Date(request.startDate).toLocaleDateString()}</td>
                    <td>{new Date(request.endDate).toLocaleDateString()}</td>
                    <td>
                      <span className="days-count">
                        {calculateDays(request.startDate, request.endDate)}
                      </span>
                    </td>
                    <td>{getStatusBadge(request.status)}</td>
                    <td>
                      <span className="reason-text" title={request.reason}>
                        {request.reason.length > 50 ? 
                          `${request.reason.substring(0, 50)}...` : 
                          request.reason
                        }
                      </span>
                    </td>
                    <td>
                      {request.status === 'Pending' && (
                        <Button
                          variant="outline-danger"
                          size="sm"
                          onClick={() => setConfirmCancel(request)}
                        >
                          <i className="fas fa-times me-1"></i>
                          Cancel
                        </Button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          )}
        </Card.Body>
      </Card>

      {/* Leave Application Modal */}
      <Modal 
        show={showApplicationForm} 
        onHide={() => setShowApplicationForm(false)} 
        size="lg"
        centered
      >
        <Modal.Header closeButton>
          <Modal.Title>Apply for Leave</Modal.Title>
        </Modal.Header>
        
        <Formik
          initialValues={{
            leaveType: '',
            startDate: '',
            endDate: '',
            reason: ''
          }}
          validationSchema={leaveValidationSchema}
          onSubmit={handleLeaveApplication}
        >
          {({ values, isSubmitting }) => (
            <Form>
              <Modal.Body>
                <Row>
                  <Col md={6}>
                    <div className="form-group">
                      <label htmlFor="leaveType">Leave Type</label>
                      <Field
                        as="select"
                        name="leaveType"
                        className="form-control"
                      >
                        <option value="">Select leave type</option>
                        <option value="Sick">Sick Leave</option>
                        <option value="Casual">Casual Leave</option>
                        <option value="Paid">Paid Leave</option>
                      </Field>
                      <ErrorMessage name="leaveType" component="div" className="error-message" />
                    </div>
                  </Col>
                  <Col md={6}>
                    <div className="form-group">
                      <label>Duration</label>
                      <div className="duration-display">
                        {values.startDate && values.endDate && (
                          <span className="duration-badge">
                            {calculateDays(values.startDate, values.endDate)} days
                          </span>
                        )}
                      </div>
                    </div>
                  </Col>
                  <Col md={6}>
                    <div className="form-group">
                      <label htmlFor="startDate">Start Date</label>
                      <Field
                        type="date"
                        name="startDate"
                        className="form-control"
                        min={new Date().toISOString().split('T')[0]}
                      />
                      <ErrorMessage name="startDate" component="div" className="error-message" />
                    </div>
                  </Col>
                  <Col md={6}>
                    <div className="form-group">
                      <label htmlFor="endDate">End Date</label>
                      <Field
                        type="date"
                        name="endDate"
                        className="form-control"
                        min={values.startDate || new Date().toISOString().split('T')[0]}
                      />
                      <ErrorMessage name="endDate" component="div" className="error-message" />
                    </div>
                  </Col>
                  <Col md={12}>
                    <div className="form-group">
                      <label htmlFor="reason">Reason for Leave</label>
                      <Field
                        as="textarea"
                        name="reason"
                        className="form-control"
                        rows="4"
                        placeholder="Please provide the reason for your leave request..."
                      />
                      <ErrorMessage name="reason" component="div" className="error-message" />
                    </div>
                  </Col>
                </Row>
              </Modal.Body>
              
              <Modal.Footer>
                <Button
                  variant="secondary"
                  onClick={() => setShowApplicationForm(false)}
                  disabled={isSubmitting}
                >
                  Cancel
                </Button>
                <Button
                  type="submit"
                  variant="primary"
                  disabled={isSubmitting}
                >
                  {isSubmitting ? <LoadingSpinner size="sm" /> : <i className="fas fa-paper-plane me-1"></i>}
                  Submit Request
                </Button>
              </Modal.Footer>
            </Form>
          )}
        </Formik>
      </Modal>

      {/* Confirmation Modal for Cancellation */}
      <ConfirmationModal
        show={!!confirmCancel}
        onHide={() => setConfirmCancel(null)}
        onConfirm={() => handleCancelRequest(confirmCancel?.leaveId)}
        title="Cancel Leave Request"
        message={`Are you sure you want to cancel your ${confirmCancel?.leaveType} leave request from ${confirmCancel ? new Date(confirmCancel.startDate).toLocaleDateString() : ''} to ${confirmCancel ? new Date(confirmCancel.endDate).toLocaleDateString() : ''}?`}
        confirmText="Cancel Request"
        confirmVariant="danger"
      />
    </Container>
  );
};

export default LeaveRequestPage;
