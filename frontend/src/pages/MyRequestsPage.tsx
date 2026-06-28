import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { WorkOrder } from '../types';
import Layout from '../components/Layout';
import { StatusBadge, formatDate } from '../components/Badges';

export default function MyRequestsPage() {
  const [orders, setOrders] = useState<WorkOrder[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api.getWorkOrders({ size: 50 })
      .then((res) => setOrders(res.content))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  return (
    <Layout>
      <div className="page-header">
        <h1>My Requests</h1>
        <Link to="/submit-request" className="btn-primary" style={{ display: 'inline-block', padding: '0.5rem 1rem', borderRadius: '8px', background: 'var(--primary)', color: 'white' }}>
          + New Request
        </Link>
      </div>
      {error && <div className="error-banner">{error}</div>}
      {loading ? (
        <div className="loading">Loading…</div>
      ) : orders.length === 0 ? (
        <div className="empty-state">No requests yet</div>
      ) : (
        <div className="card table-wrap">
          <table>
            <thead>
              <tr><th>Code</th><th>Title</th><th>Site</th><th>Status</th><th>Updated</th></tr>
            </thead>
            <tbody>
              {orders.map((wo) => (
                <tr key={wo.id}>
                  <td><Link to={`/work-orders/${wo.id}`}>{wo.code}</Link></td>
                  <td>{wo.title}</td>
                  <td>{wo.siteName}</td>
                  <td><StatusBadge status={wo.status} /></td>
                  <td>{formatDate(wo.updatedAt || wo.createdAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Layout>
  );
}
