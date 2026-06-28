import type {
  PageResponse,
  Customer,
  Site,
  WorkOrder,
  User,
  Part,
  ReportSummary,
  Notification,
} from '../types';

const API_BASE = import.meta.env.VITE_API_URL || '';

export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
    public fieldErrors?: Record<string, string>
  ) {
    super(message);
  }
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem('keystone_token');
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE}${path}`, { ...options, headers });

  if (!response.ok) {
    const body = await response.json().catch(() => ({ message: response.statusText }));
    throw new ApiError(body.message || 'Request failed', response.status, body.fieldErrors);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json();
}

export const api = {
  login: (email: string, password: string) =>
    request<{ token: string; role: string; userId: number; fullName: string; customerId?: number; email?: string }>(
      '/api/auth/login',
      { method: 'POST', body: JSON.stringify({ email, password }) }
    ),

  getCustomers: (search?: string, page = 0) =>
    request<PageResponse<Customer>>(`/api/customers?search=${search || ''}&page=${page}&size=20`),

  createCustomer: (data: Partial<Customer>) =>
    request<Customer>('/api/customers', { method: 'POST', body: JSON.stringify(data) }),

  getSites: (customerId: number, search?: string) =>
    request<PageResponse<Site>>(
      `/api/customers/${customerId}/sites?search=${search || ''}&page=0&size=100`
    ),

  createSite: (customerId: number, data: Partial<Site>) =>
    request<Site>(`/api/customers/${customerId}/sites`, {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  getWorkOrders: (params: Record<string, string | boolean | number> = {}) => {
    const qs = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== '') qs.set(k, String(v));
    });
    return request<PageResponse<WorkOrder>>(`/api/work-orders?${qs}`);
  },

  getWorkOrder: (id: number) => request<WorkOrder>(`/api/work-orders/${id}`),

  createWorkOrder: (data: object) =>
    request<WorkOrder>('/api/work-orders', { method: 'POST', body: JSON.stringify(data) }),

  assignWorkOrder: (id: number, technicianId: number) =>
    request<WorkOrder>(`/api/work-orders/${id}/assign`, {
      method: 'POST',
      body: JSON.stringify({ technicianId }),
    }),

  transitionStatus: (id: number, status: string, note?: string) =>
    request<WorkOrder>(`/api/work-orders/${id}/status`, {
      method: 'POST',
      body: JSON.stringify({ status, note }),
    }),

  logParts: (id: number, partId: number, quantity: number) =>
    request<WorkOrder>(`/api/work-orders/${id}/parts`, {
      method: 'POST',
      body: JSON.stringify({ partId, quantity }),
    }),

  logTime: (id: number, minutes: number, note?: string) =>
    request<WorkOrder>(`/api/work-orders/${id}/time`, {
      method: 'POST',
      body: JSON.stringify({ minutes, note }),
    }),

  getTechnicians: () => request<User[]>('/api/technicians'),

  getParts: (search?: string) =>
    request<PageResponse<Part>>(`/api/parts?search=${search || ''}&page=0&size=100`),

  getReportSummary: () => request<ReportSummary>('/api/reports/summary'),

  getNotifications: () =>
    request<PageResponse<Notification>>('/api/notifications?page=0&size=20'),

  createCustomerRequest: (data: object) =>
    request<WorkOrder>('/api/work-orders/customer-request', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  submitCustomerRequest: (data: object) =>
    request<WorkOrder>('/api/customer-requests', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  getUsers: () => request<PageResponse<User>>('/api/users?page=0&size=50'),

  createPart: (data: object) =>
    request<Part>('/api/parts', { method: 'POST', body: JSON.stringify(data) }),
};
