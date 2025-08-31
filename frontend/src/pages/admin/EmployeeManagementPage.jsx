import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Table, Form, InputGroup, Badge } from 'react-bootstrap';
import { Formik } from 'formik';
import apiService from '../../api/apiService';
import { useToast } from '../../components/common/ToastNotification';
import { LoadingSpinner, SkeletonLoader, ConfirmationModal } from '../../components/common';
import EmployeeDetailPane from '../../components/admin/EmployeeDetailPane';
import AddEmployeeModal from '../../components/admin/AddEmployeeModal';
import './styles/EmployeeManagementPage.css';

const EmployeeManagementPage = () => {
  const { showToast } = useToast();
  const [employees, setEmployees] = useState([]);
  const [filteredEmployees, setFilteredEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  const [showDetailPane, setShowDetailPane] = useState(false);
  const [showAddModal, setShowAddModal] = useState(false);

  // Fetch all employees on component mount
  useEffect(() => {
    fetchEmployees();
  }, []);

  // Filter employees based on search term
  useEffect(() => {
    if (!searchTerm) {
      setFilteredEmployees(employees);
    } else {
      const filtered = employees.filter(employee =>
        employee.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        employee.lastName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        employee.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
        employee.departmentName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        employee.jobTitle.toLowerCase().includes(searchTerm.toLowerCase())
      );
      setFilteredEmployees(filtered);
    }
  }, [employees, searchTerm]);

  const fetchEmployees = async () => {
    try {
      setLoading(true);
      const response = await apiService.get('/employees');
      setEmployees(response.data.data);
    } catch (error) {
      showToast('Failed to fetch employees', 'error');
      console.error('Fetch employees error:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleEmployeeClick = (employee) => {
    setSelectedEmployee(employee);
    setShowDetailPane(true);
  };

  const handleCloseDetailPane = () => {
    setShowDetailPane(false);
    setSelectedEmployee(null);
  };

  const handleEmployeeAdded = (newEmployee) => {
    setEmployees(prev => [newEmployee, ...prev]);
    setShowAddModal(false);
    showToast('Employee added successfully!', 'success');
  };

  const handleEmployeeUpdated = (updatedEmployee) => {
    setEmployees(prev => 
      prev.map(emp => 
        emp.employeeId === updatedEmployee.employeeId ? updatedEmployee : emp
      )
    );
    setSelectedEmployee(updatedEmployee);
    showToast('Employee updated successfully!', 'success');
  };

  const handleEmployeeDeleted = (deletedEmployeeId) => {
    setEmployees(prev => prev.filter(emp => emp.employeeId !== deletedEmployeeId));
    setShowDetailPane(false);
    setSelectedEmployee(null);
    showToast('Employee deleted successfully!', 'success');
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString();
  };

  const getStatusBadge = (isActive) => {
    return (
      <Badge bg={isActive ? 'success' : 'secondary'}>
        {isActive ? 'Active' : 'Inactive'}
      </Badge>
    );
  };

  return (
    <div className="employee-management-page">
      <Container fluid>
        <Row>
          <Col lg={showDetailPane ? 5 : 12} className="main-content">
            <div className="page-header">
              <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                  <h1 className="page-title">Employee Management</h1>
                  <p className="page-subtitle text-muted">
                    Manage employee information, salary structures, and access
                  </p>
                </div>
                <Button
                  variant="primary"
                  onClick={() => setShowAddModal(true)}
                  className="add-employee-btn"
                >
                  <i className="fas fa-plus me-2"></i>
                  Add Employee
                </Button>
              </div>

              {/* Search and Filter Bar */}
              <Card className="filter-card mb-4">
                <Card.Body>
                  <Row>
                    <Col md={6}>
                      <InputGroup>
                        <InputGroup.Text>
                          <i className="fas fa-search"></i>
                        </InputGroup.Text>
                        <Form.Control
                          type="text"
                          placeholder="Search employees by name, email, department, or job title..."
                          value={searchTerm}
                          onChange={(e) => setSearchTerm(e.target.value)}
                        />
                      </InputGroup>
                    </Col>
                    <Col md={6} className="d-flex justify-content-end align-items-center">
                      <span className="text-muted">
                        {loading ? (
                          <SkeletonLoader width="100px" height="20px" />
                        ) : (
                          `${filteredEmployees.length} employee${filteredEmployees.length !== 1 ? 's' : ''} found`
                        )}
                      </span>
                    </Col>
                  </Row>
                </Card.Body>
              </Card>
            </div>

            {/* Employee Table */}
            <Card className="employee-table-card">
              <Card.Body>
                {loading ? (
                  <div className="skeleton-table">
                    <SkeletonLoader height="40px" className="mb-2" />
                    {[...Array(8)].map((_, index) => (
                      <SkeletonLoader key={index} height="60px" className="mb-2" />
                    ))}
                  </div>
                ) : (
                  <Table hover responsive className="employee-table">
                    <thead>
                      <tr>
                        <th>Employee</th>
                        <th>Email</th>
                        <th>Department</th>
                        <th>Job Title</th>
                        <th>Phone</th>
                        <th>Join Date</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {filteredEmployees.length === 0 ? (
                        <tr>
                          <td colSpan="7" className="text-center py-4">
                            <div className="no-employees">
                              <i className="fas fa-users fa-3x text-muted mb-3"></i>
                              <p className="text-muted">
                                {searchTerm ? 'No employees found matching your search.' : 'No employees found.'}
                              </p>
                            </div>
                          </td>
                        </tr>
                      ) : (
                        filteredEmployees.map((employee) => (
                          <tr
                            key={employee.employeeId}
                            onClick={() => handleEmployeeClick(employee)}
                            className="employee-row"
                          >
                            <td>
                              <div className="employee-info">
                                <div className="employee-avatar">
                                  <i className="fas fa-user"></i>
                                </div>
                                <div>
                                  <div className="employee-name">
                                    {employee.firstName} {employee.lastName}
                                  </div>
                                  <small className="text-muted">ID: {employee.employeeId}</small>
                                </div>
                              </div>
                            </td>
                            <td>{employee.email}</td>
                            <td>
                              <span className="department-badge">
                                {employee.departmentName}
                              </span>
                            </td>
                            <td>{employee.jobTitle}</td>
                            <td>{employee.phone}</td>
                            <td>{formatDate(employee.createdAt)}</td>
                            <td>{getStatusBadge(employee.isActive)}</td>
                          </tr>
                        ))
                      )}
                    </tbody>
                  </Table>
                )}
              </Card.Body>
            </Card>
          </Col>

          {/* Detail Pane */}
          {showDetailPane && selectedEmployee && (
            <Col lg={7} className="detail-pane-col">
              <EmployeeDetailPane
                employee={selectedEmployee}
                onClose={handleCloseDetailPane}
                onEmployeeUpdated={handleEmployeeUpdated}
                onEmployeeDeleted={handleEmployeeDeleted}
              />
            </Col>
          )}
        </Row>
      </Container>

      {/* Add Employee Modal */}
      <AddEmployeeModal
        show={showAddModal}
        onHide={() => setShowAddModal(false)}
        onEmployeeAdded={handleEmployeeAdded}
      />
    </div>
  );
};

export default EmployeeManagementPage;
