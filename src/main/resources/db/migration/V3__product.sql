-- Example module. When copying the Product module, create a similar migration:
-- table + permissions + grants.

CREATE TABLE products
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    version     BIGINT         NOT NULL DEFAULT 0,
    created_at  DATETIME(6)    NOT NULL,
    updated_at  DATETIME(6)    NOT NULL,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    sku         VARCHAR(50)    NOT NULL,
    name        VARCHAR(200)   NOT NULL,
    description TEXT,
    price       DECIMAL(15, 2) NOT NULL,
    quantity    INT            NOT NULL DEFAULT 0,
    category    VARCHAR(100),
    image_url   VARCHAR(500),
    status      VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uk_products_sku UNIQUE (sku),
    INDEX idx_products_status (status),
    INDEX idx_products_category (category)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

INSERT INTO permissions (code, description, created_at, updated_at, created_by, updated_by)
VALUES ('PRODUCT_READ', 'View products', NOW(6), NOW(6), 'system', 'system'),
       ('PRODUCT_CREATE', 'Create products', NOW(6), NOW(6), 'system', 'system'),
       ('PRODUCT_UPDATE', 'Update products', NOW(6), NOW(6), 'system', 'system'),
       ('PRODUCT_DELETE', 'Delete products', NOW(6), NOW(6), 'system', 'system');

-- ADMIN: all product permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN ('PRODUCT_READ', 'PRODUCT_CREATE', 'PRODUCT_UPDATE', 'PRODUCT_DELETE') WHERE r.code = 'ADMIN';

-- USER: read only
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code = 'PRODUCT_READ' WHERE r.code = 'USER';
