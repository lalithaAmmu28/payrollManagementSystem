import axios from 'axios';

// API base URL
export const API_BASE_URL = 'http://localhost:8080/api/v1';

// Create axios instance with default configuration
const apiService = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000, // 10 seconds timeout
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - Add auth token to every request
apiService.interceptors.request.use(
  (config) => {
    // Get token from localStorage
    const token = localStorage.getItem('token');
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    // Log request for debugging (remove in production)
   // console.log(`API Request: ${config.method?.toUpperCase()} ${config.url}`);
    
    return config;
  },
  (error) => {
    console.error('Request interceptor error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor - Handle auth errors and token expiration
apiService.interceptors.response.use(
  (response) => {
    // Log successful response for debugging (remove in production)
    console.log(`API Response: ${response.status} ${response.config.url}`);
    return response;
  },
  (error) => {
    console.error('API Error:', error);
    
    if (error.response) {
      const { status, data } = error.response;
      
      // Handle authentication errors
      if (status === 401 || status === 403) {
        console.warn('Authentication error - logging out user');
        
        // Clear auth data from localStorage
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        localStorage.removeItem('role');
        
        // Redirect to login page
        if (window.location.pathname !== '/login') {
          window.location.href = '/login';
        }
        
        return Promise.reject({
          ...error,
          message: 'Session expired. Please login again.'
        });
      }
      
      // Handle other HTTP errors
      const errorMessage = data?.message || `HTTP ${status} Error`;
      return Promise.reject({
        ...error,
        message: errorMessage
      });
    }
    
    // Handle network errors
    if (error.code === 'ECONNABORTED') {
      return Promise.reject({
        ...error,
        message: 'Request timeout. Please try again.'
      });
    }
    
    if (!error.response) {
      return Promise.reject({
        ...error,
        message: 'Network error. Please check your connection.'
      });
    }
    
    return Promise.reject(error);
  }
);

// API helper functions for common operations
export const apiHelpers = {
  // GET request
  get: (url, config = {}) => apiService.get(url, config),
  
  // POST request
  post: (url, data = {}, config = {}) => apiService.post(url, data, config),
  
  // PUT request
  put: (url, data = {}, config = {}) => apiService.put(url, data, config),
  
  // PATCH request
  patch: (url, data = {}, config = {}) => apiService.patch(url, data, config),
  
  // DELETE request
  delete: (url, config = {}) => apiService.delete(url, config),
  
  // Upload file (multipart/form-data)
  upload: (url, formData, config = {}) => {
    return apiService.post(url, formData, {
      ...config,
      headers: {
        ...config.headers,
        'Content-Type': 'multipart/form-data',
      },
    });
  },
};

// Auth API endpoints
export const authAPI = {
  login: (credentials) => apiService.post('/auth/login', credentials),
  
  // Get current user profile
  getCurrentUser: () => apiService.get('/users/me'),
  
  // Change password
  changePassword: (passwordData) => apiService.patch('/users/me/password', passwordData),
};

// Employee API endpoints
export const employeeAPI = {
  // Get all employees (Admin only)
  getAll: () => apiService.get('/employees'),
  
  // Get employee by ID (Admin only)
  getById: (id) => apiService.get(`/employees/${id}`),
  
  // Create employee (Admin only)
  create: (employeeData) => apiService.post('/employees', employeeData),
  
  // Update employee (Admin only)
  update: (id, employeeData) => apiService.put(`/employees/${id}`, employeeData),
  
  // Delete employee (Admin only)
  delete: (id) => apiService.delete(`/employees/${id}`),
  
  // Update own profile
  updateProfile: (profileData) => apiService.patch('/employees/me', profileData),
  
  // Salary structure endpoints
  getSalaryHistory: (id) => apiService.get(`/employees/${id}/salary-structures`),
  assignSalary: (id, salaryData) => apiService.post(`/employees/${id}/salary-structures`, salaryData),
  getCurrentSalary: (id) => apiService.get(`/employees/${id}/salary-structures/current`),
};

// Department API endpoints
export const departmentAPI = {
  getAll: () => apiService.get('/departments'),
  getById: (id) => apiService.get(`/departments/${id}`),
  create: (departmentData) => apiService.post('/departments', departmentData),
  update: (id, departmentData) => apiService.put(`/departments/${id}`, departmentData),
  delete: (id) => apiService.delete(`/departments/${id}`),
};

// Job Role API endpoints
export const jobRoleAPI = {
  getAll: () => apiService.get('/jobs'),
  getById: (id) => apiService.get(`/jobs/${id}`),
  create: (jobData) => apiService.post('/jobs', jobData),
  update: (id, jobData) => apiService.put(`/jobs/${id}`, jobData),
  delete: (id) => apiService.delete(`/jobs/${id}`),
};

// Leave API endpoints
export const leaveAPI = {
  // Employee endpoints
  apply: (leaveData) => apiService.post('/leave-requests', leaveData),
  getMyRequests: () => apiService.get('/leave-requests/my'),
  cancel: (id) => apiService.delete(`/leave-requests/${id}`),
  
  // Admin endpoints
  getAll: (status = null) => {
    const params = status ? { status } : {};
    return apiService.get('/leave-requests', { params });
  },
  getById: (id) => apiService.get(`/leave-requests/${id}`),
  updateStatus: (id, statusData) => apiService.patch(`/leave-requests/${id}/status`, statusData),
};

// Payroll API endpoints
export const payrollAPI = {
  // Admin endpoints
  createRun: (runData) => apiService.post('/payroll/runs', runData),
  getAllRuns: () => apiService.get('/payroll/runs'),
  getRunById: (id) => apiService.get(`/payroll/runs/${id}`),
  processRun: (id) => apiService.post(`/payroll/runs/${id}/process`),
  lockRun: (id) => apiService.post(`/payroll/runs/${id}/lock`),
  getRunItems: (id) => apiService.get(`/payroll/runs/${id}/items`),
  getRunStatistics: (id) => apiService.get(`/payroll/runs/${id}/statistics`),
  
  // Employee endpoints
  getMyPayslips: () => apiService.get('/payroll/payslips'),
  getMyPayslip: (runId) => apiService.get(`/payroll/payslips/${runId}`),
  
  // Admin employee access
  getEmployeePayslips: (empId) => apiService.get(`/payroll/employees/${empId}/payslips`),
  getEmployeePayslip: (empId, runId) => apiService.get(`/payroll/employees/${empId}/payslips/${runId}`),
};

// Reports API endpoints
export const reportsAPI = {
  // Payroll reports
  getPayrollSummary: (params = {}) => apiService.get('/reports/payroll-summary', { params }),
  getOverallPayrollSummary: (params = {}) => apiService.get('/reports/payroll-summary/overall', { params }),
  getDepartmentCosts: (params = {}) => apiService.get('/reports/department-cost', { params }),
  getTopSpendingDepartments: (params = {}) => apiService.get('/reports/departments/top-spending', { params }),
  
  // Leave reports
  getLeaveTrends: (params = {}) => apiService.get('/reports/leave-trends', { params }),
  getLeaveTrendsByDepartment: (params = {}) => apiService.get('/reports/leave-trends/by-department', { params }),
  getMonthlyLeaveStats: (params = {}) => apiService.get('/reports/leave-trends/monthly', { params }),
  getTopLeaveTakers: (params = {}) => apiService.get('/reports/employees/top-leave-takers', { params }),
  
  // Analytics dashboard
  getAnalyticsDashboard: (params = {}) => apiService.get('/reports/analytics/dashboard', { params }),
};

// Export the main axios instance as default
export default apiService;
