import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { WorkOrder } from '../types';
import Layout from '../components/Layout';
import { KANBAN_COLUMNS, PriorityBadge, SlaBadge } from '../components/Badges';

export default function BoardPage() {
  const [orders, setOrders] = useState<WorkOrder[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api.getWorkOrders({ openOnly: true, size: 100 })
      .then((res) => setOrders(res.content))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  const byStatus = (status: string) => orders.filter((o) => o.status === status);

  if (loading) return <Layout><div className="loading">Loading board…</div></Layout>;

  return (
    <Layout>
      <div className="page-header">
        <h1>Dispatch Board</h1>
        <Link to="/work-orders/new" className="btn-primary" style={{ display: 'inline-block', padding: '0.5rem 1rem', borderRadius: '8px', background: 'var(--primary)', color: 'white' }}>
          + New Work Order
        </Link>
      </div>
      {error && <div className="error-banner">{error}</div>}

      <div className="kanban">
        {KANBAN_COLUMNS.map((status) => (
          <div key={status} className="kanban-column">
            <div className="kanban-column-header">
              <span>{status.replace('_', ' ')}</span>
              <span>{byStatus(status).length}</span>
            </div>
            {byStatus(status).length === 0 ? (
              <p style={{ padding: '1rem', fontSize: '0.8rem', color: 'var(--text-muted)' }}>Empty</p>
            ) : (
              byStatus(status).map((wo) => (
                <Link key={wo.id} to={`/work-orders/${wo.id}`} style={{ color: 'inherit' }}>
                  <div className="kanban-card">
                    <h4>{wo.code} — {wo.title}</h4>
                    <div className="kanban-card-meta">
                      {wo.siteName} · {wo.assigneeName || 'Unassigned'}
                    </div>
                    <div style={{ marginTop: '0.5rem', display: 'flex', gap: '0.35rem', flexWrap: 'wrap' }}>
                      <PriorityBadge priority={wo.priority} />
                      <SlaBadge status={wo.slaStatus} />
                    </div>
                  </div>
                </Link>
              ))
            )}
          </div>
        ))}
      </div>
    </Layout>
  );
}
