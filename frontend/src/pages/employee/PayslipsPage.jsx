import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Accordion, Table, Badge, Alert } from 'react-bootstrap';
import { useToast } from '../../components/common/ToastNotification';
import { LoadingSpinner } from '../../components/common';
import { apiService } from '../../api';
import './styles/PayslipsPage.css';

const PayslipsPage = () => {
  const { showToast } = useToast();
  const [payslips, setPayslips] = useState([]);
  const [payslipDetails, setPayslipDetails] = useState({});
  const [loading, setLoading] = useState(true);
  const [loadingDetails, setLoadingDetails] = useState({});

  useEffect(() => {
    fetchPayslips();
  }, []);

  const fetchPayslips = async () => {
    try {
      setLoading(true);
      const response = await apiService.get('/payroll/payslips');
      setPayslips(response.data.data || []);
    } catch (error) {
      showToast('Failed to load payslips', 'error');
      console.error('Payslips fetch error:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchPayslipDetails = async (runId) => {
    // Return early if already loaded
    if (payslipDetails[runId]) {
      return;
    }

    try {
      setLoadingDetails(prev => ({ ...prev, [runId]: true }));
      const response = await apiService.get(`/payroll/payslips/${runId}`);
      setPayslipDetails(prev => ({ ...prev, [runId]: response.data.data }));
    } catch (error) {
      showToast('Failed to load payslip details', 'error');
      console.error('Payslip details fetch error:', error);
    } finally {
      setLoadingDetails(prev => ({ ...prev, [runId]: false }));
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2
    }).format(amount || 0);
  };

  const formatPayPeriod = (payslip) => {
    if (payslip.runMonth && payslip.runYear) {
      const monthNames = [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'
      ];
      return `${monthNames[payslip.runMonth - 1]} ${payslip.runYear}`;
    }
    return 'Unknown Period';
  };

  const getPayslipStatus = (status) => {
    const statusConfig = {
      'Draft': { variant: 'secondary', icon: 'edit' },
      'Processed': { variant: 'success', icon: 'check-circle' },
      'Locked': { variant: 'primary', icon: 'lock' }
    };
    
    const config = statusConfig[status] || { variant: 'secondary', icon: 'question-circle' };
    
    return (
      <Badge bg={config.variant} className="status-badge">
        <i className={`fas fa-${config.icon} me-1`}></i>
        {status}
      </Badge>
    );
  };

  const renderPayslipSummary = (payslip) => (
    <div className="payslip-summary">
      <Row className="align-items-center">
        <Col sm={4}>
          <div className="pay-period">
            <h5>{formatPayPeriod(payslip)}</h5>
            <small className="text-muted">Pay Period</small>
          </div>
        </Col>
        <Col sm={3}>
          <div className="pay-date">
            <p className="mb-1">{payslip.payDate ? new Date(payslip.payDate).toLocaleDateString() : 'TBD'}</p>
            <small className="text-muted">Pay Date</small>
          </div>
        </Col>
        <Col sm={3}>
          <div className="net-pay">
            <p className="amount">{formatCurrency(payslip.netSalary)}</p>
            <small className="text-muted">Net Pay</small>
          </div>
        </Col>
        <Col sm={2} className="text-end">
          {getPayslipStatus(payslip.status)}
        </Col>
      </Row>
    </div>
  );

  const renderPayslipDetails = (runId) => {
    if (loadingDetails[runId]) {
      return (
        <div className="text-center py-4">
          <LoadingSpinner />
          <p className="mt-2">Loading payslip details...</p>
        </div>
      );
    }

    const details = payslipDetails[runId];
    if (!details) {
      return (
        <Alert variant="warning">
          <i className="fas fa-exclamation-triangle me-2"></i>
          Payslip details not available
        </Alert>
      );
    }

    return (
      <div className="payslip-details">
        <Row>
          <Col lg={6}>
            {/* Earnings */}
            <Card className="earnings-card">
              <Card.Header>
                <h5 className="mb-0">
                  <i className="fas fa-plus-circle text-success me-2"></i>
                  Earnings
                </h5>
              </Card.Header>
              <Card.Body>
                <Table borderless size="sm" className="earnings-table">
                  <tbody>
                    <tr>
                      <td>Base Salary</td>
                      <td className="text-end amount">{formatCurrency(details.baseSalary)}</td>
                    </tr>
                    {details.bonus > 0 && (
                      <tr>
                        <td>Bonus</td>
                        <td className="text-end amount">{formatCurrency(details.bonus)}</td>
                      </tr>
                    )}
                    <tr className="total-row">
                      <td><strong>Gross Earnings</strong></td>
                      <td className="text-end amount"><strong>{formatCurrency((details.baseSalary || 0) + (details.bonus || 0))}</strong></td>
                    </tr>
                  </tbody>
                </Table>
              </Card.Body>
            </Card>
          </Col>

          <Col lg={6}>
            {/* Deductions */}
            <Card className="deductions-card">
              <Card.Header>
                <h5 className="mb-0">
                  <i className="fas fa-minus-circle text-danger me-2"></i>
                  Deductions
                </h5>
              </Card.Header>
              <Card.Body>
                <Table borderless size="sm" className="deductions-table">
                  <tbody>
                    {details.deductions > 0 ? (
                      <>
                        <tr>
                          <td>Total Deductions</td>
                          <td className="text-end amount">{formatCurrency(details.deductions)}</td>
                        </tr>
                        <tr className="total-row">
                          <td><strong>Total Deductions</strong></td>
                          <td className="text-end amount"><strong>{formatCurrency(details.deductions)}</strong></td>
                        </tr>
                      </>
                    ) : (
                      <tr>
                        <td colSpan="2" className="text-center text-muted">
                          No deductions for this period
                        </td>
                      </tr>
                    )}
                  </tbody>
                </Table>
              </Card.Body>
            </Card>
          </Col>
        </Row>

        {/* Net Pay Summary */}
        <Row className="mt-4">
          <Col>
            <Card className="net-pay-card">
              <Card.Body>
                <Row className="align-items-center">
                  <Col sm={6}>
                    <h4 className="mb-1">Net Pay</h4>
                    <p className="text-muted mb-0">Amount paid to your account</p>
                  </Col>
                  <Col sm={6} className="text-end">
                    <h2 className="net-amount">{formatCurrency(details.netSalary)}</h2>
                  </Col>
                </Row>
              </Card.Body>
            </Card>
          </Col>
        </Row>

        {/* Pay Date Information */}
        {details.payDate && (
          <Row className="mt-3">
            <Col>
              <Alert variant="info" className="pay-info">
                <i className="fas fa-calendar-check me-2"></i>
                <strong>Payment Date:</strong> {new Date(details.payDate).toLocaleDateString()}
              </Alert>
            </Col>
          </Row>
        )}
      </div>
    );
  };

  if (loading) {
    return (
      <Container className="payslips-page">
        <div className="d-flex justify-content-center align-items-center" style={{ height: '400px' }}>
          <LoadingSpinner />
        </div>
      </Container>
    );
  }

  return (
    <Container className="payslips-page">
      <div className="page-header">
        <h1>My Payslips</h1>
        <p className="text-muted">View your salary statements and payment history</p>
      </div>

      {payslips.length === 0 ? (
        <Card className="empty-state-card">
          <Card.Body>
            <div className="empty-state">
              <i className="fas fa-file-invoice-dollar"></i>
              <h4>No Payslips Available</h4>
              <p>Your payslips will appear here once payroll has been processed.</p>
            </div>
          </Card.Body>
        </Card>
      ) : (
        <Card className="payslips-card">
          <Card.Header>
            <h3>Payslip History</h3>
            <small className="text-muted">Click on any payslip to view detailed breakdown</small>
          </Card.Header>
          <Card.Body className="p-0">
            <Accordion className="payslips-accordion">
              {payslips.map((payslip, index) => (
                <Accordion.Item 
                  key={payslip.runId} 
                  eventKey={index.toString()}
                  className="payslip-accordion-item"
                >
                  <Accordion.Header 
                    onClick={() => fetchPayslipDetails(payslip.runId)}
                    className="payslip-header"
                  >
                    {renderPayslipSummary(payslip)}
                  </Accordion.Header>
                  <Accordion.Body className="payslip-body">
                    {renderPayslipDetails(payslip.runId)}
                  </Accordion.Body>
                </Accordion.Item>
              ))}
            </Accordion>
          </Card.Body>
        </Card>
      )}
    </Container>
  );
};

export default PayslipsPage;
