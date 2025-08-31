import React, { useState, useEffect } from 'react';
import { Form, Button, Row, Col, Alert } from 'react-bootstrap';
import { Formik, Field, ErrorMessage } from 'formik';
import apiService from '../../../api/apiService';
import { useToast } from '../../common/ToastNotification';
import { LoadingSpinner } from '../../common';
import { employeeUpdateSchema } from '../../../validation/employeeSchema';
import '../styles/EmployeeDetailTabs.css';

const ProfileTab = ({ employee, onEmployeeUpdated }) => {
  const { showToast } = useToast();
  const [isEditing, setIsEditing] = useState(false);
  const [departments, setDepartments] = useState([]);
  const [jobRoles, setJobRoles] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchDepartmentsAndJobs();
  }, []);

  const fetchDepartmentsAndJobs = async () => {
    try {
      const [deptResponse, jobResponse] = await Promise.all([
        apiService.get('/departments'),
        apiService.get('/jobs')
      ]);
      setDepartments(deptResponse.data.data);
      setJobRoles(jobResponse.data.data);
    } catch (error) {
      showToast('Failed to fetch departments and job roles', 'error');
      console.error('Fetch error:', error);
    }
  };

  const handleSubmit = async (values, { setSubmitting }) => {
    try {
      const response = await apiService.put(`/employees/${employee.employeeId}`, values);
      onEmployeeUpdated(response.data.data);
      setIsEditing(false);
      showToast('Employee profile updated successfully!', 'success');
    } catch (error) {
      showToast('Failed to update employee profile', 'error');
      console.error('Update error:', error);
    } finally {
      setSubmitting(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).toISOString().split('T')[0];
  };

  const initialValues = {
    email: employee.email || '',
    firstName: employee.firstName || '',
    lastName: employee.lastName || '',
    dateOfBirth: formatDate(employee.dateOfBirth),
    phone: employee.phone || '',
    address: employee.address || '',
    jobId: employee.jobId || '',
    departmentId: employee.departmentId || '',
    leaveBalance: employee.leaveBalance || 0
  };

  return (
    <div className="profile-tab">
      {!isEditing ? (
        // View Mode
        <div className="profile-view">
          <div className="d-flex justify-content-between align-items-center mb-4">
            <h6 className="tab-title">Employee Profile</h6>
            <Button
              variant="outline-primary"
              size="sm"
              onClick={() => setIsEditing(true)}
              className="edit-btn"
            >
              <i className="fas fa-edit me-2"></i>
              Edit Profile
            </Button>
          </div>

          <Row className="profile-info">
            <Col md={6} className="mb-3">
              <div className="info-group">
                <label className="info-label">Email Address</label>
                <div className="info-value">{employee.email}</div>
              </div>
            </Col>
            <Col md={6} className="mb-3">
              <div className="info-group">
                <label className="info-label">First Name</label>
                <div className="info-value">{employee.firstName}</div>
              </div>
            </Col>
            <Col md={6} className="mb-3">
              <div className="info-group">
                <label className="info-label">Last Name</label>
                <div className="info-value">{employee.lastName}</div>
              </div>
            </Col>
            <Col md={6} className="mb-3">
              <div className="info-group">
                <label className="info-label">Date of Birth</label>
                <div className="info-value">
                  {employee.dateOfBirth 
                    ? new Date(employee.dateOfBirth).toLocaleDateString()
                    : 'Not specified'
                  }
                </div>
              </div>
            </Col>
            <Col md={6} className="mb-3">
              <div className="info-group">
                <label className="info-label">Phone Number</label>
                <div className="info-value">{employee.phone}</div>
              </div>
            </Col>
            <Col md={6} className="mb-3">
              <div className="info-group">
                <label className="info-label">Leave Balance</label>
                <div className="info-value">
                  <span className="leave-balance">{employee.leaveBalance} days</span>
                </div>
              </div>
            </Col>
            <Col md={12} className="mb-3">
              <div className="info-group">
                <label className="info-label">Address</label>
                <div className="info-value">{employee.address}</div>
              </div>
            </Col>
          </Row>
        </div>
      ) : (
        // Edit Mode
        <div className="profile-edit">
          <div className="d-flex justify-content-between align-items-center mb-4">
            <h6 className="tab-title">Edit Employee Profile</h6>
            <Button
              variant="outline-secondary"
              size="sm"
              onClick={() => setIsEditing(false)}
              className="cancel-btn"
            >
              <i className="fas fa-times me-2"></i>
              Cancel
            </Button>
          </div>

          <Formik
            initialValues={initialValues}
            validationSchema={employeeUpdateSchema}
            onSubmit={handleSubmit}
            enableReinitialize
          >
            {({ handleSubmit, isSubmitting, values, handleChange, handleBlur, errors, touched }) => (
              <Form onSubmit={handleSubmit}>
                <Row>
                  <Col md={6} className="mb-3">
                    <Form.Group>
                      <Form.Label>Email Address *</Form.Label>
                      <Form.Control
                        type="email"
                        name="email"
                        value={values.email}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        isInvalid={touched.email && errors.email}
                      />
                      <Form.Control.Feedback type="invalid">
                        {errors.email}
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                  <Col md={6} className="mb-3">
                    <Form.Group>
                      <Form.Label>First Name *</Form.Label>
                      <Form.Control
                        type="text"
                        name="firstName"
                        value={values.firstName}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        isInvalid={touched.firstName && errors.firstName}
                      />
                      <Form.Control.Feedback type="invalid">
                        {errors.firstName}
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                  <Col md={6} className="mb-3">
                    <Form.Group>
                      <Form.Label>Last Name *</Form.Label>
                      <Form.Control
                        type="text"
                        name="lastName"
                        value={values.lastName}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        isInvalid={touched.lastName && errors.lastName}
                      />
                      <Form.Control.Feedback type="invalid">
                        {errors.lastName}
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                  <Col md={6} className="mb-3">
                    <Form.Group>
                      <Form.Label>Date of Birth *</Form.Label>
                      <Form.Control
                        type="date"
                        name="dateOfBirth"
                        value={values.dateOfBirth}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        isInvalid={touched.dateOfBirth && errors.dateOfBirth}
                      />
                      <Form.Control.Feedback type="invalid">
                        {errors.dateOfBirth}
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                  <Col md={6} className="mb-3">
                    <Form.Group>
                      <Form.Label>Phone Number *</Form.Label>
                      <Form.Control
                        type="text"
                        name="phone"
                        value={values.phone}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        isInvalid={touched.phone && errors.phone}
                      />
                      <Form.Control.Feedback type="invalid">
                        {errors.phone}
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                  <Col md={6} className="mb-3">
                    <Form.Group>
                      <Form.Label>Leave Balance *</Form.Label>
                      <Form.Control
                        type="number"
                        name="leaveBalance"
                        value={values.leaveBalance}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        min="0"
                        max="365"
                        isInvalid={touched.leaveBalance && errors.leaveBalance}
                      />
                      <Form.Control.Feedback type="invalid">
                        {errors.leaveBalance}
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                  <Col md={6} className="mb-3">
                    <Form.Group>
                      <Form.Label>Department *</Form.Label>
                      <Form.Select
                        name="departmentId"
                        value={values.departmentId}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        isInvalid={touched.departmentId && errors.departmentId}
                      >
                        <option value="">Select Department</option>
                        {departments.map(dept => (
                          <option key={dept.departmentId} value={dept.departmentId}>
                            {dept.departmentName}
                          </option>
                        ))}
                      </Form.Select>
                      <Form.Control.Feedback type="invalid">
                        {errors.departmentId}
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                  <Col md={6} className="mb-3">
                    <Form.Group>
                      <Form.Label>Job Role *</Form.Label>
                      <Form.Select
                        name="jobId"
                        value={values.jobId}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        isInvalid={touched.jobId && errors.jobId}
                      >
                        <option value="">Select Job Role</option>
                        {jobRoles.map(job => (
                          <option key={job.jobId} value={job.jobId}>
                            {job.jobTitle}
                          </option>
                        ))}
                      </Form.Select>
                      <Form.Control.Feedback type="invalid">
                        {errors.jobId}
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                  <Col md={12} className="mb-3">
                    <Form.Group>
                      <Form.Label>Address *</Form.Label>
                      <Form.Control
                        as="textarea"
                        rows={3}
                        name="address"
                        value={values.address}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        isInvalid={touched.address && errors.address}
                      />
                      <Form.Control.Feedback type="invalid">
                        {errors.address}
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                </Row>

                <div className="form-actions">
                  <Button
                    type="submit"
                    variant="primary"
                    disabled={isSubmitting}
                    className="save-btn"
                  >
                    {isSubmitting ? <LoadingSpinner size="sm" /> : <i className="fas fa-save me-2"></i>}
                    Save Changes
                  </Button>
                </div>
              </Form>
            )}
          </Formik>
        </div>
      )}
    </div>
  );
};

export default ProfileTab;
