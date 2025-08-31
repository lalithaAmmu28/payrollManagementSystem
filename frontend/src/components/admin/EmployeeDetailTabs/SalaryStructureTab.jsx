import React, { useState, useEffect } from 'react';
import { Button, Modal, Form, Alert, Badge } from 'react-bootstrap';
import { Formik } from 'formik';
import apiService from '../../../api/apiService';
import { useToast } from '../../common/ToastNotification';
import { LoadingSpinner, SkeletonLoader } from '../../common';
import { salaryStructureSchema } from '../../../validation/employeeSchema';
import '../styles/EmployeeDetailTabs.css';

const SalaryStructureTab = ({ employeeId }) => {
  const { showToast } = useToast();
  const [salaryStructures, setSalaryStructures] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAddModal, setShowAddModal] = useState(false);

  useEffect(() => {
    fetchSalaryStructures();
  }, [employeeId]);

  const fetchSalaryStructures = async () => {
    try {
      setLoading(true);
      const response = await apiService.get(`/employees/${employeeId}/salary-structures`);
      setSalaryStructures(response.data.data);
    } catch (error) {
      showToast('Failed to fetch salary structures', 'error');
      console.error('Fetch salary structures error:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAddSalaryStructure = async (values, { setSubmitting, resetForm }) => {
    try {
      // Convert bonus type and amount to JSON format
      const payload = {
        baseSalary: values.baseSalary,
        effectiveFrom: values.effectiveFrom,
        effectiveTo: values.effectiveTo || null
      };

      // Add bonus details as JSON if both type and amount are provided
      if (values.bonusType && values.bonusAmount) {
        if (values.bonusType === 'percentage') {
          payload.bonusDetails = { percentage: parseFloat(values.bonusAmount) };
        } else if (values.bonusType === 'fixed') {
          payload.bonusDetails = { fixed: parseFloat(values.bonusAmount) };
        }
      }

      const response = await apiService.post(`/employees/${employeeId}/salary-structures`, payload);
      setSalaryStructures(prev => [response.data.data, ...prev]);
      setShowAddModal(false);
      resetForm();
      showToast('Salary structure added successfully!', 'success');
    } catch (error) {
      showToast('Failed to add salary structure', 'error');
      console.error('Add salary structure error:', error);
    } finally {
      setSubmitting(false);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(amount);
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'Present';
    return new Date(dateString).toLocaleDateString();
  };

  const isCurrentStructure = (structure) => {
    const today = new Date();
    const effectiveFrom = new Date(structure.effectiveFrom);
    const effectiveTo = structure.effectiveTo ? new Date(structure.effectiveTo) : null;
    
    return effectiveFrom <= today && (!effectiveTo || effectiveTo >= today);
  };

  if (loading) {
    return (
      <div className="salary-structure-tab">
        <div className="d-flex justify-content-between align-items-center mb-4">
          <SkeletonLoader width="150px" height="24px" />
          <SkeletonLoader width="120px" height="32px" />
        </div>
        <div className="timeline">
          {[...Array(3)].map((_, index) => (
            <div key={index} className="timeline-item">
              <SkeletonLoader height="120px" className="mb-3" />
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="salary-structure-tab">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h6 className="tab-title">Salary Structure Timeline</h6>
        <Button
          variant="primary"
          size="sm"
          onClick={() => setShowAddModal(true)}
          className="add-structure-btn"
        >
          <i className="fas fa-plus me-2"></i>
          Add Structure
        </Button>
      </div>

      {salaryStructures.length === 0 ? (
        <div className="no-structures">
          <div className="text-center py-5">
            <i className="fas fa-money-bill-wave fa-3x text-muted mb-3"></i>
            <h6 className="text-muted">No Salary Structures Found</h6>
            <p className="text-muted mb-4">
              This employee doesn't have any salary structures assigned yet.
            </p>
            <Button
              variant="primary"
              onClick={() => setShowAddModal(true)}
            >
              <i className="fas fa-plus me-2"></i>
              Add First Structure
            </Button>
          </div>
        </div>
      ) : (
        <div className="salary-timeline">
          {salaryStructures.map((structure, index) => (
            <div key={structure.structureId} className="timeline-item">
              <div className="timeline-marker">
                <div className={`${isCurrentStructure(structure) ? 'active' : ''}`}>
                  <i className="fas"></i>
                </div>
                {index < salaryStructures.length - 1 && <div className="timeline-line"></div>}
              </div>
              
              <div className="timeline-content">
                <div className="structure-card">
                  <div className="structure-header">
                    <div className="structure-amount">
                      {formatCurrency(structure.baseSalary)}
                      {isCurrentStructure(structure) && (
                        <Badge bg="success" className="ms-2">Current</Badge>
                      )}
                    </div>
                    <div className="structure-period">
                      {formatDate(structure.effectiveFrom)} - {formatDate(structure.effectiveTo)}
                    </div>
                  </div>
                  
                  {structure.bonusDetails && (
                    <div className="structure-bonus">
                      <small className="text-muted">Bonus Details:</small>
                      <div className="bonus-text">
                        {typeof structure.bonusDetails === 'string' 
                          ? structure.bonusDetails 
                          : JSON.stringify(structure.bonusDetails)
                        }
                      </div>
                    </div>
                  )}
                  
                  <div className="structure-meta">
                    <small className="text-muted">
                      Added on {new Date(structure.createdAt).toLocaleDateString()}
                    </small>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Add Salary Structure Modal */}
      <Modal show={showAddModal} onHide={() => setShowAddModal(false)} size="lg">
        <Modal.Header closeButton>
          <Modal.Title>Add New Salary Structure</Modal.Title>
        </Modal.Header>
        
        <Formik
          initialValues={{
            baseSalary: '',
            bonusType: '',
            bonusAmount: '',
            effectiveFrom: '',
            effectiveTo: ''
          }}
          validationSchema={salaryStructureSchema}
          onSubmit={handleAddSalaryStructure}
        >
          {({ handleSubmit, isSubmitting, values, handleChange, handleBlur, errors, touched }) => (
            <Form onSubmit={handleSubmit}>
              <Modal.Body>
                <div className="row">
                  <div className="col-md-6 mb-3">
                    <Form.Group>
                      <Form.Label>Base Salary (₹) *</Form.Label>
                      <Form.Control
                        type="number"
                        name="baseSalary"
                        value={values.baseSalary}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        placeholder="Enter base salary"
                        isInvalid={touched.baseSalary && errors.baseSalary}
                      />
                      <Form.Control.Feedback type="invalid">
                        {errors.baseSalary}
                      </Form.Control.Feedback>
                    </Form.Group>
                  </div>
                  
                  <div className="col-md-6 mb-3">
                    <Form.Group>
                      <Form.Label>Effective From *</Form.Label>
                      <Form.Control
                        type="date"
                        name="effectiveFrom"
                        value={values.effectiveFrom}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        isInvalid={touched.effectiveFrom && errors.effectiveFrom}
                      />
                      <Form.Control.Feedback type="invalid">
                        {errors.effectiveFrom}
                      </Form.Control.Feedback>
                    </Form.Group>
                  </div>
                  
                  <div className="col-md-6 mb-3">
                    <Form.Group>
                      <Form.Label>Effective To</Form.Label>
                      <Form.Control
                        type="date"
                        name="effectiveTo"
                        value={values.effectiveTo}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        isInvalid={touched.effectiveTo && errors.effectiveTo}
                      />
                      <Form.Control.Feedback type="invalid">
                        {errors.effectiveTo}
                      </Form.Control.Feedback>
                      <Form.Text className="text-muted">
                        Leave blank if this is an ongoing structure
                      </Form.Text>
                    </Form.Group>
                  </div>
                  
                  <div className="col-md-6 mb-3">
                    <Form.Group>
                      <Form.Label>Bonus Type</Form.Label>
                      <Form.Select
                        name="bonusType"
                        value={values.bonusType}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        isInvalid={touched.bonusType && errors.bonusType}
                      >
                        <option value="">Select Bonus Type</option>
                        <option value="percentage">Percentage</option>
                        <option value="fixed">Fixed Amount</option>
                      </Form.Select>
                      <Form.Control.Feedback type="invalid">
                        {errors.bonusType}
                      </Form.Control.Feedback>
                    </Form.Group>
                  </div>
                  
                  <div className="col-md-6 mb-3">
                    <Form.Group>
                      <Form.Label>Bonus Value</Form.Label>
                      <Form.Control
                        type="number"
                        name="bonusAmount"
                        value={values.bonusAmount}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        placeholder={values.bonusType === 'percentage' ? 'Enter percentage (e.g., 10)' : 'Enter amount (e.g., 5000)'}
                        isInvalid={touched.bonusAmount && errors.bonusAmount}
                        disabled={!values.bonusType}
                      />
                      <Form.Control.Feedback type="invalid">
                        {errors.bonusAmount}
                      </Form.Control.Feedback>
                      <Form.Text className="text-muted">
                        {values.bonusType === 'percentage' ? 'Enter percentage value (without % sign)' : 'Enter fixed amount in rupees'}
                      </Form.Text>
                    </Form.Group>
                  </div>
                </div>
              </Modal.Body>
              
              <Modal.Footer>
                <Button
                  variant="secondary"
                  onClick={() => setShowAddModal(false)}
                  disabled={isSubmitting}
                >
                  Cancel
                </Button>
                <Button
                  type="submit"
                  variant="primary"
                  disabled={isSubmitting}
                >
                  {isSubmitting ? <LoadingSpinner size="sm" /> : <i className="fas fa-save me-2"></i>}
                  Add Structure
                </Button>
              </Modal.Footer>
            </Form>
          )}
        </Formik>
      </Modal>
    </div>
  );
};

export default SalaryStructureTab;
