import React from 'react';
import './styles/SkeletonLoader.css';
import { Card, Row, Col } from 'react-bootstrap';

const SkeletonLoader = ({ 
  width = '100%', 
  height = '1rem', 
  className = '',
  rounded = false,
  animated = true
}) => {
  const skeletonStyles = {
    width,
    height,
    backgroundColor: '#e9ecef',
    borderRadius: rounded ? '50%' : 'var(--border-radius)',
    animation: animated ? 'skeleton-pulse 1.5s ease-in-out infinite alternate' : 'none',
  };

  return (
    <div 
      className={`skeleton-loader ${className}`} 
      style={skeletonStyles}
      aria-label="Loading..."
    />
  );
};

// Table skeleton loader
export const TableSkeleton = ({ rows = 5, columns = 4 }) => {
  return (
    <tbody>
      {Array.from({ length: rows }).map((_, rowIndex) => (
        <tr key={rowIndex}>
          {Array.from({ length: columns }).map((_, colIndex) => (
            <td key={colIndex} style={{ padding: '1rem' }}>
              <SkeletonLoader height="1rem" />
            </td>
          ))}
        </tr>
      ))}
    </tbody>
  );
};

// Card skeleton loader
export const CardSkeleton = ({ count = 1 }) => {
  return (
    <>
      {Array.from({ length: count }).map((_, index) => (
        <Col key={index} md={6} lg={4} className="mb-4">
          <Card className="h-100">
            <Card.Body>
              {/* Card title skeleton */}
              <SkeletonLoader width="70%" height="1.5rem" className="mb-3" />
              
              {/* Card content skeleton */}
              <SkeletonLoader width="100%" height="1rem" className="mb-2" />
              <SkeletonLoader width="85%" height="1rem" className="mb-2" />
              <SkeletonLoader width="60%" height="1rem" className="mb-3" />
              
              {/* Button skeleton */}
              <SkeletonLoader width="120px" height="2.5rem" />
            </Card.Body>
          </Card>
        </Col>
      ))}
    </>
  );
};

// Profile skeleton loader
export const ProfileSkeleton = () => {
  return (
    <div className="content-card">
      <Row>
        <Col md={4} className="text-center mb-4">
          {/* Avatar skeleton */}
          <SkeletonLoader 
            width="120px" 
            height="120px" 
            rounded={true} 
            className="mx-auto mb-3" 
          />
          {/* Name skeleton */}
          <SkeletonLoader width="80%" height="1.5rem" className="mx-auto mb-2" />
          {/* Role skeleton */}
          <SkeletonLoader width="60%" height="1rem" className="mx-auto" />
        </Col>
        
        <Col md={8}>
          {/* Form fields skeleton */}
          <div className="mb-3">
            <SkeletonLoader width="100px" height="1rem" className="mb-2" />
            <SkeletonLoader width="100%" height="2.5rem" />
          </div>
          
          <div className="mb-3">
            <SkeletonLoader width="80px" height="1rem" className="mb-2" />
            <SkeletonLoader width="100%" height="2.5rem" />
          </div>
          
          <div className="mb-3">
            <SkeletonLoader width="120px" height="1rem" className="mb-2" />
            <SkeletonLoader width="100%" height="2.5rem" />
          </div>
          
          <div className="mb-4">
            <SkeletonLoader width="90px" height="1rem" className="mb-2" />
            <SkeletonLoader width="100%" height="6rem" />
          </div>
          
          {/* Button skeleton */}
          <SkeletonLoader width="150px" height="2.5rem" />
        </Col>
      </Row>
    </div>
  );
};

// List item skeleton
export const ListSkeleton = ({ count = 5 }) => {
  return (
    <div className="content-card">
      {Array.from({ length: count }).map((_, index) => (
        <div key={index} className="d-flex align-items-center p-3 border-bottom">
          {/* Avatar skeleton */}
          <SkeletonLoader 
            width="48px" 
            height="48px" 
            rounded={true} 
            className="me-3" 
          />
          
          <div className="flex-grow-1">
            {/* Title skeleton */}
            <SkeletonLoader width="70%" height="1.25rem" className="mb-2" />
            {/* Subtitle skeleton */}
            <SkeletonLoader width="50%" height="1rem" />
          </div>
          
          {/* Action skeleton */}
          <SkeletonLoader width="80px" height="2rem" />
        </div>
      ))}
    </div>
  );
};

// Dashboard stats skeleton
export const StatsSkeleton = () => {
  return (
    <Row>
      {Array.from({ length: 4 }).map((_, index) => (
        <Col key={index} md={6} lg={3} className="mb-4">
          <Card className="h-100">
            <Card.Body className="text-center">
              {/* Icon skeleton */}
              <SkeletonLoader 
                width="48px" 
                height="48px" 
                rounded={true} 
                className="mx-auto mb-3" 
              />
              {/* Value skeleton */}
              <SkeletonLoader width="60%" height="2rem" className="mx-auto mb-2" />
              {/* Label skeleton */}
              <SkeletonLoader width="80%" height="1rem" className="mx-auto" />
            </Card.Body>
          </Card>
        </Col>
      ))}
    </Row>
  );
};

// CSS for skeleton animation (add to custom.css)
const SkeletonStyles = () => (
  <style jsx>{`
    @keyframes skeleton-pulse {
      0% {
        background-color: #e9ecef;
      }
      100% {
        background-color: #f8f9fa;
      }
    }
    
    .skeleton-loader {
      display: block;
    }
  `}</style>
);

export default SkeletonLoader;
