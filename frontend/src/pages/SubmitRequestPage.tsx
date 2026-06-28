import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import { Site, Priority } from '../types';
import { useAuth } from '../context/AuthContext';
import Layout from '../components/Layout';
import { PRIORITIES } from '../components/Badges';

export default function SubmitRequestPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [sites, setSites] = useState<Site[]>([]);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState<Priority>('MEDIUM');
  const [siteId, setSiteId] = useState<number | ''>('');
  const [contactEmail, setContactEmail] = useState(user?.role === 'CUSTOMER' ? '' : '');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (user?.customerId) {
      api.getSites(user.customerId).then((res) => setSites(res.content));
    }
  }, [user]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      const payload = { title, description, priority, siteId: Number(siteId) };
      const wo = user?.role === 'CUSTOMER'
        ? await api.createCustomerRequest(payload)
        : await api.submitCustomerRequest({
            ...payload,
            contactEmail,
          });
      if (user?.role === 'CUSTOMER') {
        navigate(`/work-orders/${wo.id}`);
      } else {
        setSuccess(`Request submitted: ${wo.code}`);
        setTitle('');
        setDescription('');
      }
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to submit');
    } finally {
      setLoading(false);
    }
  };

  const content = (
    <>
      <div className="page-header"><h1>Submit Service Request</h1></div>
      {error && <div className="error-banner">{error}</div>}
      {success && <div className="card" style={{ marginBottom: '1rem', borderColor: 'var(--success)' }}>{success}</div>}
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
          {user?.role === 'CUSTOMER' ? (
            <div className="form-group">
              <label>Site</label>
              <select value={siteId} onChange={(e) => setSiteId(Number(e.target.value) || '')} required>
                <option value="">Select site</option>
                {sites.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
              </select>
            </div>
          ) : (
            <>
              <div className="form-group">
                <label>Site ID</label>
                <input type="number" value={siteId} onChange={(e) => setSiteId(Number(e.target.value) || '')} required />
              </div>
              <div className="form-group">
                <label>Contact Email (must match customer record)</label>
                <input type="email" value={contactEmail} onChange={(e) => setContactEmail(e.target.value)} required />
              </div>
            </>
          )}
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Submitting…' : 'Submit Request'}
          </button>
        </form>
      </div>
    </>
  );

  if (user?.role === 'CUSTOMER') {
    return <Layout>{content}</Layout>;
  }

  return <div className="login-page"><div style={{ width: '100%', maxWidth: 600, padding: '1rem' }}>{content}</div></div>;
}
