import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api, ApiError } from '../api/client';
import { WorkOrder, User, Part } from '../types';
import { useAuth, hasRole } from '../context/AuthContext';
import Layout from '../components/Layout';
import { StatusBadge, PriorityBadge, SlaBadge, formatDate, formatMinutes } from '../components/Badges';

export default function WorkOrderDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [wo, setWo] = useState<WorkOrder | null>(null);
  const [technicians, setTechnicians] = useState<User[]>([]);
  const [parts, setParts] = useState<Part[]>([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [selectedTech, setSelectedTech] = useState<number | ''>('');
  const [partId, setPartId] = useState<number | ''>('');
  const [partQty, setPartQty] = useState(1);
  const [timeMinutes, setTimeMinutes] = useState(30);
  const [timeNote, setTimeNote] = useState('');
  const [note, setNote] = useState('');

  const load = () => {
    if (!id) return;
    setLoading(true);
    api.getWorkOrder(Number(id))
      .then(setWo)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
    if (hasRole(user, 'DISPATCHER', 'MANAGER')) {
      api.getTechnicians().then(setTechnicians);
    }
    if (hasRole(user, 'TECHNICIAN')) {
      api.getParts().then((res) => setParts(res.content));
    }
  }, [id]);

  const handleAssign = async () => {
    if (!selectedTech || !id) return;
    try {
      const updated = await api.assignWorkOrder(Number(id), Number(selectedTech));
      setWo(updated);
      setError('');
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Assign failed');
    }
  };

  const handleTransition = async (status: string) => {
    if (!id) return;
    try {
      const updated = await api.transitionStatus(Number(id), status, note || undefined);
      setWo(updated);
      setNote('');
      setError('');
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Transition failed');
    }
  };

  const handleLogParts = async () => {
    if (!partId || !id) return;
    try {
      const updated = await api.logParts(Number(id), Number(partId), partQty);
      setWo(updated);
      setError('');
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Failed to log parts');
    }
  };

  const handleLogTime = async () => {
    if (!id) return;
    try {
      const updated = await api.logTime(Number(id), timeMinutes, timeNote || undefined);
      setWo(updated);
      setError('');
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Failed to log time');
    }
  };

  if (loading) return <Layout><div className="loading">Loading…</div></Layout>;
  if (!wo) return <Layout><div className="error-banner">{error || 'Not found'}</div></Layout>;

  const isAssignee = user?.userId === wo.assigneeId;
  const isOpen = !['CLOSED', 'CANCELLED'].includes(wo.status);

  return (
    <Layout>
      <div className="page-header">
        <div>
          <button className="btn-secondary btn-sm" onClick={() => navigate(-1)} style={{ marginBottom: '0.5rem' }}>
            ← Back
          </button>
          <h1>{wo.code} — {wo.title}</h1>
        </div>
        <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
          <StatusBadge status={wo.status} />
          <PriorityBadge priority={wo.priority} />
          <SlaBadge status={wo.slaStatus} />
        </div>
      </div>

      {error && <div className="error-banner">{error}</div>}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1rem' }}>
        <div className="card">
          <h3 style={{ marginBottom: '1rem' }}>Details</h3>
          <p style={{ marginBottom: '0.5rem' }}>{wo.description || 'No description'}</p>
          <table>
            <tbody>
              <tr><td style={{ color: 'var(--text-muted)' }}>Customer</td><td>{wo.customerName}</td></tr>
              <tr><td style={{ color: 'var(--text-muted)' }}>Site</td><td>{wo.siteName}</td></tr>
              <tr><td style={{ color: 'var(--text-muted)' }}>Assignee</td><td>{wo.assigneeName || 'Unassigned'}</td></tr>
              <tr><td style={{ color: 'var(--text-muted)' }}>SLA Due</td><td>{formatDate(wo.slaDueAt)}</td></tr>
              <tr><td style={{ color: 'var(--text-muted)' }}>Parts Cost</td><td>${wo.totalPartsCost?.toFixed(2) ?? '0.00'}</td></tr>
              <tr><td style={{ color: 'var(--text-muted)' }}>Time Logged</td><td>{formatMinutes(wo.totalMinutesLogged ?? 0)}</td></tr>
            </tbody>
          </table>
        </div>

        {hasRole(user, 'DISPATCHER', 'MANAGER') && isOpen && (
          <div className="card">
            <h3 style={{ marginBottom: '1rem' }}>Assign Technician</h3>
            <div className="form-group">
              <select value={selectedTech} onChange={(e) => setSelectedTech(Number(e.target.value) || '')}>
                <option value="">Select technician</option>
                {technicians.map((t) => <option key={t.id} value={t.id}>{t.fullName}</option>)}
              </select>
            </div>
            <button className="btn-primary" onClick={handleAssign} disabled={!selectedTech}>Assign</button>
          </div>
        )}

        {hasRole(user, 'TECHNICIAN') && isAssignee && isOpen && (
          <div className="card">
            <h3 style={{ marginBottom: '1rem' }}>Field Actions</h3>
            <div className="form-group">
              <label>Note (optional)</label>
              <input value={note} onChange={(e) => setNote(e.target.value)} placeholder="Transition note" />
            </div>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginBottom: '1rem' }}>
              {wo.status === 'ASSIGNED' && (
                <button className="btn-primary" onClick={() => handleTransition('IN_PROGRESS')}>Start Work</button>
              )}
              {wo.status === 'IN_PROGRESS' && (
                <>
                  <button className="btn-secondary" onClick={() => handleTransition('ON_HOLD')}>Hold</button>
                  <button className="btn-primary" onClick={() => handleTransition('COMPLETED')}>Complete</button>
                </>
              )}
              {wo.status === 'ON_HOLD' && (
                <button className="btn-primary" onClick={() => handleTransition('IN_PROGRESS')}>Resume</button>
              )}
            </div>

            <h4 style={{ marginBottom: '0.5rem', fontSize: '0.875rem' }}>Log Parts</h4>
            <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem', flexWrap: 'wrap' }}>
              <select value={partId} onChange={(e) => setPartId(Number(e.target.value) || '')} style={{ flex: 1 }}>
                <option value="">Select part</option>
                {parts.map((p) => <option key={p.id} value={p.id}>{p.name} (stock: {p.stockQuantity})</option>)}
              </select>
              <input type="number" min={1} value={partQty} onChange={(e) => setPartQty(Number(e.target.value))} style={{ width: 70 }} />
              <button className="btn-secondary" onClick={handleLogParts} disabled={!partId}>Log</button>
            </div>

            <h4 style={{ marginBottom: '0.5rem', fontSize: '0.875rem' }}>Log Time</h4>
            <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
              <input type="number" min={1} value={timeMinutes} onChange={(e) => setTimeMinutes(Number(e.target.value))} style={{ width: 80 }} />
              <input value={timeNote} onChange={(e) => setTimeNote(e.target.value)} placeholder="Note" style={{ flex: 1 }} />
              <button className="btn-secondary" onClick={handleLogTime}>Log Time</button>
            </div>
          </div>
        )}

        {hasRole(user, 'MANAGER') && wo.status === 'COMPLETED' && (
          <div className="card">
            <h3 style={{ marginBottom: '1rem' }}>Close Out</h3>
            <button className="btn-primary" onClick={() => handleTransition('CLOSED')}>Close Work Order</button>
          </div>
        )}

        {hasRole(user, 'DISPATCHER', 'MANAGER') && isOpen && (
          <div className="card">
            <h3 style={{ marginBottom: '1rem' }}>Cancel</h3>
            <button className="btn-danger" onClick={() => handleTransition('CANCELLED')}>Cancel Work Order</button>
          </div>
        )}
      </div>

      {wo.statusHistory && wo.statusHistory.length > 0 && (
        <div className="card" style={{ marginTop: '1rem' }}>
          <h3 style={{ marginBottom: '0.5rem' }}>Status History</h3>
          <ul className="history-list">
            {wo.statusHistory.map((h) => (
              <li key={h.id}>
                <strong>{h.fromStatus ? `${h.fromStatus} → ${h.toStatus}` : h.toStatus}</strong>
                {h.changedByName && ` by ${h.changedByName}`}
                {h.note && <span> — {h.note}</span>}
                <div className="time">{formatDate(h.changedAt)}</div>
              </li>
            ))}
          </ul>
        </div>
      )}
    </Layout>
  );
}
