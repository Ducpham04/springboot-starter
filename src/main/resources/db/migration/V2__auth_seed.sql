-- Default roles & permissions. The bootstrap admin account is created at startup
-- by AdminSeeder from APP_ADMIN_EMAIL / APP_ADMIN_PASSWORD (no password in SQL).

INSERT INTO roles (code, name, created_at, updated_at, created_by, updated_by)
VALUES ('ADMIN', 'Administrator', NOW(6), NOW(6), 'system', 'system'),
       ('USER', 'User', NOW(6), NOW(6), 'system', 'system');

INSERT INTO permissions (code, description, created_at, updated_at, created_by, updated_by)
VALUES ('USER_READ', 'View users', NOW(6), NOW(6), 'system', 'system'),
       ('USER_MANAGE', 'Create/update/disable users', NOW(6), NOW(6), 'system', 'system'),
       ('FILE_DELETE', 'Delete uploaded files', NOW(6), NOW(6), 'system', 'system');

-- ADMIN gets every permission
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p WHERE r.code = 'ADMIN';
