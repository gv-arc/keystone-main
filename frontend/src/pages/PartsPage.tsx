import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { Part } from '../types';
import Layout from '../components/Layout';

export default function PartsPage() {
  const [parts, setParts] = useState<Part[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ sku: '', name: '', description: '', unitCost: 0, stockQuantity: 0 });

  const load = () => {
    setLoading(true);
    api.getParts()
      .then((res) => setParts(res.content))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.createPart(form);
      setShowForm(false);
      setForm({ sku: '', name: '', description: '', unitCost: 0, stockQuantity: 0 });
      load();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed');
    }
  };

  return (
    <Layout>
      <div className="page-header">
        <h1>Parts Inventory</h1>
        <button className="btn-primary" onClick={() => setShowForm(true)}>+ Part</button>
      </div>
      {error && <div className="error-banner">{error}</div>}
      {loading ? (
        <div className="loading">Loading…</div>
      ) : (
        <div className="card table-wrap">
          <table>
            <thead>
              <tr><th>SKU</th><th>Name</th><th>Unit Cost</th><th>Stock</th></tr>
            </thead>
            <tbody>
              {parts.map((p) => (
                <tr key={p.id}>
                  <td>{p.sku}</td>
                  <td>{p.name}</td>
                  <td>${p.unitCost.toFixed(2)}</td>
                  <td>{p.stockQuantity}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showForm && (
        <div className="modal-overlay" onClick={() => setShowForm(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>New Part</h2>
            <form onSubmit={handleCreate}>
              <div className="form-group"><label>SKU</label><input value={form.sku} onChange={(e) => setForm({ ...form, sku: e.target.value })} required /></div>
              <div className="form-group"><label>Name</label><input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required /></div>
              <div className="form-group"><label>Description</label><input value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></div>
              <div className="form-group"><label>Unit Cost</label><input type="number" step="0.01" value={form.unitCost} onChange={(e) => setForm({ ...form, unitCost: Number(e.target.value) })} required /></div>
              <div className="form-group"><label>Stock</label><input type="number" value={form.stockQuantity} onChange={(e) => setForm({ ...form, stockQuantity: Number(e.target.value) })} required /></div>
              <div className="modal-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowForm(false)}>Cancel</button>
                <button type="submit" className="btn-primary">Create</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </Layout>
  );
}
