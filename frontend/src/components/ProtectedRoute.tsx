import { Navigate, Outlet } from 'react-router-dom';
import { useAuth, hasRole } from '../context/AuthContext';
import { Role } from '../types';

export function ProtectedRoute({ roles }: { roles?: Role[] }) {
  const { isAuthenticated, user } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (roles && user && !hasRole(user, ...roles)) {
    const home: Record<Role, string> = {
      MANAGER: '/dashboard',
      DISPATCHER: '/board',
      TECHNICIAN: '/my-jobs',
      CUSTOMER: '/my-requests',
    };
    return <Navigate to={home[user.role]} replace />;
  }

  return <Outlet />;
}

export function PublicRoute() {
  const { isAuthenticated, user } = useAuth();
  if (isAuthenticated && user) {
    const home: Record<Role, string> = {
      MANAGER: '/dashboard',
      DISPATCHER: '/board',
      TECHNICIAN: '/my-jobs',
      CUSTOMER: '/my-requests',
    };
    return <Navigate to={home[user.role]} replace />;
  }
  return <Outlet />;
}
