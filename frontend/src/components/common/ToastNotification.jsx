import React, { useState, useEffect, createContext, useContext } from 'react';
import { Toast, ToastContainer } from 'react-bootstrap';
import './styles/ToastNotification.css';

// Toast Context for global toast management
const ToastContext = createContext();

export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
};

// Toast Provider Component
export const ToastProvider = ({ children }) => {
  const [toasts, setToasts] = useState([]);

  const addToast = (message, variant = 'info', options = {}) => {
    const id = Date.now() + Math.random();
    const toast = {
      id,
      message,
      variant,
      timestamp: new Date(),
      ...options,
    };
    
    setToasts(prev => [...prev, toast]);
    
    // Auto-remove toast after delay (default 5 seconds)
    const delay = options.autoHide !== false ? (options.delay || 5000) : null;
    if (delay) {
      setTimeout(() => {
        removeToast(id);
      }, delay);
    }
    
    return id;
  };

  const removeToast = (id) => {
    setToasts(prev => prev.filter(toast => toast.id !== id));
  };

  const clearAll = () => {
    setToasts([]);
  };

  // Toast helper methods
  const showSuccess = (message, options = {}) => 
    addToast(message, 'success', options);
  
  const showError = (message, options = {}) => 
    addToast(message, 'danger', options);
  
  const showWarning = (message, options = {}) => 
    addToast(message, 'warning', options);
  
  const showInfo = (message, options = {}) => 
    addToast(message, 'info', options);

  const value = {
    toasts,
    addToast,
    removeToast,
    clearAll,
    showSuccess,
    showError,
    showWarning,
    showInfo,
  };

  return (
    <ToastContext.Provider value={value}>
      {children}
      <ToastNotificationContainer />
    </ToastContext.Provider>
  );
};

// Individual Toast Component
const ToastNotification = ({ 
  id,
  message, 
  variant = 'info', 
  onClose,
  title = null,
  timestamp = null,
  autoHide = true,
  delay = 5000,
  showTimestamp = false,
}) => {
  const [show, setShow] = useState(true);

  const handleClose = () => {
    setShow(false);
    setTimeout(() => onClose && onClose(id), 150); // Delay for animation
  };

  const getToastIcon = () => {
    switch (variant) {
      case 'success':
        return '✅';
      case 'danger':
        return '❌';
      case 'warning':
        return '⚠️';
      case 'info':
      default:
        return 'ℹ️';
    }
  };

  const getToastTitle = () => {
    if (title) return title;
    
    switch (variant) {
      case 'success':
        return 'Success';
      case 'danger':
        return 'Error';
      case 'warning':
        return 'Warning';
      case 'info':
      default:
        return 'Information';
    }
  };

  const getToastBg = () => {
    switch (variant) {
      case 'success':
        return 'success';
      case 'danger':
        return 'danger';
      case 'warning':
        return 'warning';
      case 'info':
      default:
        return 'info';
    }
  };

  return (
    <Toast
      show={show}
      onClose={handleClose}
      bg={getToastBg()}
      autohide={autoHide}
      delay={delay}
      className="text-white"
      style={{
        borderRadius: 'var(--border-radius)',
        boxShadow: 'var(--box-shadow-hover)',
      }}
    >
      <Toast.Header
        closeButton={true}
        className="d-flex align-items-center"
        style={{
          backgroundColor: 'rgba(255, 255, 255, 0.1)',
          border: 'none',
          color: 'white',
        }}
      >
        <span className="me-2" style={{ fontSize: '1.2rem' }}>
          {getToastIcon()}
        </span>
        <strong className="me-auto">{getToastTitle()}</strong>
        {showTimestamp && timestamp && (
          <small className="ms-2">
            {timestamp.toLocaleTimeString()}
          </small>
        )}
      </Toast.Header>
      
      <Toast.Body>
        {typeof message === 'string' ? (
          <div>{message}</div>
        ) : (
          message
        )}
      </Toast.Body>
    </Toast>
  );
};

// Toast Container Component
const ToastNotificationContainer = () => {
  const { toasts, removeToast } = useToast();

  return (
    <ToastContainer
      position="top-end"
      className="position-fixed"
      style={{
        zIndex: 9999,
        top: '20px',
        right: '20px',
      }}
    >
      {toasts.map((toast) => (
        <ToastNotification
          key={toast.id}
          id={toast.id}
          message={toast.message}
          variant={toast.variant}
          title={toast.title}
          timestamp={toast.timestamp}
          autoHide={toast.autoHide}
          delay={toast.delay}
          showTimestamp={toast.showTimestamp}
          onClose={removeToast}
        />
      ))}
    </ToastContainer>
  );
};

// Standalone Toast Component (for direct usage without context)
const StandaloneToast = ({ 
  show = false,
  onClose,
  message,
  variant = 'info',
  title = null,
  autoHide = true,
  delay = 5000,
  position = 'top-end',
  ...props
}) => {
  return (
    <ToastContainer
      position={position}
      className="position-fixed"
      style={{ zIndex: 9999 }}
    >
      <ToastNotification
        message={message}
        variant={variant}
        title={title}
        autoHide={autoHide}
        delay={delay}
        onClose={onClose}
        {...props}
      />
    </ToastContainer>
  );
};

export { StandaloneToast };
export default ToastNotification;
