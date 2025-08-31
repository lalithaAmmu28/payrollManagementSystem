import React, { useState, useEffect } from 'react';
import { Modal, Button, Form, Alert, Card, ProgressBar } from 'react-bootstrap';
import { Formik } from 'formik';
import apiService from '../../api/apiService';
import { useToast } from '../common/ToastNotification';
import { LoadingSpinner } from '../common';
import { employeeCreateSchema } from '../../validation/employeeSchema';
import './styles/AddEmployeeModal.css';

const AddEmployeeModal = ({ show, onHide, onEmployeeAdded }) => {
  const { showToast } = useToast();
  const [currentStep, setCurrentStep] = useState(1);
  const [departments, setDepartments] = useState([]);
  const [jobRoles, setJobRoles] = useState([]);
  const [loading, setLoading] = useState(false);

  const totalSteps = 3;

  useEffect(() => {
    if (show) {
      fetchDepartmentsAndJobs();
      setCurrentStep(1); // Reset to first step when modal opens
    }
  }, [show]);

  const fetchDepartmentsAndJobs = async () => {
    try {
      setLoading(true);
      const [deptResponse, jobResponse] = await Promise.all([
        apiService.get('/departments'),
        apiService.get('/jobs')
      ]);
      setDepartments(deptResponse.data.data);
      setJobRoles(jobResponse.data.data);
    } catch (error) {
      showToast('Failed to fetch departments and job roles', 'error');
      console.error('Fetch error:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (values, { setSubmitting, resetForm }) => {
    try {
      const response = await apiService.post('/employees', values);
      onEmployeeAdded(response.data.data);
      resetForm();
      setCurrentStep(1);
      onHide();
    } catch (error) {
      if (error.response?.data?.message) {
        showToast(error.response.data.message, 'error');
      } else {
        showToast('Failed to create employee', 'error');
      }
      console.error('Create employee error:', error);
    } finally {
      setSubmitting(false);
    }
  };

  const nextStep = () => {
    if (currentStep < totalSteps) {
      setCurrentStep(currentStep + 1);
    }
  };

  const prevStep = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
    }
  };

  const getStepTitle = (step) => {
    const titles = {
      1: 'User Account Details',
      2: 'Personal Information',
      3: 'Employment Details'
    };
    return titles[step];
  };

  const validateCurrentStep = (values, errors) => {
    const stepFields = {
      1: ['username', 'email', 'password'],
      2: ['firstName', 'lastName', 'dateOfBirth', 'phone', 'address'],
      3: ['jobId', 'departmentId', 'leaveBalance']
    };

    const currentStepFields = stepFields[currentStep];
    return currentStepFields.some(field => errors[field]);
  };

  const renderStepContent = (values, errors, touched, handleChange, handleBlur) => {
    switch (currentStep) {
      case 1:
        return (
          <div className="step-content">
            <div className="step-header">
              <h5>Create User Account</h5>
              <p className="text-muted">Set up login credentials for the new employee</p>
            </div>
            
            <Form.Group className="mb-3">
              <Form.Label>Username *</Form.Label>
              <Form.Control
                type="text"
                name="username"
                value={values.username}
                onChange={handleChange}
                onBlur={handleBlur}
                placeholder="Enter username"
                isInvalid={touched.username && errors.username}
              />
              <Form.Control.Feedback type="invalid">
                {errors.username}
              </Form.Control.Feedback>
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Email Address *</Form.Label>
              <Form.Control
                type="email"
                name="email"
                value={values.email}
                onChange={handleChange}
                onBlur={handleBlur}
                placeholder="Enter email address"
                isInvalid={touched.email && errors.email}
              />
              <Form.Control.Feedback type="invalid">
                {errors.email}
              </Form.Control.Feedback>
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Password *</Form.Label>
              <Form.Control
                type="password"
                name="password"
                value={values.password}
                onChange={handleChange}
                onBlur={handleBlur}
                placeholder="Enter password"
                isInvalid={touched.password && errors.password}
              />
              <Form.Control.Feedback type="invalid">
                {errors.password}
              </Form.Control.Feedback>
              <Form.Text className="text-muted">
                Password must be at least 6 characters long
              </Form.Text>
            </Form.Group>
          </div>
        );

      case 2:
        return (
          <div className="step-content">
            <div className="step-header">
              <h5>Personal Information</h5>
              <p className="text-muted">Enter the employee's personal details</p>
            </div>
            
            <div className="row">
              <div className="col-md-6">
                <Form.Group className="mb-3">
                  <Form.Label>First Name *</Form.Label>
                  <Form.Control
                    type="text"
                    name="firstName"
                    value={values.firstName}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="Enter first name"
                    isInvalid={touched.firstName && errors.firstName}
                  />
                  <Form.Control.Feedback type="invalid">
                    {errors.firstName}
                  </Form.Control.Feedback>
                </Form.Group>
              </div>
              <div className="col-md-6">
                <Form.Group className="mb-3">
                  <Form.Label>Last Name *</Form.Label>
                  <Form.Control
                    type="text"
                    name="lastName"
                    value={values.lastName}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="Enter last name"
                    isInvalid={touched.lastName && errors.lastName}
                  />
                  <Form.Control.Feedback type="invalid">
                    {errors.lastName}
                  </Form.Control.Feedback>
                </Form.Group>
              </div>
            </div>

            <div className="row">
              <div className="col-md-6">
                <Form.Group className="mb-3">
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
              </div>
              <div className="col-md-6">
                <Form.Group className="mb-3">
                  <Form.Label>Phone Number *</Form.Label>
                  <Form.Control
                    type="text"
                    name="phone"
                    value={values.phone}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    placeholder="Enter phone number"
                    isInvalid={touched.phone && errors.phone}
                  />
                  <Form.Control.Feedback type="invalid">
                    {errors.phone}
                  </Form.Control.Feedback>
                </Form.Group>
              </div>
            </div>

            <Form.Group className="mb-3">
              <Form.Label>Address *</Form.Label>
              <Form.Control
                as="textarea"
                rows={3}
                name="address"
                value={values.address}
                onChange={handleChange}
                onBlur={handleBlur}
                placeholder="Enter full address"
                isInvalid={touched.address && errors.address}
              />
              <Form.Control.Feedback type="invalid">
                {errors.address}
              </Form.Control.Feedback>
            </Form.Group>
          </div>
        );

      case 3:
        return (
          <div className="step-content">
            <div className="step-header">
              <h5>Employment Details</h5>
              <p className="text-muted">Assign department, role, and leave balance</p>
            </div>
            
            <div className="row">
              <div className="col-md-6">
                <Form.Group className="mb-3">
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
              </div>
              <div className="col-md-6">
                <Form.Group className="mb-3">
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
                        {job.jobTitle} - ₹{job.baseSalary.toLocaleString()}
                      </option>
                    ))}
                  </Form.Select>
                  <Form.Control.Feedback type="invalid">
                    {errors.jobId}
                  </Form.Control.Feedback>
                </Form.Group>
              </div>
            </div>

            <Form.Group className="mb-3">
              <Form.Label>Initial Leave Balance *</Form.Label>
              <Form.Control
                type="number"
                name="leaveBalance"
                value={values.leaveBalance}
                onChange={handleChange}
                onBlur={handleBlur}
                min="0"
                max="365"
                placeholder="Enter leave balance (days)"
                isInvalid={touched.leaveBalance && errors.leaveBalance}
              />
              <Form.Control.Feedback type="invalid">
                {errors.leaveBalance}
              </Form.Control.Feedback>
              <Form.Text className="text-muted">
                Number of leave days available to the employee
              </Form.Text>
            </Form.Group>

            <Alert variant="info">
              <i className="fas fa-info-circle me-2"></i>
              Please review all the information before creating the employee account.
            </Alert>
          </div>
        );

      default:
        return null;
    }
  };

  const initialValues = {
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    dateOfBirth: '',
    phone: '',
    address: '',
    jobId: '',
    departmentId: '',
    leaveBalance: 20
  };

  return (
    <Modal show={show} onHide={onHide} size="lg" backdrop="static">
      <Modal.Header closeButton>
        <Modal.Title>
          <i className="fas fa-user-plus me-2"></i>
          Add New Employee
        </Modal.Title>
      </Modal.Header>

      <Formik
        initialValues={initialValues}
        validationSchema={employeeCreateSchema}
        onSubmit={handleSubmit}
        enableReinitialize
      >
        {({ values, errors, touched, handleChange, handleBlur, handleSubmit, isSubmitting }) => (
          <Form onSubmit={handleSubmit}>
            <Modal.Body>
              {/* Progress Bar */}
              <div className="step-progress mb-4">
                <div className="d-flex justify-content-between align-items-center mb-2">
                  <span className="step-indicator">
                    Step {currentStep} of {totalSteps}: {getStepTitle(currentStep)}
                  </span>
                  <span className="progress-text">
                    {Math.round((currentStep / totalSteps) * 100)}% Complete
                  </span>
                </div>
                <ProgressBar 
                  now={(currentStep / totalSteps) * 100} 
                  className="custom-progress"
                />
              </div>

              {loading ? (
                <div className="text-center py-5">
                  <LoadingSpinner />
                  <p className="text-muted mt-3">Loading departments and job roles...</p>
                </div>
              ) : (
                renderStepContent(values, errors, touched, handleChange, handleBlur)
              )}
            </Modal.Body>

            <Modal.Footer>
              <div className="d-flex justify-content-between w-100">
                <div>
                  {currentStep > 1 && (
                    <Button
                      variant="outline-secondary"
                      onClick={prevStep}
                      disabled={isSubmitting}
                    >
                      <i className="fas fa-arrow-left me-2"></i>
                      Previous
                    </Button>
                  )}
                </div>
                
                <div>
                  <Button
                    variant="secondary"
                    onClick={onHide}
                    disabled={isSubmitting}
                    className="me-2"
                  >
                    Cancel
                  </Button>
                  
                  {currentStep < totalSteps ? (
                    <Button
                      variant="primary"
                      onClick={nextStep}
                      disabled={validateCurrentStep(values, errors) || loading}
                    >
                      Next
                      <i className="fas fa-arrow-right ms-2"></i>
                    </Button>
                  ) : (
                    <Button
                      type="submit"
                      variant="success"
                      disabled={isSubmitting || Object.keys(errors).length > 0}
                    >
                      {isSubmitting ? <LoadingSpinner size="sm" /> : <i className="fas fa-user-plus me-2"></i>}
                      Create Employee
                    </Button>
                  )}
                </div>
              </div>
            </Modal.Footer>
          </Form>
        )}
      </Formik>
    </Modal>
  );
};

export default AddEmployeeModal;
