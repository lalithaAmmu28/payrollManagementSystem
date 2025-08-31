import React from 'react';
import { Modal, Button } from 'react-bootstrap';
import { ButtonSpinner } from './LoadingSpinner';

const ConfirmationModal = ({
  show = false,
  onHide,
  onConfirm,
  title = 'Confirm Action',
  message = 'Are you sure you want to perform this action?',
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  confirmVariant = 'primary',
  cancelVariant = 'outline-secondary',
  isLoading = false,
  size = 'md',
  centered = true,
  icon = null,
  variant = 'default', // 'default', 'danger', 'warning', 'success'
}) => {
  const getVariantStyles = () => {
    switch (variant) {
      case 'danger':
        return {
          iconColor: 'var(--error-color)',
          icon: icon || '⚠️',
          confirmVariant: 'danger',
        };
      case 'warning':
        return {
          iconColor: 'var(--warning-color)',
          icon: icon || '⚠️',
          confirmVariant: 'warning',
        };
      case 'success':
        return {
          iconColor: 'var(--success-color)',
          icon: icon || '✅',
          confirmVariant: 'success',
        };
      default:
        return {
          iconColor: 'var(--accent-color)',
          icon: icon || '❓',
          confirmVariant: confirmVariant,
        };
    }
  };

  const variantStyles = getVariantStyles();

  const handleConfirm = () => {
    if (onConfirm && !isLoading) {
      onConfirm();
    }
  };

  const handleCancel = () => {
    if (onHide && !isLoading) {
      onHide();
    }
  };

  return (
    <Modal
      show={show}
      onHide={handleCancel}
      size={size}
      centered={centered}
      backdrop={isLoading ? 'static' : true}
      keyboard={!isLoading}
    >
      <Modal.Header 
        closeButton={!isLoading}
        style={{ borderBottom: '1px solid var(--borders-color)' }}
      >
        <Modal.Title className="d-flex align-items-center">
          {variantStyles.icon && (
            <span 
              className="me-2"
              style={{ 
                fontSize: '1.5rem',
                color: variantStyles.iconColor
              }}
            >
              {variantStyles.icon}
            </span>
          )}
          {title}
        </Modal.Title>
      </Modal.Header>

      <Modal.Body className="py-4">
        <div className="text-center">
          {typeof message === 'string' ? (
            <p className="mb-0" style={{ fontSize: '1.1rem', lineHeight: '1.6' }}>
              {message}
            </p>
          ) : (
            message
          )}
        </div>
      </Modal.Body>

      <Modal.Footer 
        className="d-flex justify-content-center gap-3"
        style={{ borderTop: '1px solid var(--borders-color)' }}
      >
        <Button
          variant={cancelVariant}
          onClick={handleCancel}
          disabled={isLoading}
          style={{ 
            minWidth: '100px',
            borderRadius: 'var(--border-radius)'
          }}
        >
          {cancelText}
        </Button>
        
        <Button
          variant={variantStyles.confirmVariant}
          onClick={handleConfirm}
          disabled={isLoading}
          style={{ 
            minWidth: '100px',
            borderRadius: 'var(--border-radius)'
          }}
        >
          {isLoading ? (
            <ButtonSpinner text={confirmText} />
          ) : (
            confirmText
          )}
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

// Specific confirmation modal variants
export const DeleteConfirmationModal = ({
  show,
  onHide,
  onConfirm,
  itemName = 'item',
  isLoading = false,
  ...props
}) => (
  <ConfirmationModal
    show={show}
    onHide={onHide}
    onConfirm={onConfirm}
    title="Delete Confirmation"
    message={
      <div>
        <p className="mb-2">Are you sure you want to delete this {itemName}?</p>
        <p className="mb-0 text-danger">
          <small><strong>This action cannot be undone.</strong></small>
        </p>
      </div>
    }
    confirmText="Delete"
    cancelText="Cancel"
    variant="danger"
    isLoading={isLoading}
    {...props}
  />
);

export const SaveConfirmationModal = ({
  show,
  onHide,
  onConfirm,
  message = 'Do you want to save your changes?',
  isLoading = false,
  ...props
}) => (
  <ConfirmationModal
    show={show}
    onHide={onHide}
    onConfirm={onConfirm}
    title="Save Changes"
    message={message}
    confirmText="Save"
    cancelText="Cancel"
    variant="success"
    isLoading={isLoading}
    {...props}
  />
);

export const LogoutConfirmationModal = ({
  show,
  onHide,
  onConfirm,
  isLoading = false,
  ...props
}) => (
  <ConfirmationModal
    show={show}
    onHide={onHide}
    onConfirm={onConfirm}
    title="Logout Confirmation"
    message="Are you sure you want to logout?"
    confirmText="Logout"
    cancelText="Cancel"
    variant="warning"
    isLoading={isLoading}
    {...props}
  />
);

export const ProcessConfirmationModal = ({
  show,
  onHide,
  onConfirm,
  processName = 'process',
  isLoading = false,
  ...props
}) => (
  <ConfirmationModal
    show={show}
    onHide={onHide}
    onConfirm={onConfirm}
    title={`${processName} Confirmation`}
    message={`Are you sure you want to ${processName.toLowerCase()}? This action may take some time.`}
    confirmText={processName}
    cancelText="Cancel"
    variant="default"
    isLoading={isLoading}
    {...props}
  />
);

export default ConfirmationModal;

