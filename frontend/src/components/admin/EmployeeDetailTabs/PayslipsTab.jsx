import React, { useState, useEffect } from 'react';
import { Accordion, Badge, Alert } from 'react-bootstrap';
import apiService from '../../../api/apiService';
import { useToast } from '../../common/ToastNotification';
import { SkeletonLoader } from '../../common';
import '../styles/EmployeeDetailTabs.css';

const PayslipsTab = ({ employeeId }) => {
  const { showToast } = useToast();
  const [payslips, setPayslips] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchPayslips();
  }, [employeeId]);

  const fetchPayslips = async () => {
    try {
      setLoading(true);
      const response = await apiService.get(`/payroll/employees/${employeeId}/payslips`);
      setPayslips(response.data.data);
    } catch (error) {
      showToast('Failed to fetch payslips', 'error');
      console.error('Fetch payslips error:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
    }).format(amount);
  };

  const formatPayPeriod = (month, year) => {
    const date = new Date(year, month - 1);
    return date.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
  };

  const getStatusBadge = (status) => {
    const statusConfig = {
      'PROCESSED': { bg: 'success', text: 'Processed' },
      'LOCKED': { bg: 'info', text: 'Locked' },
      'DRAFT': { bg: 'warning', text: 'Draft' }
    };
    
    const config = statusConfig[status] || { bg: 'secondary', text: status };
    return <Badge bg={config.bg}>{config.text}</Badge>;
  };

  if (loading) {
    return (
      <div className="payslips-tab">
        <div className="mb-4">
          <h6 className="tab-title">Employee Payslips</h6>
        </div>
        <div className="payslips-skeleton">
          {[...Array(4)].map((_, index) => (
            <div key={index} className="mb-3">
              <SkeletonLoader height="60px" />
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="payslips-tab">
      <div className="mb-4">
        <h6 className="tab-title">Employee Payslips</h6>
        <p className="text-muted">
          View historical payslips and salary details for this employee.
        </p>
      </div>

      {payslips.length === 0 ? (
        <div className="no-payslips">
          <div className="text-center py-5">
            <i className="fas fa-file-invoice-dollar fa-3x text-muted mb-3"></i>
            <h6 className="text-muted">No Payslips Found</h6>
            <p className="text-muted">
              This employee doesn't have any processed payslips yet.
            </p>
          </div>
        </div>
      ) : (
        <Accordion className="payslips-accordion">
          {payslips.map((payslip, index) => (
            <Accordion.Item eventKey={index.toString()} key={payslip.itemId} className="payslip-item">
              <Accordion.Header className="payslip-header">
                <div className="payslip-summary">
                  <div className="payslip-period">
                    <i className="fas fa-calendar-alt me-2"></i>
                    {formatPayPeriod(payslip.runMonth, payslip.runYear)}
                  </div>
                  <div className="payslip-amount">
                    <span className="net-salary">{formatCurrency(payslip.netSalary)}</span>
                    {getStatusBadge(payslip.status)}
                  </div>
                </div>
              </Accordion.Header>
              
              <Accordion.Body className="payslip-details">
                <div className="row">
                  {/* Earnings Section */}
                  <div className="col-md-6">
                    <div className="earnings-section">
                      <h6 className="section-title text-success">
                        <i className="fas fa-plus-circle me-2"></i>
                        Earnings
                      </h6>
                      <div className="earnings-breakdown">
                        <div className="earning-item">
                          <span className="earning-label">Base Salary</span>
                          <span className="earning-amount">{formatCurrency(payslip.baseSalary)}</span>
                        </div>
                        {payslip.bonus > 0 && (
                          <div className="earning-item">
                            <span className="earning-label">Bonus</span>
                            <span className="earning-amount">{formatCurrency(payslip.bonus)}</span>
                          </div>
                        )}
                        <div className="earning-item total">
                          <span className="earning-label">Gross Salary</span>
                          <span className="earning-amount">
                            {formatCurrency(payslip.baseSalary + payslip.bonus)}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Deductions Section */}
                  <div className="col-md-6">
                    <div className="deductions-section">
                      <h6 className="section-title text-danger">
                        <i className="fas fa-minus-circle me-2"></i>
                        Deductions
                      </h6>
                      <div className="deductions-breakdown">
                        {payslip.deductions > 0 ? (
                          <>
                            <div className="deduction-item">
                              <span className="deduction-label">Total Deductions</span>
                              <span className="deduction-amount">{formatCurrency(payslip.deductions)}</span>
                            </div>
                          </>
                        ) : (
                          <div className="deduction-item">
                            <span className="deduction-label text-muted">No deductions</span>
                            <span className="deduction-amount">{formatCurrency(0)}</span>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                </div>

                {/* Net Salary Section */}
                <div className="row mt-4">
                  <div className="col-12">
                    <div className="net-salary-section">
                      <div className="net-salary-card">
                        <div className="d-flex justify-content-between align-items-center">
                          <h5 className="net-salary-label mb-0">
                            <i className="fas fa-wallet me-2"></i>
                            Net Salary
                          </h5>
                          <h4 className="net-salary-amount mb-0">
                            {formatCurrency(payslip.netSalary)}
                          </h4>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Payslip Details */}
                <div className="row mt-4">
                  <div className="col-12">
                    <div className="payslip-meta">
                      <div className="row">
                        <div className="col-md-6">
                          <small className="text-muted">
                            <i className="fas fa-calendar me-1"></i>
                            Pay Date: {payslip.payDate 
                              ? new Date(payslip.payDate).toLocaleDateString()
                              : 'Not specified'
                            }
                          </small>
                        </div>
                        <div className="col-md-6">
                          <small className="text-muted">
                            <i className="fas fa-hashtag me-1"></i>
                            Payroll Run ID: {payslip.runId}
                          </small>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </Accordion.Body>
            </Accordion.Item>
          ))}
        </Accordion>
      )}
    </div>
  );
};

export default PayslipsTab;
