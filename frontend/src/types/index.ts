export type Role = 'DISPATCHER' | 'TECHNICIAN' | 'MANAGER' | 'CUSTOMER';

export type WorkOrderStatus =
  | 'NEW'
  | 'ASSIGNED'
  | 'IN_PROGRESS'
  | 'ON_HOLD'
  | 'COMPLETED'
  | 'CLOSED'
  | 'CANCELLED';

export type Priority = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';

export interface AuthUser {
  token: string;
  role: Role;
  userId: number;
  fullName: string;
  customerId?: number;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface Customer {
  id: number;
  name: string;
  contactEmail: string;
  contactPhone?: string;
  address?: string;
}

export interface Site {
  id: number;
  customerId: number;
  customerName?: string;
  name: string;
  address: string;
  city?: string;
  postcode?: string;
}

export interface User {
  id: number;
  email: string;
  fullName: string;
  role: Role;
  customerId?: number;
  active: boolean;
}

export interface Part {
  id: number;
  sku: string;
  name: string;
  description?: string;
  unitCost: number;
  stockQuantity: number;
}

export interface StatusHistory {
  id: number;
  fromStatus?: string;
  toStatus: string;
  changedByName?: string;
  note?: string;
  changedAt: string;
}

export interface PartUsage {
  id: number;
  partId: number;
  partName: string;
  partSku: string;
  quantity: number;
  unitCost: number;
  totalCost: number;
  loggedByName: string;
  loggedAt: string;
}

export interface TimeLog {
  id: number;
  minutes: number;
  note?: string;
  technicianName: string;
  loggedAt: string;
}

export interface WorkOrder {
  id: number;
  code: string;
  title: string;
  description?: string;
  priority: Priority;
  status: WorkOrderStatus;
  customerId?: number;
  customerName?: string;
  siteId: number;
  siteName: string;
  assigneeId?: number;
  assigneeName?: string;
  slaDueAt?: string;
  slaStatus: string;
  totalPartsCost?: number;
  totalMinutesLogged?: number;
  createdAt: string;
  updatedAt?: string;
  closedAt?: string;
  statusHistory?: StatusHistory[];
  partUsages?: PartUsage[];
  timeLogs?: TimeLog[];
}

export interface ReportSummary {
  statusCounts: Record<string, number>;
  overdueCount: number;
  slaOnTrack: number;
  slaAtRisk: number;
  slaBreached: number;
  byTechnician: { technicianId: number; technicianName: string; openCount: number }[];
  bySite: { siteId: number; siteName: string; openCount: number }[];
}

export interface Notification {
  id: number;
  type: string;
  message: string;
  workOrderId?: number;
  workOrderCode?: string;
  read: boolean;
  createdAt: string;
}
