import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { ReportSummary } from '../types';
import Layout from '../components/Layout';

export default function DashboardPage() {
  const [report, setReport] = useState<ReportSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api.getReportSummary()
      .then(setReport)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Layout><div className="loading">Loading dashboard…</div></Layout>;
  if (error) return <Layout><div className="error-banner">{error}</div></Layout>;
  if (!report) return <Layout><div className="empty-state">No data available</div></Layout>;

  const openTotal = Object.entries(report.statusCounts)
    .filter(([s]) => !['CLOSED', 'CANCELLED'].includes(s))
    .reduce((sum, [, c]) => sum + c, 0);

  return (
    <Layout>
      <div className="page-header">
        <h1>Operations Dashboard</h1>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="value">{openTotal}</div>
          <div className="label">Open Work Orders</div>
        </div>
        <div className="stat-card">
          <div className="value" style={{ color: 'var(--danger)' }}>{report.overdueCount}</div>
          <div className="label">Overdue</div>
        </div>
        <div className="stat-card">
          <div className="value" style={{ color: 'var(--success)' }}>{report.slaOnTrack}</div>
          <div className="label">SLA On Track</div>
        </div>
        <div className="stat-card">
          <div className="value" style={{ color: 'var(--warning)' }}>{report.slaAtRisk}</div>
          <div className="label">SLA At Risk</div>
        </div>
        <div className="stat-card">
          <div className="value" style={{ color: 'var(--danger)' }}>{report.slaBreached}</div>
          <div className="label">SLA Breached</div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1rem' }}>
        <div className="card">
          <h3 style={{ marginBottom: '1rem', fontSize: '1rem' }}>Status Breakdown</h3>
          <table>
            <tbody>
              {Object.entries(report.statusCounts).map(([status, count]) => (
                <tr key={status}>
                  <td>{status.replace('_', ' ')}</td>
                  <td style={{ textAlign: 'right', fontWeight: 600 }}>{count}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="card">
          <h3 style={{ marginBottom: '1rem', fontSize: '1rem' }}>Open by Technician</h3>
          {report.byTechnician.length === 0 ? (
            <p className="empty-state" style={{ padding: '1rem' }}>No assigned work</p>
          ) : (
            <table>
              <tbody>
                {report.byTechnician.map((t) => (
                  <tr key={t.technicianId}>
                    <td>{t.technicianName}</td>
                    <td style={{ textAlign: 'right', fontWeight: 600 }}>{t.openCount}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        <div className="card">
          <h3 style={{ marginBottom: '1rem', fontSize: '1rem' }}>Open by Site</h3>
          {report.bySite.length === 0 ? (
            <p className="empty-state" style={{ padding: '1rem' }}>No open work</p>
          ) : (
            <table>
              <tbody>
                {report.bySite.map((s) => (
                  <tr key={s.siteId}>
                    <td>{s.siteName}</td>
                    <td style={{ textAlign: 'right', fontWeight: 600 }}>{s.openCount}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </Layout>
  );
}
