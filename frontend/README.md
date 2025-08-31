# Payroll Management System - Frontend

A modern React application built with Vite for managing payroll, employees, leave requests, and analytics.

## 🚀 Tech Stack

- **React 18+** - Modern React with hooks
- **Vite** - Fast development and build tool
- **Bootstrap 5** - UI component framework
- **React Bootstrap** - Bootstrap components for React
- **React Router Dom** - Client-side routing
- **Axios** - HTTP client for API calls
- **Formik + Yup** - Form handling and validation

## 🎨 Design System

- **Font**: Poppins (Google Fonts)
- **Colors**: Professional muted palette
- **Components**: Consistent 8px border radius
- **Animations**: Smooth transitions and loading states
- **Responsive**: Mobile-first design approach

## 📁 Project Structure

```
src/
├── api/              # API services and configuration
├── assets/styles/    # Custom CSS and design system
├── components/common/# Reusable UI components
├── context/          # React context providers
├── hooks/           # Custom React hooks
├── layouts/         # Page layout components
├── pages/
│   ├── admin/       # Admin-only pages
│   └── employee/    # Employee pages
├── routes/          # Route definitions and guards
└── validation/      # Form validation schemas
```

## 🏃‍♂️ Getting Started

1. Install dependencies:
   ```bash
   npm install
   ```

2. Start development server:
   ```bash
   npm run dev
   ```

3. Build for production:
   ```bash
   npm run build
   ```

## 🔗 Backend Integration

The frontend connects to the backend API at `http://localhost:8080/api/v1`

### Key Features to be Implemented:
- JWT Authentication
- Role-based access (Admin/Employee)
- Employee management
- Leave request system
- Payroll processing
- Analytics dashboard

## 📋 Development Modules

1. **Authentication** - Login, JWT handling
2. **Layout & Navigation** - Responsive sidebar navigation
3. **Employee Profile** - Self-service profile management
4. **Employee Management** - Admin CRUD operations
5. **Organization** - Departments & Job Roles
6. **Leave Management** - Request/approval workflow
7. **Payroll** - Processing and payslip generation
8. **Reports** - Analytics and insights

---

Built with ❤️ using modern web technologies