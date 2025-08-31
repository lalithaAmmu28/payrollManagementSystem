import React from 'react'
import { BrowserRouter as Router } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { ToastProvider } from './components/common/ToastNotification'
import AppRoutes from './routes/AppRoutes'

function App() {
  return (
    <Router>
      <AuthProvider>
        <ToastProvider>
          <div className="App">
            <AppRoutes />
          </div>
        </ToastProvider>
      </AuthProvider>
    </Router>
  )
}

export default App
