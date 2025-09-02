import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Form, Button, Alert, Spinner } from 'react-bootstrap';
import { Formik } from 'formik';
import * as Yup from 'yup';
import { useAuth } from '../context/AuthContext';
import { useNavigate, useLocation } from 'react-router-dom';
import { useToast } from '../components/common/ToastNotification';
import './styles/LoginPage.css';

// Validation schema using Yup
const loginValidationSchema = Yup.object().shape({
  username: Yup.string()
    .min(3, 'Username must be at least 3 characters')
    .max(50, 'Username must be less than 50 characters')
    .required('Username is required'),
  password: Yup.string()
    .min(6, 'Password must be at least 6 characters')
    .required('Password is required'),
});

const LoginPage = () => {
  const { login, isAuthenticated, role } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const { showSuccess, showError } = useToast();
  
  const [errorMessage, setErrorMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  // Get the intended destination after login
  const from = location.state?.from?.pathname || '/';

  // Redirect if already authenticated
  useEffect(() => {
    if (isAuthenticated()) {
      const redirectPath = role === 'Admin' ? '/admin/dashboard' : '/employee/dashboard';
      navigate(redirectPath, { replace: true });
    }
  }, [isAuthenticated, role, navigate]);

  // Handle form submission
  const handleSubmit = async (values, { setSubmitting }) => {
    setIsLoading(true);
    setErrorMessage('');

    try {
      const result = await login(values);
      
      if (result.success) {
        // Show success toast
        showSuccess('Login successful! Redirecting...', { delay: 2000 });
        
        // Successful login - determine redirect path
        const user = JSON.parse(localStorage.getItem('user'));
        const redirectPath = user?.role === 'Admin' 
          ? '/admin/dashboard' 
          : '/employee/dashboard';
        
        // Small delay for better UX
        setTimeout(() => {
          navigate(redirectPath, { replace: true });
        }, 1000);
      } else {
        setErrorMessage(result.message);
        showError(result.message);
      }
    } catch (error) {
      console.error('Login error:', error);
      setErrorMessage('An unexpected error occurred. Please try again.');
    } finally {
      setIsLoading(false);
      setSubmitting(false);
    }
  };

  return (
    <div className="login-page" style={{
      background: 'linear-gradient(135deg, var(--primary-background) 0%, #e9ecef 100%)',
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      paddingTop: '2rem',
      paddingBottom: '2rem'
    }}>
      <Container>
        <Row className="justify-content-center">
          <Col md={6} lg={4}>
            <div className="text-center mb-4">
              <h1 className="display-6 fw-bold" style={{ color: 'var(--primary-dark)' }}>
                Payroll Management
              </h1>
              <p className="text-muted-custom">
                Sign in to access your account
              </p>
            </div>

            <Card className="shadow-lg border-0" style={{
              borderRadius: 'var(--border-radius)',
              background: 'var(--cards-background)'
            }}>
              <Card.Body className="p-4">
                <div className="text-center mb-4">
                  <h3 className="card-title mb-0">Welcome Back</h3>
                  <p className="text-muted-custom small">
                    Please enter your credentials to continue
                  </p>
                </div>

                {/* Error Alert */}
                {errorMessage && (
                  <Alert 
                    variant="danger" 
                    className="mb-3"
                    style={{ borderRadius: 'var(--border-radius)' }}
                  >
                    <small>{errorMessage}</small>
                  </Alert>
                )}

                {/* Login Form */}
                <Formik
                  initialValues={{
                    username: '',
                    password: '',
                  }}
                  validationSchema={loginValidationSchema}
                  onSubmit={handleSubmit}
                >
                  {({
                    values,
                    errors,
                    touched,
                    handleChange,
                    handleBlur,
                    handleSubmit,
                    isSubmitting,
                  }) => (
                    <Form onSubmit={handleSubmit}>
                      {/* Username Field */}
                      <Form.Group className="mb-3">
                        <Form.Label htmlFor="username" className="fw-semibold">
                          Username
                        </Form.Label>
                        <Form.Control
                          id="username"
                          name="username"
                          type="text"
                          placeholder="Enter your username"
                          value={values.username}
                          onChange={handleChange}
                          onBlur={handleBlur}
                          isInvalid={touched.username && errors.username}
                          disabled={isSubmitting || isLoading}
                          style={{
                            borderRadius: 'var(--border-radius)',
                            padding: '0.75rem 1rem',
                          }}
                        />
                        <Form.Control.Feedback type="invalid">
                          {errors.username}
                        </Form.Control.Feedback>
                      </Form.Group>

                      {/* Password Field */}
                      <Form.Group className="mb-4">
                        <Form.Label htmlFor="password" className="fw-semibold">
                          Password
                        </Form.Label>
                        <Form.Control
                          id="password"
                          name="password"
                          type="password"
                          placeholder="Enter your password"
                          value={values.password}
                          onChange={handleChange}
                          onBlur={handleBlur}
                          isInvalid={touched.password && errors.password}
                          disabled={isSubmitting || isLoading}
                          style={{
                            borderRadius: 'var(--border-radius)',
                            padding: '0.75rem 1rem',
                          }}
                        />
                        <Form.Control.Feedback type="invalid">
                          {errors.password}
                        </Form.Control.Feedback>
                      </Form.Group>

                      {/* Submit Button */}
                      <Button
                        type="submit"
                        variant="primary"
                        size="lg"
                        className="w-100"
                        disabled={isSubmitting || isLoading}
                        style={{
                          borderRadius: 'var(--border-radius)',
                          padding: '0.75rem',
                          fontWeight: '600',
                          transition: 'all var(--transition-fast)',
                        }}
                      >
                        {isLoading ? (
                          <>
                            <Spinner
                              as="span"
                              animation="border"
                              size="sm"
                              className="me-2"
                            />
                            Signing In...
                          </>
                        ) : (
                          'Sign In'
                        )}
                      </Button>
                    </Form>
                  )}
                </Formik>

                {/* Removed demo credentials section */}
              </Card.Body>
            </Card>

            {/* Footer */}
            {/* <div className="text-center mt-4">
              <small className="text-muted-custom">
                © 2024 Payroll Management System. All rights reserved.
              </small>
            </div> */}
          </Col>
        </Row>
      </Container>
    </div>
  );
};

export default LoginPage;
