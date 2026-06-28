import { NavLink } from 'react-router-dom';
import { useAuth, hasRole } from '../context/AuthContext';

export default function Layout({ children }: { children: React.ReactNode }) {
  const { user, logout } = useAuth();

  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="sidebar-logo">KEYSTONE</div>
        <div className="sidebar-sub">Meridian Facilities Mgmt</div>
        <nav>
          {hasRole(user, 'MANAGER') && (
            <NavLink to="/dashboard" className={({ isActive }) => (isActive ? 'active' : '')}>
              Dashboard
            </NavLink>
          )}
          {hasRole(user, 'DISPATCHER', 'MANAGER') && (
            <>
              <NavLink to="/board" className={({ isActive }) => (isActive ? 'active' : '')}>
                Dispatch Board
              </NavLink>
              <NavLink to="/work-orders" className={({ isActive }) => (isActive ? 'active' : '')}>
                Work Orders
              </NavLink>
              <NavLink to="/customers" className={({ isActive }) => (isActive ? 'active' : '')}>
                Customers
              </NavLink>
            </>
          )}
          {hasRole(user, 'TECHNICIAN') && (
            <NavLink to="/my-jobs" className={({ isActive }) => (isActive ? 'active' : '')}>
              My Jobs
            </NavLink>
          )}
          {hasRole(user, 'CUSTOMER') && (
            <>
              <NavLink to="/my-requests" className={({ isActive }) => (isActive ? 'active' : '')}>
                My Requests
              </NavLink>
              <NavLink to="/submit-request" className={({ isActive }) => (isActive ? 'active' : '')}>
                Submit Request
              </NavLink>
            </>
          )}
          {hasRole(user, 'MANAGER') && (
            <>
              <NavLink to="/users" className={({ isActive }) => (isActive ? 'active' : '')}>
                Users
              </NavLink>
              <NavLink to="/parts" className={({ isActive }) => (isActive ? 'active' : '')}>
                Parts
              </NavLink>
            </>
          )}
          <NavLink to="/notifications" className={({ isActive }) => (isActive ? 'active' : '')}>
            Notifications
          </NavLink>
        </nav>
        <div style={{ marginTop: 'auto', paddingTop: '1rem', borderTop: '1px solid var(--border)' }}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.5rem' }}>
            {user?.fullName}
            <br />
            <span style={{ textTransform: 'capitalize' }}>{user?.role?.toLowerCase()}</span>
          </div>
          <button className="btn-secondary btn-sm" onClick={logout} style={{ width: '100%' }}>
            Sign out
          </button>
        </div>
      </aside>
      <main className="main">{children}</main>
    </div>
  );
}
