import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import { Customer, Site, Priority } from '../types';
import Layout from '../components/Layout';
import { PRIORITIES } from '../components/Badges';

export default function WorkOrderCreatePage() {
  const navigate = useNavigate();
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [sites, setSites] = useState<Site[]>([]);
  const [customerId, setCustomerId] = useState<number | ''>('');
  const [siteId, setSiteId] = useState<number | ''>('');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState<Priority>('MEDIUM');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    api.getCustomers().then((res) => setCustomers(res.content));
  }, []);

  useEffect(() => {
    if (customerId) {
      api.getSites(Number(customerId)).then((res) => setSites(res.content));
    } else {
      setSites([]);
    }
  }, [customerId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const wo = await api.createWorkOrder({
        title,
        description,
        priority,
        customerId: Number(customerId),
        siteId: Number(siteId),
      });
      navigate(`/work-orders/${wo.id}`);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to create');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Layout>
      <div className="page-header"><h1>New Work Order</h1></div>
      {error && <div className="error-banner">{error}</div>}
      <div className="card" style={{ maxWidth: 560 }}>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Title</label>
            <input value={title} onChange={(e) => setTitle(e.target.value)} required />
          </div>
          <div className="form-group">
            <label>Description</label>
            <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={3} />
          </div>
          <div className="form-group">
            <label>Priority</label>
            <select value={priority} onChange={(e) => setPriority(e.target.value as Priority)}>
              {PRIORITIES.map((p) => <option key={p} value={p}>{p}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label>Customer</label>
            <select value={customerId} onChange={(e) => setCustomerId(Number(e.target.value) || '')} required>
              <option value="">Select customer</option>
              {customers.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label>Site</label>
            <select value={siteId} onChange={(e) => setSiteId(Number(e.target.value) || '')} required disabled={!customerId}>
              <option value="">Select site</option>
              {sites.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
            </select>
          </div>
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Creating…' : 'Create Work Order'}
          </button>
        </form>
      </div>
    </Layout>
  );
}
