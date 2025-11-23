import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Scraping from './pages/Scraping';
import Ilanlar from './pages/Ilanlar';
import Mesajlasma from './pages/Mesajlasma';

// Protected Route Component
function ProtectedRoute({ children }) {
  const token = localStorage.getItem('token');
  return token ? children : <Navigate to="/login" replace />;
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/scraping"
          element={
            <ProtectedRoute>
              <Scraping />
            </ProtectedRoute>
          }
        />
        <Route
          path="/ilanlar"
          element={
            <ProtectedRoute>
              <Ilanlar />
            </ProtectedRoute>
          }
        />
        <Route
          path="/mesajlasma"
          element={
            <ProtectedRoute>
              <Mesajlasma />
            </ProtectedRoute>
          }
        />
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
