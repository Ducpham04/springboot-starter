-- TiemPro roles: ADMIN (shop owner), MANAGER, STAFF (cashier). USER from the template keeps PRODUCT_READ only.

INSERT INTO roles (code, name, created_at, updated_at, created_by, updated_by)
VALUES ('MANAGER', 'Quản lý', NOW(6), NOW(6), 'system', 'system'),
       ('STAFF', 'Nhân viên bán hàng', NOW(6), NOW(6), 'system', 'system');

UPDATE roles SET name = 'Chủ cửa hàng' WHERE code = 'ADMIN';
UPDATE roles SET name = 'Người dùng' WHERE code = 'USER';

INSERT INTO permissions (code, description, created_at, updated_at, created_by, updated_by)
VALUES ('DASHBOARD_VIEW', 'Xem tổng quan, báo cáo', NOW(6), NOW(6), 'system', 'system'),
       ('PRODUCT_COST_VIEW', 'Xem giá vốn', NOW(6), NOW(6), 'system', 'system'),
       ('CATEGORY_READ', 'Xem nhóm hàng', NOW(6), NOW(6), 'system', 'system'),
       ('CATEGORY_MANAGE', 'Thêm/sửa/xoá nhóm hàng', NOW(6), NOW(6), 'system', 'system'),
       ('CUSTOMER_READ', 'Xem khách hàng', NOW(6), NOW(6), 'system', 'system'),
       ('CUSTOMER_WRITE', 'Thêm/sửa khách hàng', NOW(6), NOW(6), 'system', 'system'),
       ('CUSTOMER_DELETE', 'Xoá khách hàng', NOW(6), NOW(6), 'system', 'system'),
       ('ORDER_READ', 'Xem đơn hàng', NOW(6), NOW(6), 'system', 'system'),
       ('ORDER_READ_ALL', 'Xem đơn của mọi nhân viên', NOW(6), NOW(6), 'system', 'system'),
       ('ORDER_CREATE', 'Tạo đơn hàng', NOW(6), NOW(6), 'system', 'system'),
       ('ORDER_CANCEL', 'Huỷ đơn hàng', NOW(6), NOW(6), 'system', 'system'),
       ('STOCK_READ', 'Xem kho', NOW(6), NOW(6), 'system', 'system'),
       ('STOCK_MANAGE', 'Nhập hàng, kiểm kho', NOW(6), NOW(6), 'system', 'system');

-- ADMIN: every permission it does not have yet
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.code = 'ADMIN'
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

-- MANAGER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.code IN (
                                          'DASHBOARD_VIEW',
                                          'PRODUCT_READ', 'PRODUCT_CREATE', 'PRODUCT_UPDATE', 'PRODUCT_DELETE',
                                          'PRODUCT_COST_VIEW',
                                          'CATEGORY_READ', 'CATEGORY_MANAGE',
                                          'CUSTOMER_READ', 'CUSTOMER_WRITE', 'CUSTOMER_DELETE',
                                          'ORDER_READ', 'ORDER_READ_ALL', 'ORDER_CREATE', 'ORDER_CANCEL',
                                          'STOCK_READ', 'STOCK_MANAGE')
WHERE r.code = 'MANAGER';

-- STAFF
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.code IN (
                                          'PRODUCT_READ', 'CATEGORY_READ',
                                          'CUSTOMER_READ', 'CUSTOMER_WRITE',
                                          'ORDER_READ', 'ORDER_CREATE')
WHERE r.code = 'STAFF';
