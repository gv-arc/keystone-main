import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { Customer, Site } from '../types';
import Layout from '../components/Layout';

export default function CustomersPage() {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);
  const [sites, setSites] = useState<Site[]>([]);
  const [search, setSearch] = useState('');
  const [showCustomerForm, setShowCustomerForm] = useState(false);
  const [showSiteForm, setShowSiteForm] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const [cForm, setCForm] = useState({ name: '', contactEmail: '', contactPhone: '', address: '' });
  const [sForm, setSForm] = useState({ name: '', address: '', city: '', postcode: '' });

  const loadCustomers = () => {
    setLoading(true);
    api.getCustomers(search)
      .then((res) => setCustomers(res.content))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadCustomers(); }, []);

  useEffect(() => {
    if (selectedCustomer) {
      api.getSites(selectedCustomer.id).then((res) => setSites(res.content));
    }
  }, [selectedCustomer]);

  const createCustomer = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.createCustomer(cForm);
      setShowCustomerForm(false);
      setCForm({ name: '', contactEmail: '', contactPhone: '', address: '' });
      loadCustomers();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed');
    }
  };

  const createSite = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedCustomer) return;
    try {
      await api.createSite(selectedCustomer.id, sForm);
      setShowSiteForm(false);
      setSForm({ name: '', address: '', city: '', postcode: '' });
      api.getSites(selectedCustomer.id).then((res) => setSites(res.content));
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed');
    }
  };

  return (
    <Layout>
      <div className="page-header">
        <h1>Customers & Sites</h1>
        <button className="btn-primary" onClick={() => setShowCustomerForm(true)}>+ Customer</button>
      </div>

      {error && <div className="error-banner">{error}</div>}

      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
        <input placeholder="Search customers…" value={search} onChange={(e) => setSearch(e.target.value)} style={{ maxWidth: 280 }} />
        <button className="btn-secondary" onClick={loadCustomers}>Search</button>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
        <div className="card">
          <h3 style={{ marginBottom: '1rem' }}>Customers</h3>
          {loading ? <div className="loading">Loading…</div> : customers.length === 0 ? (
            <div className="empty-state">No customers</div>
          ) : (
            <table>
              <tbody>
                {customers.map((c) => (
                  <tr key={c.id} onClick={() => setSelectedCustomer(c)} style={{ cursor: 'pointer' }}>
                    <td><strong>{c.name}</strong><br /><span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{c.contactEmail}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3>Sites {selectedCustomer ? `— ${selectedCustomer.name}` : ''}</h3>
            {selectedCustomer && (
              <button className="btn-secondary btn-sm" onClick={() => setShowSiteForm(true)}>+ Site</button>
            )}
          </div>
          {!selectedCustomer ? (
            <div className="empty-state">Select a customer</div>
          ) : sites.length === 0 ? (
            <div className="empty-state">No sites</div>
          ) : (
            <table>
              <tbody>
                {sites.map((s) => (
                  <tr key={s.id}>
                    <td><strong>{s.name}</strong><br /><span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{s.address}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {showCustomerForm && (
        <div className="modal-overlay" onClick={() => setShowCustomerForm(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>New Customer</h2>
            <form onSubmit={createCustomer}>
              <div className="form-group"><label>Name</label><input value={cForm.name} onChange={(e) => setCForm({ ...cForm, name: e.target.value })} required /></div>
              <div className="form-group"><label>Email</label><input type="email" value={cForm.contactEmail} onChange={(e) => setCForm({ ...cForm, contactEmail: e.target.value })} required /></div>
              <div className="form-group"><label>Phone</label><input value={cForm.contactPhone} onChange={(e) => setCForm({ ...cForm, contactPhone: e.target.value })} /></div>
              <div className="form-group"><label>Address</label><input value={cForm.address} onChange={(e) => setCForm({ ...cForm, address: e.target.value })} /></div>
              <div className="modal-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowCustomerForm(false)}>Cancel</button>
                <button type="submit" className="btn-primary">Create</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showSiteForm && selectedCustomer && (
        <div className="modal-overlay" onClick={() => setShowSiteForm(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>New Site</h2>
            <form onSubmit={createSite}>
              <div className="form-group"><label>Name</label><input value={sForm.name} onChange={(e) => setSForm({ ...sForm, name: e.target.value })} required /></div>
              <div className="form-group"><label>Address</label><input value={sForm.address} onChange={(e) => setSForm({ ...sForm, address: e.target.value })} required /></div>
              <div className="form-group"><label>City</label><input value={sForm.city} onChange={(e) => setSForm({ ...sForm, city: e.target.value })} /></div>
              <div className="form-group"><label>Postcode</label><input value={sForm.postcode} onChange={(e) => setSForm({ ...sForm, postcode: e.target.value })} /></div>
              <div className="modal-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowSiteForm(false)}>Cancel</button>
                <button type="submit" className="btn-primary">Create</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </Layout>
  );
}
