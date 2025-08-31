import React, { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';

const AuthContext = createContext();

// Custom hook to use the AuthContext
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

// AuthProvider component
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [role, setRole] = useState(null);
  const [loading, setLoading] = useState(true);

  // API base URL
  const API_BASE_URL = 'http://localhost:8080/api/v1';

  // Initialize auth state from localStorage on app load
  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    const storedUser = localStorage.getItem('user');
    const storedRole = localStorage.getItem('role');

    if (storedToken && storedUser && storedRole) {
      setToken(storedToken);
      setUser(JSON.parse(storedUser));
      setRole(storedRole);
    }
    setLoading(false);
  }, []);

  // Login function
  const login = async (credentials) => {
    try {
      const response = await axios.post(`${API_BASE_URL}/auth/login`, credentials);
      
      if (response.data.success) {
        const { data } = response.data;
        const { accessToken, user: userData } = data;
        
        // Store in state
        setToken(accessToken);
        setUser(userData);
        setRole(userData.role);
        
        // Store in localStorage
        localStorage.setItem('token', accessToken);
        localStorage.setItem('user', JSON.stringify(userData));
        localStorage.setItem('role', userData.role);
        
        return { success: true, message: response.data.message };
      } else {
        return { success: false, message: response.data.message || 'Login failed' };
      }
    } catch (error) {
      console.error('Login error:', error);
      
      if (error.response?.data?.message) {
        return { success: false, message: error.response.data.message };
      }
      
      return { 
        success: false, 
        message: error.response?.status === 401 
          ? 'Invalid username or password' 
          : 'Network error. Please try again.' 
      };
    }
  };

  // Logout function
  const logout = () => {
    // Clear state
    setToken(null);
    setUser(null);
    setRole(null);
    
    // Clear localStorage
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('role');
    
    // Redirect to login page
    window.location.href = '/login';
  };

  // Check if user is authenticated
  const isAuthenticated = () => {
    return !!token && !!user;
  };

  // Check if user has required role
  const hasRole = (requiredRole) => {
    if (!role) return false;
    
    // Convert to uppercase for consistency
    const userRole = role.toUpperCase();
    const reqRole = requiredRole.toUpperCase();
    
    return userRole === reqRole;
  };

  // Check if user is admin
  const isAdmin = () => {
    return hasRole('ADMIN');
  };

  // Check if user is employee
  const isEmployee = () => {
    return hasRole('EMPLOYEE');
  };

  // Get user display name
  const getUserDisplayName = () => {
    if (!user) return '';
    return user.username || user.email || 'User';
  };

  // Context value
  const value = {
    // State
    user,
    token,
    role,
    loading,
    
    // Functions
    login,
    logout,
    isAuthenticated,
    hasRole,
    isAdmin,
    isEmployee,
    getUserDisplayName,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export default AuthContext;
