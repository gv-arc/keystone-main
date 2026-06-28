import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { WorkOrder } from '../types';
import Layout from '../components/Layout';
import { StatusBadge, PriorityBadge, SlaBadge, formatDate } from '../components/Badges';

export default function WorkOrdersPage() {
  const [orders, setOrders] = useState<WorkOrder[]>([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = () => {
    setLoading(true);
    api.getWorkOrders({ search, size: 50 })
      .then((res) => setOrders(res.content))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  return (
    <Layout>
      <div className="page-header">
        <h1>Work Orders</h1>
        <Link to="/work-orders/new" className="btn-primary" style={{ display: 'inline-block', padding: '0.5rem 1rem', borderRadius: '8px', background: 'var(--primary)', color: 'white' }}>
          + New
        </Link>
      </div>

      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
        <input
          placeholder="Search by title or code…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          style={{ maxWidth: 320 }}
        />
        <button className="btn-secondary" onClick={load}>Search</button>
      </div>

      {error && <div className="error-banner">{error}</div>}
      {loading ? (
        <div className="loading">Loading…</div>
      ) : orders.length === 0 ? (
        <div className="empty-state">No work orders found</div>
      ) : (
        <div className="card table-wrap">
          <table>
            <thead>
              <tr>
                <th>Code</th>
                <th>Title</th>
                <th>Customer</th>
                <th>Site</th>
                <th>Priority</th>
                <th>Status</th>
                <th>SLA</th>
                <th>Assignee</th>
                <th>Due</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((wo) => (
                <tr key={wo.id}>
                  <td><Link to={`/work-orders/${wo.id}`}>{wo.code}</Link></td>
                  <td>{wo.title}</td>
                  <td>{wo.customerName}</td>
                  <td>{wo.siteName}</td>
                  <td><PriorityBadge priority={wo.priority} /></td>
                  <td><StatusBadge status={wo.status} /></td>
                  <td><SlaBadge status={wo.slaStatus} /></td>
                  <td>{wo.assigneeName || '—'}</td>
                  <td>{formatDate(wo.slaDueAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Layout>
  );
}
