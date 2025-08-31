import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Modal, Alert } from 'react-bootstrap';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../components/common/ToastNotification';
import { LoadingSpinner } from '../../components/common';
import { apiService } from '../../api';
import './styles/ProfilePage.css';

const ProfilePage = () => {
  const { user } = useAuth();
  const { showToast } = useToast();
  const [profileData, setProfileData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [editMode, setEditMode] = useState(false);
  const [showPasswordModal, setShowPasswordModal] = useState(false);

  // Validation schemas
  const profileValidationSchema = Yup.object({
    firstName: Yup.string().required('First name is required'),
    lastName: Yup.string().required('Last name is required'),
    phone: Yup.string().required('Phone number is required'),
    address: Yup.string().required('Address is required'),
    dateOfBirth: Yup.date().required('Date of birth is required')
  });

  const passwordValidationSchema = Yup.object({
    currentPassword: Yup.string().required('Current password is required'),
    newPassword: Yup.string()
      .min(6, 'Password must be at least 6 characters')
      .required('New password is required'),
    confirmPassword: Yup.string()
      .oneOf([Yup.ref('newPassword')], 'Passwords must match')
      .required('Confirm password is required')
  });

  // Fetch user profile data
  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      setLoading(true);
      const response = await apiService.get('/users/me');
      setProfileData(response.data.data);
    } catch (error) {
      showToast('Failed to load profile data', 'error');
      console.error('Profile fetch error:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleProfileUpdate = async (values, { setSubmitting }) => {
    try {
      // Remove email from the update request since it shouldn't be changed
      const { email, ...updateData } = values;
      const response = await apiService.patch('/employees/me', updateData);
      setProfileData(response.data.data);
      setEditMode(false);
      showToast('Profile updated successfully!', 'success');
    } catch (error) {
      showToast('Failed to update profile', 'error');
      console.error('Profile update error:', error);
    } finally {
      setSubmitting(false);
    }
  };

  const handlePasswordChange = async (values, { setSubmitting, resetForm }) => {
    try {
      await apiService.patch('/users/me/password', {
        currentPassword: values.currentPassword,
        newPassword: values.newPassword
      });
      showToast('Password changed successfully!', 'success');
      setShowPasswordModal(false);
      resetForm();
    } catch (error) {
      showToast('Failed to change password', 'error');
      console.error('Password change error:', error);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <Container className="profile-page">
        <div className="d-flex justify-content-center align-items-center" style={{ height: '400px' }}>
          <LoadingSpinner />
        </div>
      </Container>
    );
  }

  return (
    <Container className="profile-page">
      <Row>
        <Col lg={8} className="mx-auto">
          <div className="page-header">
            <h1>My Profile</h1>
            <p className="text-muted">Manage your personal information and account settings</p>
          </div>

          <Card className="profile-card">
            <Card.Header className="profile-card-header">
              <div className="d-flex justify-content-between align-items-center">
                <h3>Personal Information</h3>
                <div className="profile-actions">
                  {!editMode ? (
                    <>
                      <Button
                        variant="outline-primary"
                        size="sm"
                        onClick={() => setEditMode(true)}
                        className="me-2"
                      >
                        <i className="fas fa-edit me-1"></i>
                        Edit Profile
                      </Button>
                      <Button
                        variant="outline-secondary"
                        size="sm"
                        onClick={() => setShowPasswordModal(true)}
                      >
                        <i className="fas fa-key me-1"></i>
                        Change Password
                      </Button>
                    </>
                  ) : (
                    <Button
                      variant="outline-secondary"
                      size="sm"
                      onClick={() => setEditMode(false)}
                    >
                      <i className="fas fa-times me-1"></i>
                      Cancel
                    </Button>
                  )}
                </div>
              </div>
            </Card.Header>

            <Card.Body>
              {!editMode ? (
                // Display Mode
                <div className="profile-display">
                  <Row>
                    <Col md={6}>
                      <div className="profile-field">
                        <label>First Name</label>
                        <p>{profileData?.firstName || 'N/A'}</p>
                      </div>
                    </Col>
                    <Col md={6}>
                      <div className="profile-field">
                        <label>Last Name</label>
                        <p>{profileData?.lastName || 'N/A'}</p>
                      </div>
                    </Col>
                    <Col md={6}>
                      <div className="profile-field">
                        <label>Email</label>
                        <p>{profileData?.email || 'N/A'}</p>
                      </div>
                    </Col>
                    <Col md={6}>
                      <div className="profile-field">
                        <label>Phone</label>
                        <p>{profileData?.phone || 'N/A'}</p>
                      </div>
                    </Col>
                    <Col md={6}>
                      <div className="profile-field">
                        <label>Date of Birth</label>
                        <p>{profileData?.dateOfBirth ? new Date(profileData.dateOfBirth).toLocaleDateString() : 'N/A'}</p>
                      </div>
                    </Col>
                    <Col md={6}>
                      <div className="profile-field">
                        <label>Employee ID</label>
                        <p>{profileData?.employeeId || 'N/A'}</p>
                      </div>
                    </Col>
                    <Col md={12}>
                      <div className="profile-field">
                        <label>Address</label>
                        <p>{profileData?.address || 'N/A'}</p>
                      </div>
                    </Col>
                  </Row>
                </div>
              ) : (
                // Edit Mode
                <Formik
                  initialValues={{
                    firstName: profileData?.firstName || '',
                    lastName: profileData?.lastName || '',
                    email: profileData?.email || '',
                    phone: profileData?.phone || '',
                    address: profileData?.address || '',
                    dateOfBirth: profileData?.dateOfBirth ? profileData.dateOfBirth.split('T')[0] : ''
                  }}
                  validationSchema={profileValidationSchema}
                  onSubmit={handleProfileUpdate}
                >
                  {({ isSubmitting }) => (
                    <Form className="profile-form">
                      <Row>
                        <Col md={6}>
                          <div className="form-group">
                            <label htmlFor="firstName">First Name</label>
                            <Field
                              type="text"
                              name="firstName"
                              className="form-control"
                              placeholder="Enter first name"
                            />
                            <ErrorMessage name="firstName" component="div" className="error-message" />
                          </div>
                        </Col>
                        <Col md={6}>
                          <div className="form-group">
                            <label htmlFor="lastName">Last Name</label>
                            <Field
                              type="text"
                              name="lastName"
                              className="form-control"
                              placeholder="Enter last name"
                            />
                            <ErrorMessage name="lastName" component="div" className="error-message" />
                          </div>
                        </Col>
                        <Col md={6}>
                          <div className="form-group">
                            <label htmlFor="email">Email</label>
                            <Field
                              type="email"
                              name="email"
                              className="form-control"
                              placeholder="Enter email address"
                              disabled
                            />
                            <small className="text-muted">Email cannot be changed</small>
                          </div>
                        </Col>
                        <Col md={6}>
                          <div className="form-group">
                            <label htmlFor="phone">Phone</label>
                            <Field
                              type="text"
                              name="phone"
                              className="form-control"
                              placeholder="Enter phone number"
                            />
                            <ErrorMessage name="phone" component="div" className="error-message" />
                          </div>
                        </Col>
                        <Col md={6}>
                          <div className="form-group">
                            <label htmlFor="dateOfBirth">Date of Birth</label>
                            <Field
                              type="date"
                              name="dateOfBirth"
                              className="form-control"
                            />
                            <ErrorMessage name="dateOfBirth" component="div" className="error-message" />
                          </div>
                        </Col>
                        <Col md={12}>
                          <div className="form-group">
                            <label htmlFor="address">Address</label>
                            <Field
                              as="textarea"
                              name="address"
                              className="form-control"
                              rows="3"
                              placeholder="Enter full address"
                            />
                            <ErrorMessage name="address" component="div" className="error-message" />
                          </div>
                        </Col>
                      </Row>
                      
                      <div className="form-actions">
                        <Button
                          type="submit"
                          variant="primary"
                          disabled={isSubmitting}
                        >
                          {isSubmitting ? <LoadingSpinner size="sm" /> : <i className="fas fa-save me-1"></i>}
                          Save Changes
                        </Button>
                      </div>
                    </Form>
                  )}
                </Formik>
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Password Change Modal */}
      <Modal show={showPasswordModal} onHide={() => setShowPasswordModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Change Password</Modal.Title>
        </Modal.Header>
        <Formik
          initialValues={{
            currentPassword: '',
            newPassword: '',
            confirmPassword: ''
          }}
          validationSchema={passwordValidationSchema}
          onSubmit={handlePasswordChange}
        >
          {({ isSubmitting }) => (
            <Form>
              <Modal.Body>
                <div className="form-group">
                  <label htmlFor="currentPassword">Current Password</label>
                  <Field
                    type="password"
                    name="currentPassword"
                    className="form-control"
                    placeholder="Enter current password"
                  />
                  <ErrorMessage name="currentPassword" component="div" className="error-message" />
                </div>

                <div className="form-group">
                  <label htmlFor="newPassword">New Password</label>
                  <Field
                    type="password"
                    name="newPassword"
                    className="form-control"
                    placeholder="Enter new password"
                  />
                  <ErrorMessage name="newPassword" component="div" className="error-message" />
                </div>

                <div className="form-group">
                  <label htmlFor="confirmPassword">Confirm New Password</label>
                  <Field
                    type="password"
                    name="confirmPassword"
                    className="form-control"
                    placeholder="Confirm new password"
                  />
                  <ErrorMessage name="confirmPassword" component="div" className="error-message" />
                </div>
              </Modal.Body>
              
              <Modal.Footer>
                <Button
                  variant="secondary"
                  onClick={() => setShowPasswordModal(false)}
                  disabled={isSubmitting}
                >
                  Cancel
                </Button>
                <Button
                  type="submit"
                  variant="primary"
                  disabled={isSubmitting}
                >
                  {isSubmitting ? <LoadingSpinner size="sm" /> : <i className="fas fa-key me-1"></i>}
                  Change Password
                </Button>
              </Modal.Footer>
            </Form>
          )}
        </Formik>
      </Modal>
    </Container>
  );
};

export default ProfilePage;
