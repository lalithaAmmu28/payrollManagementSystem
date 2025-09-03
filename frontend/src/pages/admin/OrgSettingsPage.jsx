import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Table, Tabs, Tab, Form, Alert } from 'react-bootstrap';
import apiService from '../../api/apiService';
import { useToast } from '../../components/common/ToastNotification';
import { LoadingSpinner, SkeletonLoader, ConfirmationModal } from '../../components/common';
import './styles/OrgSettingsPage.css';

const OrgSettingsPage = () => {
  const { showToast } = useToast();
  const [activeTab, setActiveTab] = useState('departments');
  const [departments, setDepartments] = useState([]);
  const [jobRoles, setJobRoles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editingItem, setEditingItem] = useState(null);
  const [newItem, setNewItem] = useState(null);
  const [deleteConfirm, setDeleteConfirm] = useState(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [deptResponse, jobResponse] = await Promise.all([
        apiService.get('/departments'),
        apiService.get('/jobs')
      ]);
      setDepartments(deptResponse.data.data);
      setJobRoles(jobResponse.data.data);
    } catch (error) {
      showToast('Failed to fetch organization data', 'error');
      console.error('Fetch error:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (item, type) => {
    setEditingItem({ ...item, type });
  };

  const handleCancelEdit = () => {
    setEditingItem(null);
    setNewItem(null);
  };

  const handleSave = async (item, type) => {
    try {
      setSaving(true);
      let response;
      let updatedData;

      if (item.isNew) {
        // Create new item
        if (type === 'department') {
          response = await apiService.post('/departments', {
            departmentName: item.departmentName
          });
          updatedData = response.data.data;
          setDepartments(prev => [updatedData, ...prev]);
        } else {
          response = await apiService.post('/jobs', {
            jobTitle: item.jobTitle,
            baseSalary: item.baseSalary
          });
          updatedData = response.data.data;
          setJobRoles(prev => [updatedData, ...prev]);
        }
        showToast(`${type === 'department' ? 'Department' : 'Job role'} created successfully!`, 'success');
        setNewItem(null);
      } else {
        // Update existing item
        if (type === 'department') {
          response = await apiService.put(`/departments/${item.departmentId}`, {
            departmentName: item.departmentName
          });
          updatedData = response.data.data;
          setDepartments(prev => 
            prev.map(dept => 
              dept.departmentId === item.departmentId ? updatedData : dept
            )
          );
        } else {
          response = await apiService.put(`/jobs/${item.jobId}`, {
            jobTitle: item.jobTitle,
            baseSalary: item.baseSalary
          });
          updatedData = response.data.data;
          setJobRoles(prev => 
            prev.map(job => 
              job.jobId === item.jobId ? updatedData : job
            )
          );
        }
        showToast(`${type === 'department' ? 'Department' : 'Job role'} updated successfully!`, 'success');
      }
      setEditingItem(null);
    } catch (error) {
      showToast(`Failed to save ${type}`, 'error');
      console.error('Save error:', error);
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id, type) => {
    try {
      if (type === 'department') {
        await apiService.delete(`/departments/${id}`);
        setDepartments(prev => prev.filter(dept => dept.departmentId !== id));
        showToast('Department deleted successfully!', 'success');
      } else {
        await apiService.delete(`/jobs/${id}`);
        setJobRoles(prev => prev.filter(job => job.jobId !== id));
        showToast('Job role deleted successfully!', 'success');
      }
    } catch (error) {
      const backendMessage = error?.response?.data?.message;
      const humanType = type === 'department' ? 'Department' : 'Job role';
      showToast(backendMessage || `Failed to delete ${humanType.toLowerCase()}`, 'error');
      console.error('Delete error:', error);
    } finally {
      setDeleteConfirm(null);
    }
  };

  const handleAddNew = (type) => {
    const newItemTemplate = type === 'department' 
      ? { 
          departmentId: 'new', 
          departmentName: '',
          isNew: true 
        }
      : { 
          jobId: 'new', 
          jobTitle: '',
          baseSalary: '',
          isNew: true 
        };
    
    setNewItem({ ...newItemTemplate, type });
    setEditingItem({ ...newItemTemplate, type });
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(amount);
  };

  const renderDepartmentsTable = () => {
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

    return (
      <Table hover responsive className="org-table">
        <thead>
          <tr>
            <th>Department Name</th>
            <th>Created</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {newItem && newItem.type === 'department' && (
            <tr className="new-item-row">
              <td>
                <Form.Control
                  type="text"
                  value={editingItem?.departmentName || ''}
                  onChange={(e) => setEditingItem(prev => ({ ...prev, departmentName: e.target.value }))}
                  placeholder="Enter department name"
                  size="sm"
                />
              </td>
              <td>-</td>
              <td>
                <div className="action-buttons d-flex flex-row flex-nowrap">
                  <Button
                    variant="success"
                    size="sm"
                    onClick={() => handleSave(editingItem, 'department')}
                    disabled={saving || !editingItem?.departmentName}
                    className="me-2"
                  >
                    {saving ? <LoadingSpinner size="sm" /> : <i className="fas fa-check"></i>}
                  </Button>
                  <Button
                    variant="secondary"
                    size="sm"
                    onClick={handleCancelEdit}
                    disabled={saving}
                  >
                    <i className="fas fa-times"></i>
                  </Button>
                </div>
              </td>
            </tr>
          )}
          {departments.map((dept) => (
            <tr key={dept.departmentId} className="data-row">
              <td>
                {editingItem?.departmentId === dept.departmentId && editingItem?.type === 'department' ? (
                  <Form.Control
                    type="text"
                    value={editingItem.departmentName}
                    onChange={(e) => setEditingItem(prev => ({ ...prev, departmentName: e.target.value }))}
                    size="sm"
                  />
                ) : (
                  <span className="item-name">{dept.departmentName}</span>
                )}
              </td>
              <td>
                <small className="text-muted">
                  {new Date(dept.createdAt).toLocaleDateString()}
                </small>
              </td>
              <td>
                {editingItem?.departmentId === dept.departmentId && editingItem?.type === 'department' ? (
                  <div className="action-buttons d-flex flex-row flex-nowrap">
                    <Button
                      variant="success"
                      size="sm"
                      onClick={() => handleSave(editingItem, 'department')}
                      disabled={saving}
                      className="me-2"
                    >
                      {saving ? <LoadingSpinner size="sm" /> : <i className="fas fa-check"></i>}
                    </Button>
                    <Button
                      variant="secondary"
                      size="sm"
                      onClick={handleCancelEdit}
                      disabled={saving}
                    >
                      <i className="fas fa-times"></i>
                    </Button>
                  </div>
                ) : (
                  <div className="action-buttons d-flex flex-row flex-nowrap">
                    <Button
                      variant="outline-primary"
                      size="sm"
                      onClick={() => handleEdit(dept, 'department')}
                      className="me-2"
                    >
                      <i className="fas fa-edit"></i>
                    </Button>
                    <Button
                      variant="outline-danger"
                      size="sm"
                      onClick={() => setDeleteConfirm({ id: dept.departmentId, type: 'department', name: dept.departmentName })}
                    >
                      <i className="fas fa-trash"></i>
                    </Button>
                  </div>
                )}
              </td>
            </tr>
          ))}
          {departments.length === 0 && !newItem && (
            <tr>
              <td colSpan="3" className="text-center py-4">
                <div className="no-data">
                  <i className="fas fa-building fa-3x text-muted mb-3"></i>
                  <p className="text-muted">No departments found.</p>
                </div>
              </td>
            </tr>
          )}
        </tbody>
      </Table>
    );
  };

  const renderJobRolesTable = () => {
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

    return (
      <Table hover responsive className="org-table">
        <thead>
          <tr>
            <th>Job Title</th>
            <th>Base Salary</th>
            <th>Created</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {newItem && newItem.type === 'job' && (
            <tr className="new-item-row">
              <td>
                <Form.Control
                  type="text"
                  value={editingItem?.jobTitle || ''}
                  onChange={(e) => setEditingItem(prev => ({ ...prev, jobTitle: e.target.value }))}
                  placeholder="Enter job title"
                  size="sm"
                />
              </td>
              <td>
                <Form.Control
                  type="number"
                  value={editingItem?.baseSalary || ''}
                  onChange={(e) => setEditingItem(prev => ({ ...prev, baseSalary: e.target.value }))}
                  placeholder="Enter base salary"
                  size="sm"
                />
              </td>
              <td>-</td>
              <td>
                <div className="action-buttons d-flex flex-row flex-nowrap">
                  <Button
                    variant="success"
                    size="sm"
                    onClick={() => handleSave(editingItem, 'job')}
                    disabled={saving || !editingItem?.jobTitle || !editingItem?.baseSalary}
                    className="me-2"
                  >
                    {saving ? <LoadingSpinner size="sm" /> : <i className="fas fa-check"></i>}
                  </Button>
                  <Button
                    variant="secondary"
                    size="sm"
                    onClick={handleCancelEdit}
                    disabled={saving}
                  >
                    <i className="fas fa-times"></i>
                  </Button>
                </div>
              </td>
            </tr>
          )}
          {jobRoles.map((job) => (
            <tr key={job.jobId} className="data-row">
              <td>
                {editingItem?.jobId === job.jobId && editingItem?.type === 'job' ? (
                  <Form.Control
                    type="text"
                    value={editingItem.jobTitle}
                    onChange={(e) => setEditingItem(prev => ({ ...prev, jobTitle: e.target.value }))}
                    size="sm"
                  />
                ) : (
                  <span className="item-name">{job.jobTitle}</span>
                )}
              </td>
              <td>
                {editingItem?.jobId === job.jobId && editingItem?.type === 'job' ? (
                  <Form.Control
                    type="number"
                    value={editingItem.baseSalary}
                    onChange={(e) => setEditingItem(prev => ({ ...prev, baseSalary: e.target.value }))}
                    size="sm"
                  />
                ) : (
                  <span className="salary-amount">{formatCurrency(job.baseSalary)}</span>
                )}
              </td>
              <td>
                <small className="text-muted">
                  {new Date(job.createdAt).toLocaleDateString()}
                </small>
              </td>
              <td>
                {editingItem?.jobId === job.jobId && editingItem?.type === 'job' ? (
                  <div className="action-buttons d-flex flex-row flex-nowrap">
                    <Button
                      variant="success"
                      size="sm"
                      onClick={() => handleSave(editingItem, 'job')}
                      disabled={saving}
                      className="me-2"
                    >
                      {saving ? <LoadingSpinner size="sm" /> : <i className="fas fa-check"></i>}
                    </Button>
                    <Button
                      variant="secondary"
                      size="sm"
                      onClick={handleCancelEdit}
                      disabled={saving}
                    >
                      <i className="fas fa-times"></i>
                    </Button>
                  </div>
                ) : (
                  <div className="action-buttons d-flex flex-row flex-nowrap">
                    <Button
                      variant="outline-primary"
                      size="sm"
                      onClick={() => handleEdit(job, 'job')}
                      className="me-2"
                    >
                      <i className="fas fa-edit"></i>
                    </Button>
                    <Button
                      variant="outline-danger"
                      size="sm"
                      onClick={() => setDeleteConfirm({ id: job.jobId, type: 'job', name: job.jobTitle })}
                    >
                      <i className="fas fa-trash"></i>
                    </Button>
                  </div>
                )}
              </td>
            </tr>
          ))}
          {jobRoles.length === 0 && !newItem && (
            <tr>
              <td colSpan="4" className="text-center py-4">
                <div className="no-data">
                  <i className="fas fa-briefcase fa-3x text-muted mb-3"></i>
                  <p className="text-muted">No job roles found.</p>
                </div>
              </td>
            </tr>
          )}
        </tbody>
      </Table>
    );
  };

  return (
    <div className="org-settings-page">
      <Container fluid>
        <div className="page-header">
          <div className="d-flex justify-content-between align-items-center mb-4">
            <div>
              <h1 className="page-title">Organization Settings</h1>
              <p className="page-subtitle text-muted">
                Manage departments and job roles for your organization
              </p>
            </div>
          </div>
        </div>

        <Card className="org-settings-card">
          <Card.Body>
            <Tabs
              activeKey={activeTab}
              onSelect={(tab) => setActiveTab(tab)}
              className="org-tabs"
            >
              <Tab eventKey="departments" title={
                <span><i className="fas fa-building me-2"></i>Departments</span>
              }>
                <div className="tab-content-wrapper">
                  <div className="d-flex justify-content-between align-items-center mb-3">
                    <h5 className="tab-title">Manage Departments</h5>
                    <Button
                      variant="primary"
                      size="sm"
                      onClick={() => handleAddNew('department')}
                      disabled={!!newItem || !!editingItem}
                      className="add-button"
                    >
                      <i className="fas fa-plus me-2"></i>
                      Add Department
                    </Button>
                  </div>
                  {renderDepartmentsTable()}
                </div>
              </Tab>

              <Tab eventKey="jobs" title={
                <span><i className="fas fa-briefcase me-2"></i>Job Roles</span>
              }>
                <div className="tab-content-wrapper">
                  <div className="d-flex justify-content-between align-items-center mb-3">
                    <h5 className="tab-title">Manage Job Roles</h5>
                    <Button
                      variant="primary"
                      size="sm"
                      onClick={() => handleAddNew('job')}
                      disabled={!!newItem || !!editingItem}
                      className="add-button"
                    >
                      <i className="fas fa-plus me-2"></i>
                      Add Job Role
                    </Button>
                  </div>
                  {renderJobRolesTable()}
                </div>
              </Tab>
            </Tabs>
          </Card.Body>
        </Card>

        {/* Delete Confirmation Modal */}
        <ConfirmationModal
          show={!!deleteConfirm}
          onHide={() => setDeleteConfirm(null)}
          onConfirm={() => handleDelete(deleteConfirm?.id, deleteConfirm?.type)}
          title={`Delete ${deleteConfirm?.type === 'department' ? 'Department' : 'Job Role'}`}
          message={
            <div>
              <p>Are you sure you want to delete <strong>{deleteConfirm?.name}</strong>?</p>
              <Alert variant="warning" className="mt-3">
                <small>
                  <i className="fas fa-exclamation-triangle me-2"></i>
                  This action cannot be undone. Any employees assigned to this {deleteConfirm?.type} will need to be reassigned.
                </small>
              </Alert>
            </div>
          }
          confirmText="Delete"
          confirmVariant="danger"
        />
      </Container>
    </div>
  );
};

export default OrgSettingsPage;
