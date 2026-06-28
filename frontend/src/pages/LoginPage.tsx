import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, ApiError } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { Role } from '../types';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await api.login(email, password);
      const authUser = {
        token: res.token,
        role: res.role as Role,
        userId: res.userId,
        fullName: res.fullName,
        customerId: res.customerId,
      };
      localStorage.setItem('keystone_token', res.token);
      localStorage.setItem('keystone_auth', JSON.stringify(authUser));
      login(authUser);
      const routes: Record<Role, string> = {
        MANAGER: '/dashboard',
        DISPATCHER: '/board',
        TECHNICIAN: '/my-jobs',
        CUSTOMER: '/my-requests',
      };
      navigate(routes[authUser.role] || '/');
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
      <div className="login-page">
        <div className="login-card card">
          <h1>KEYSTONE</h1>
          <p className="subtitle">Field Service Management Platform</p>
          {error && <div className="error-banner">{error}</div>}
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="email">Email</label>
              <input
                  id="email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  autoComplete="username"
              />
            </div>
            <div className="form-group">
              <label htmlFor="password">Password</label>
              <input
                  id="password"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  autoComplete="current-password"
              />
            </div>
            <button type="submit" className="btn-primary" disabled={loading} style={{ width: '100%' }}>
              {loading ? 'Signing in…' : 'Sign in'}
            </button>
          </form>
          <p style={{ marginTop: '1.5rem', fontSize: '0.8rem', color: 'var(--text-muted)', textAlign: 'center' }}>
            Seed password: <code>password123</code>
          </p>
        </div>
      </div>
  );
}
