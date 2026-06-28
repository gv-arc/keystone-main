-- Password for all seed users: password123 (BCrypt)
INSERT INTO customers (id, name, contact_email, contact_phone, address) VALUES
(1, 'Acme Corp', 'facilities@acme.com', '555-0100', '100 Business Park, London'),
(2, 'Global Retail Ltd', 'ops@globalretail.com', '555-0200', '50 High Street, Manchester');

INSERT INTO sites (id, customer_id, name, address, city, postcode) VALUES
(1, 1, 'Acme HQ', '100 Business Park', 'London', 'EC1A 1BB'),
(2, 1, 'Acme Warehouse', '200 Industrial Way', 'London', 'E14 5AB'),
(3, 2, 'Global Retail Flagship', '50 High Street', 'Manchester', 'M1 1AA');

INSERT INTO users (id, email, password_hash, full_name, role, customer_id, active) VALUES
(1, 'manager@meridian.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Sarah Manager', 'MANAGER', NULL, TRUE),
(2, 'dispatcher@meridian.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Dave Dispatcher', 'DISPATCHER', NULL, TRUE),
(3, 'tech1@meridian.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Tom Technician', 'TECHNICIAN', NULL, TRUE),
(4, 'tech2@meridian.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Lisa Technician', 'TECHNICIAN', NULL, TRUE),
(5, 'customer@acme.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'John Acme', 'CUSTOMER', 1, TRUE),
(6, 'customer@globalretail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jane Global', 'CUSTOMER', 2, TRUE);

INSERT INTO parts (id, sku, name, description, unit_cost, stock_quantity) VALUES
(1, 'HVAC-FILTER-001', 'HVAC Filter Standard', 'Standard HVAC air filter', 25.00, 100),
(2, 'ELEC-FUSE-020', '20A Fuse Pack', 'Pack of 10 fuses', 15.50, 50),
(3, 'PLUMB-SEAL-T', 'Pipe Sealant Tube', 'Plumbing sealant', 8.75, 75),
(4, 'HVAC-BELT-A', 'Drive Belt Type A', 'HVAC drive belt', 45.00, 30);

INSERT INTO work_orders (id, code, title, description, priority, status, customer_id, site_id, assignee_id, sla_due_at, sla_status, created_by_id) VALUES
(1, 'WO-2024-0001', 'HVAC not cooling', 'Office AC unit on 3rd floor not cooling properly', 'HIGH', 'ASSIGNED', 1, 1, 3, NOW() + INTERVAL '8 hours', 'ON_TRACK', 2),
(2, 'WO-2024-0002', 'Flickering lights', 'Lights flickering in warehouse aisle 5', 'MEDIUM', 'NEW', 1, 2, NULL, NOW() + INTERVAL '24 hours', 'ON_TRACK', 2),
(3, 'WO-2024-0003', 'Leaking pipe', 'Water leak under sink in staff kitchen', 'CRITICAL', 'IN_PROGRESS', 2, 3, 4, NOW() + INTERVAL '4 hours', 'AT_RISK', 2);

INSERT INTO work_order_status_history (work_order_id, from_status, to_status, changed_by_id, note) VALUES
(1, NULL, 'NEW', 2, 'Work order created'),
(1, 'NEW', 'ASSIGNED', 2, 'Assigned to Tom Technician'),
(2, NULL, 'NEW', 2, 'Work order created'),
(3, NULL, 'NEW', 2, 'Work order created'),
(3, 'NEW', 'ASSIGNED', 2, 'Assigned to Lisa Technician'),
(3, 'ASSIGNED', 'IN_PROGRESS', 4, 'Started work on site');

SELECT setval(pg_get_serial_sequence('customers', 'id'), (SELECT MAX(id) FROM customers));
SELECT setval(pg_get_serial_sequence('sites', 'id'), (SELECT MAX(id) FROM sites));
SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));
SELECT setval(pg_get_serial_sequence('parts', 'id'), (SELECT MAX(id) FROM parts));
SELECT setval(pg_get_serial_sequence('work_orders', 'id'), (SELECT MAX(id) FROM work_orders));
