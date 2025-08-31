import * as Yup from 'yup';

// Employee creation validation schema
export const employeeCreateSchema = Yup.object({
  // User details
  username: Yup.string()
    .min(3, 'Username must be at least 3 characters')
    .max(50, 'Username must not exceed 50 characters')
    .required('Username is required'),
  email: Yup.string()
    .email('Invalid email format')
    .required('Email is required'),
  password: Yup.string()
    .min(6, 'Password must be at least 6 characters')
    .required('Password is required'),
  
  // Employee details
  firstName: Yup.string()
    .min(2, 'First name must be at least 2 characters')
    .max(50, 'First name must not exceed 50 characters')
    .required('First name is required'),
  lastName: Yup.string()
    .min(2, 'Last name must be at least 2 characters')
    .max(50, 'Last name must not exceed 50 characters')
    .required('Last name is required'),
  dateOfBirth: Yup.date()
    .max(new Date(), 'Date of birth cannot be in the future')
    .required('Date of birth is required'),
  phone: Yup.string()
    .matches(/^[0-9+\-\s()]+$/, 'Invalid phone number format')
    .min(10, 'Phone number must be at least 10 characters')
    .max(15, 'Phone number must not exceed 15 characters')
    .required('Phone number is required'),
  address: Yup.string()
    .min(10, 'Address must be at least 10 characters')
    .max(200, 'Address must not exceed 200 characters')
    .required('Address is required'),
  jobId: Yup.string()
    .required('Job role is required'),
  departmentId: Yup.string()
    .required('Department is required'),
  leaveBalance: Yup.number()
    .min(0, 'Leave balance cannot be negative')
    .max(365, 'Leave balance cannot exceed 365 days')
    .required('Leave balance is required')
});

// Employee update validation schema (without password and username)
export const employeeUpdateSchema = Yup.object({
  email: Yup.string()
    .email('Invalid email format')
    .required('Email is required'),
  firstName: Yup.string()
    .min(2, 'First name must be at least 2 characters')
    .max(50, 'First name must not exceed 50 characters')
    .required('First name is required'),
  lastName: Yup.string()
    .min(2, 'Last name must be at least 2 characters')
    .max(50, 'Last name must not exceed 50 characters')
    .required('Last name is required'),
  dateOfBirth: Yup.date()
    .max(new Date(), 'Date of birth cannot be in the future')
    .required('Date of birth is required'),
  phone: Yup.string()
    .matches(/^[0-9+\-\s()]+$/, 'Invalid phone number format')
    .min(10, 'Phone number must be at least 10 characters')
    .max(15, 'Phone number must not exceed 15 characters')
    .required('Phone number is required'),
  address: Yup.string()
    .min(10, 'Address must be at least 10 characters')
    .max(200, 'Address must not exceed 200 characters')
    .required('Address is required'),
  jobId: Yup.string()
    .required('Job role is required'),
  departmentId: Yup.string()
    .required('Department is required'),
  leaveBalance: Yup.number()
    .min(0, 'Leave balance cannot be negative')
    .max(365, 'Leave balance cannot exceed 365 days')
    .required('Leave balance is required')
});

// Salary structure validation schema
export const salaryStructureSchema = Yup.object({
  baseSalary: Yup.number()
    .positive('Base salary must be a positive number')
    .min(1000, 'Base salary must be at least 1000')
    .max(10000000, 'Base salary must not exceed 10,000,000')
    .required('Base salary is required'),
  bonusType: Yup.string(),
  bonusAmount: Yup.number()
    .when('bonusType', {
      is: (bonusType) => bonusType && bonusType.length > 0,
      then: () => Yup.number()
        .positive('Bonus amount must be a positive number')
        .required('Bonus amount is required when bonus type is selected'),
      otherwise: () => Yup.number()
    }),
  effectiveFrom: Yup.date()
    .required('Effective from date is required'),
  effectiveTo: Yup.date()
    .min(Yup.ref('effectiveFrom'), 'Effective to date must be after effective from date')
    .nullable()
});
