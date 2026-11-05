CREATE TABLE categories
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    version     BIGINT       NOT NULL DEFAULT 0,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT uk_categories_name UNIQUE (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Product: link to category, unit, cost price, low-stock threshold.
-- The template's free-text "category" column is replaced by category_id.
ALTER TABLE products
    DROP INDEX idx_products_category,
    DROP COLUMN category,
    ADD COLUMN category_id BIGINT NULL AFTER description,
    ADD COLUMN unit        VARCHAR(30)    NOT NULL DEFAULT 'cái' AFTER category_id,
    ADD COLUMN cost_price  DECIMAL(15, 2) NOT NULL DEFAULT 0 AFTER price,
    ADD COLUMN min_stock   INT            NOT NULL DEFAULT 0 AFTER quantity,
    ADD CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id),
    ADD INDEX idx_products_category (category_id),
    ADD INDEX idx_products_name (name);

CREATE TABLE customers
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    version    BIGINT       NOT NULL DEFAULT 0,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    name       VARCHAR(150) NOT NULL,
    phone      VARCHAR(20)  NOT NULL,
    email      VARCHAR(150),
    address    VARCHAR(255),
    note       VARCHAR(500),
    CONSTRAINT uk_customers_phone UNIQUE (phone),
    INDEX idx_customers_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
