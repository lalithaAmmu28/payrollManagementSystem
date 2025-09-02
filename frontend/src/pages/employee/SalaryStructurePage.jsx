import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Badge, Alert, Button } from 'react-bootstrap';
import { LoadingSpinner } from '../../components/common';
import { useToast } from '../../components/common/ToastNotification';
import { apiService } from '../../api';
import './styles/SalaryStructurePage.css';

const SalaryStructurePage = () => {
  const { showToast } = useToast();
  const [salaryStructures, setSalaryStructures] = useState([]);
  const [currentSalary, setCurrentSalary] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchSalaryStructures();
  }, []);

  const fetchSalaryStructures = async () => {
    try {
      setLoading(true);
      
      // Employees can access their profile data via /users/me
      // This includes basic salary info from their job role
      const userResponse = await apiService.get('/users/me');
      const userData = userResponse.data.data;

      console.log('User data from /users/me:', userData); // Debug log

      // Extract salary info from the employee profile
      if (userData) {
        // Create a basic salary structure from available employee data
        const annualSalary = userData.baseSalary || 0;
        const monthlySalary = annualSalary / 12; // Convert annual to monthly
        
        const basicSalaryInfo = {
          id: userData.employeeId,
          baseSalary: monthlySalary, // Monthly base salary
          annualSalary: annualSalary, // Store annual for reference
          allowances: 0, // Not available in employee profile
          deductions: 0, // Not available in employee profile
          effectiveDate: userData.createdAt,
          notes: 'Current salary information from your job role'
        };

        setCurrentSalary(basicSalaryInfo);
        setSalaryStructures([basicSalaryInfo]); // Only current info available
      } else {
        setCurrentSalary(null);
        setSalaryStructures([]);
      }

    } catch (error) {
      console.error('Error fetching salary information:', error);
      showToast('Failed to load salary information', 'error');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2
    }).format(amount || 0);
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const calculateAnnualSalary = (monthlySalary) => {
    return (monthlySalary || 0) * 12;
  };

  const renderCurrentSalaryCard = () => {
    if (!currentSalary) {
      return (
        <Alert variant="warning">
          <i className="fas fa-exclamation-triangle me-2"></i>
          No salary information found. Please contact HR for assistance.
        </Alert>
      );
    }

    return (
      <Card className="current-salary-card">
        <Card.Header className="bg-primary text-white">
          <div className="d-flex justify-content-between align-items-center">
            <div>
              <h4 className="mb-0">Current Salary Structure</h4>
              <small>Effective from {formatDate(currentSalary.effectiveDate)}</small>
            </div>
            <Badge bg="success" className="current-badge">
              <i className="fas fa-check-circle me-1"></i>
              Current
            </Badge>
          </div>
        </Card.Header>
        <Card.Body>
          <Row>
            <Col lg={6}>
              <div className="salary-breakdown">
                <h5 className="section-title">
                  <i className="fas fa-money-bill-wave me-2"></i>
                  Monthly Breakdown
                </h5>
                
                <div className="salary-item">
                  <div className="salary-label">Monthly Base Salary</div>
                  <div className="salary-value primary">
                    {formatCurrency(currentSalary.baseSalary)}
                  </div>
                </div>

                {/* Note: Allowances and deductions are not available in employee view
                {currentSalary.notes && currentSalary.notes.includes('Current salary information') && (
                  <div className="salary-note">
                    <i className="fas fa-info-circle me-2 text-muted"></i>
                    <small className="text-muted">
                      Monthly salary calculated from your annual job role salary. Detailed allowances and deductions are available in your payslips.
                    </small>
                  </div>
                )} */}

                {currentSalary.allowances && currentSalary.allowances > 0 && (
                  <div className="salary-item">
                    <div className="salary-label">Allowances</div>
                    <div className="salary-value positive">
                      +{formatCurrency(currentSalary.allowances)}
                    </div>
                  </div>
                )}

                {currentSalary.deductions && currentSalary.deductions > 0 && (
                  <div className="salary-item">
                    <div className="salary-label">Deductions</div>
                    <div className="salary-value negative">
                      -{formatCurrency(currentSalary.deductions)}
                    </div>
                  </div>
                )}

                <hr />
                
                <div className="salary-item total">
                  <div className="salary-label">Net Monthly Salary</div>
                  <div className="salary-value">
                    {formatCurrency(
                      (currentSalary.baseSalary || 0) + 
                      (currentSalary.allowances || 0) - 
                      (currentSalary.deductions || 0)
                    )}
                  </div>
                </div>
              </div>
            </Col>

            <Col lg={6}>
              <div className="salary-summary">
                <h5 className="section-title">
                  <i className="fas fa-chart-line me-2"></i>
                  Annual Summary
                </h5>
                
                <div className="annual-card">
                  <div className="annual-amount">
                    {formatCurrency(
                      (currentSalary.annualSalary || calculateAnnualSalary(currentSalary.baseSalary)) + 
                      calculateAnnualSalary(currentSalary.allowances || 0) - 
                      calculateAnnualSalary(currentSalary.deductions || 0)
                    )}
                  </div>
                  <div className="annual-label">Gross Annual Salary</div>
                </div>

                <div className="salary-components">
                  <div className="component-item">
                    <span className="component-label">Base Annual:</span>
                    <span className="component-value">
                      {formatCurrency(currentSalary.annualSalary || calculateAnnualSalary(currentSalary.baseSalary))}
                    </span>
                  </div>
                  
                  {currentSalary.allowances && currentSalary.allowances > 0 && (
                    <div className="component-item">
                      <span className="component-label">Annual Allowances:</span>
                      <span className="component-value positive">
                        +{formatCurrency(calculateAnnualSalary(currentSalary.allowances))}
                      </span>
                    </div>
                  )}
                  
                  {currentSalary.deductions && currentSalary.deductions > 0 && (
                    <div className="component-item">
                      <span className="component-label">Annual Deductions:</span>
                      <span className="component-value negative">
                        -{formatCurrency(calculateAnnualSalary(currentSalary.deductions))}
                      </span>
                    </div>
                  )}
                </div>
              </div>
            </Col>
          </Row>
        </Card.Body>
      </Card>
    );
  };

  const renderSalaryHistory = () => null;

  if (loading) {
    return (
      <Container className="salary-structure-page">
        <div className="d-flex justify-content-center align-items-center" style={{ height: '400px' }}>
          <LoadingSpinner />
        </div>
      </Container>
    );
  }

  return (
    <Container className="salary-structure-page">
      {/* Page Header */}
      <div className="page-header">
        <div className="header-content">
          <h1>
            <i className="fas fa-chart-line me-3"></i>
            My Salary Structure
          </h1>
          <p className="header-subtitle">
            View your current salary breakdown and historical changes
          </p>
        </div>
        <div className="header-actions">
          <Button 
            variant="outline-primary" 
            onClick={fetchSalaryStructures}
            disabled={loading}
          >
            <i className="fas fa-sync-alt me-2"></i>
            Refresh
          </Button>
        </div>
      </div>

      {/* Current Salary Structure */}
      <Row className="mb-4">
        <Col>
          {renderCurrentSalaryCard()}
        </Col>
      </Row>

      {/* Salary History */}
      
    </Container>
  );
};

export default SalaryStructurePage;
