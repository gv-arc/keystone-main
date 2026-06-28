import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute, PublicRoute } from './components/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import BoardPage from './pages/BoardPage';
import WorkOrdersPage from './pages/WorkOrdersPage';
import WorkOrderDetailPage from './pages/WorkOrderDetailPage';
import WorkOrderCreatePage from './pages/WorkOrderCreatePage';
import CustomersPage from './pages/CustomersPage';
import MyJobsPage from './pages/MyJobsPage';
import MyRequestsPage from './pages/MyRequestsPage';
import SubmitRequestPage from './pages/SubmitRequestPage';
import UsersPage from './pages/UsersPage';
import PartsPage from './pages/PartsPage';
import NotificationsPage from './pages/NotificationsPage';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<PublicRoute />}>
            <Route path="/login" element={<LoginPage />} />
          </Route>

          <Route path="/submit-request" element={<SubmitRequestPage />} />

          <Route element={<ProtectedRoute />}>
            <Route path="/notifications" element={<NotificationsPage />} />
            <Route path="/work-orders/:id" element={<WorkOrderDetailPage />} />
          </Route>

          <Route element={<ProtectedRoute roles={['MANAGER']} />}>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/users" element={<UsersPage />} />
            <Route path="/parts" element={<PartsPage />} />
          </Route>

          <Route element={<ProtectedRoute roles={['DISPATCHER', 'MANAGER']} />}>
            <Route path="/board" element={<BoardPage />} />
            <Route path="/work-orders" element={<WorkOrdersPage />} />
            <Route path="/work-orders/new" element={<WorkOrderCreatePage />} />
            <Route path="/customers" element={<CustomersPage />} />
          </Route>

          <Route element={<ProtectedRoute roles={['TECHNICIAN']} />}>
            <Route path="/my-jobs" element={<MyJobsPage />} />
          </Route>

          <Route element={<ProtectedRoute roles={['CUSTOMER']} />}>
            <Route path="/my-requests" element={<MyRequestsPage />} />
          </Route>

          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
