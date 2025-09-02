import React from 'react';
import { useAuth } from '../../context/AuthContext';

const AdminDashboard = () => {
  const { user } = useAuth();

  return (
    <div className="content-card fade-in">
      <div className="mb-4">
        <h1>Welcome to Admin Portal</h1>
        <p className="text-muted-custom mb-0">
          Manage your organization from this central administration panel.
        </p>
      </div>

      <div className="mt-4 p-3 bg-light-custom rounded">
        <h3>Quick Info</h3>
        <p><strong>User ID:</strong> {user?.userId}</p>
        <p><strong>Username:</strong> {user?.username}</p>
        <p><strong>Email:</strong> {user?.email}</p>
        <p><strong>Role:</strong> {user?.role}</p>
      </div>
    </div>
  );
};

export default AdminDashboard;
