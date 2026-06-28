import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { WorkOrder } from '../types';
import Layout from '../components/Layout';
import { StatusBadge, PriorityBadge, SlaBadge, formatDate } from '../components/Badges';

export default function MyJobsPage() {
  const [orders, setOrders] = useState<WorkOrder[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api.getWorkOrders({ openOnly: true, size: 50 })
      .then((res) => setOrders(res.content))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Layout><div className="loading">Loading your jobs…</div></Layout>;

  return (
    <Layout>
      <div className="page-header"><h1>My Jobs</h1></div>
      {error && <div className="error-banner">{error}</div>}
      {orders.length === 0 ? (
        <div className="empty-state">No assigned jobs</div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          {orders.map((wo) => (
            <Link key={wo.id} to={`/work-orders/${wo.id}`} style={{ color: 'inherit' }}>
              <div className="card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.75rem' }}>
                <div>
                  <strong>{wo.code}</strong> — {wo.title}
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                    {wo.siteName} · Due {formatDate(wo.slaDueAt)}
                  </div>
                </div>
                <div style={{ display: 'flex', gap: '0.35rem', flexWrap: 'wrap' }}>
                  <StatusBadge status={wo.status} />
                  <PriorityBadge priority={wo.priority} />
                  <SlaBadge status={wo.slaStatus} />
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </Layout>
  );
}
