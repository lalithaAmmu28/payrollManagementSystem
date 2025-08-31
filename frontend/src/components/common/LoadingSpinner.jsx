import React from 'react';
import { Spinner } from 'react-bootstrap';
import './styles/LoadingSpinner.css';

const LoadingSpinner = ({ 
  size = 'sm', 
  variant = 'primary', 
  className = '', 
  text = '',
  inline = false,
  fullPage = false
}) => {
  const spinnerElement = (
    <Spinner
      as="span"
      animation="border"
      size={size}
      variant={variant}
      className={`spinner-custom ${className}`}
      role="status"
      aria-hidden="true"
    />
  );

  // Full page loading spinner
  if (fullPage) {
    return (
      <div 
        className="d-flex justify-content-center align-items-center position-fixed w-100 h-100"
        style={{
          top: 0,
          left: 0,
          backgroundColor: 'rgba(248, 249, 250, 0.8)',
          zIndex: 9999,
        }}
      >
        <div className="text-center">
          <Spinner
            animation="border"
            variant={variant}
            style={{ width: '3rem', height: '3rem' }}
          />
          {text && (
            <p className="mt-3 text-muted-custom">{text}</p>
          )}
        </div>
      </div>
    );
  }

  // Inline spinner (for buttons)
  if (inline) {
    return (
      <>
        {spinnerElement}
        {text && <span className="ms-2">{text}</span>}
      </>
    );
  }

  // Block spinner (centered in container)
  return (
    <div className={`text-center py-4 ${className}`}>
      {spinnerElement}
      {text && (
        <p className="mt-2 mb-0 text-muted-custom">{text}</p>
      )}
    </div>
  );
};

// Specific spinner variants for common use cases
export const ButtonSpinner = ({ text = 'Loading...', className = '' }) => (
  <LoadingSpinner 
    size="sm" 
    inline={true} 
    text={text} 
    className={className}
  />
);

export const PageSpinner = ({ text = 'Loading...', className = '' }) => (
  <LoadingSpinner 
    size="lg" 
    text={text} 
    className={className}
  />
);

export const FullPageSpinner = ({ text = 'Loading...' }) => (
  <LoadingSpinner 
    fullPage={true} 
    text={text}
  />
);

export const TableSpinner = ({ text = 'Loading data...', className = '' }) => (
  <tr>
    <td colSpan="100%" className="text-center py-4">
      <LoadingSpinner text={text} className={className} />
    </td>
  </tr>
);

export default LoadingSpinner;
