import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { Notification } from '../types';
import Layout from '../components/Layout';
import { formatDate } from '../components/Badges';

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api.getNotifications()
      .then((res) => setNotifications(res.content))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  return (
    <Layout>
      <div className="page-header"><h1>Notifications</h1></div>
      {error && <div className="error-banner">{error}</div>}
      {loading ? (
        <div className="loading">Loading…</div>
      ) : notifications.length === 0 ? (
        <div className="empty-state">No notifications</div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
          {notifications.map((n) => (
            <div key={n.id} className="card" style={{ opacity: n.read ? 0.7 : 1 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', gap: '1rem' }}>
                <div>
                  <span className="badge" style={{ marginBottom: '0.35rem' }}>{n.type.replace('_', ' ')}</span>
                  <p>{n.message}</p>
                  {n.workOrderId && (
                    <Link to={`/work-orders/${n.workOrderId}`}>{n.workOrderCode}</Link>
                  )}
                </div>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>
                  {formatDate(n.createdAt)}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </Layout>
  );
}
