CREATE TABLE code_sequences
(
    seq_key VARCHAR(30) PRIMARY KEY,
    value   BIGINT NOT NULL
) ENGINE = InnoDB;

CREATE TABLE orders
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    version        BIGINT         NOT NULL DEFAULT 0,
    created_at     DATETIME(6)    NOT NULL,
    updated_at     DATETIME(6)    NOT NULL,
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    code           VARCHAR(30)    NOT NULL,
    customer_id    BIGINT         NULL,
    seller_id      BIGINT         NOT NULL,
    status         VARCHAR(20)    NOT NULL,
    payment_method VARCHAR(20)    NOT NULL,
    subtotal       DECIMAL(15, 2) NOT NULL,
    discount       DECIMAL(15, 2) NOT NULL DEFAULT 0,
    total          DECIMAL(15, 2) NOT NULL,
    total_cost     DECIMAL(15, 2) NOT NULL DEFAULT 0,
    cash_received  DECIMAL(15, 2) NULL,
    note           VARCHAR(500),
    cancelled_at   DATETIME(6)    NULL,
    cancelled_by   VARCHAR(100)   NULL,
    cancel_reason  VARCHAR(500)   NULL,
    CONSTRAINT uk_orders_code UNIQUE (code),
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_orders_seller FOREIGN KEY (seller_id) REFERENCES users (id),
    INDEX idx_orders_created_at (created_at),
    INDEX idx_orders_status_created (status, created_at),
    INDEX idx_orders_seller (seller_id, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE order_items
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    version      BIGINT         NOT NULL DEFAULT 0,
    created_at   DATETIME(6)    NOT NULL,
    updated_at   DATETIME(6)    NOT NULL,
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100),
    order_id     BIGINT         NOT NULL,
    product_id   BIGINT         NOT NULL,
    product_name VARCHAR(200)   NOT NULL,
    sku          VARCHAR(50)    NOT NULL,
    unit         VARCHAR(30)    NOT NULL,
    unit_price   DECIMAL(15, 2) NOT NULL,
    unit_cost    DECIMAL(15, 2) NOT NULL,
    quantity     INT            NOT NULL,
    line_total   DECIMAL(15, 2) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id),
    INDEX idx_order_items_product (product_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE stock_receipts
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    version       BIGINT         NOT NULL DEFAULT 0,
    created_at    DATETIME(6)    NOT NULL,
    updated_at    DATETIME(6)    NOT NULL,
    created_by    VARCHAR(100),
    updated_by    VARCHAR(100),
    code          VARCHAR(30)    NOT NULL,
    supplier_name VARCHAR(200),
    note          VARCHAR(500),
    total_amount  DECIMAL(15, 2) NOT NULL,
    CONSTRAINT uk_stock_receipts_code UNIQUE (code),
    INDEX idx_stock_receipts_created_at (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE stock_receipt_items
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    version    BIGINT         NOT NULL DEFAULT 0,
    created_at DATETIME(6)    NOT NULL,
    updated_at DATETIME(6)    NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    receipt_id BIGINT         NOT NULL,
    product_id BIGINT         NOT NULL,
    quantity   INT            NOT NULL,
    unit_cost  DECIMAL(15, 2) NOT NULL,
    line_total DECIMAL(15, 2) NOT NULL,
    CONSTRAINT fk_receipt_items_receipt FOREIGN KEY (receipt_id) REFERENCES stock_receipts (id),
    CONSTRAINT fk_receipt_items_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE stock_movements
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    version         BIGINT      NOT NULL DEFAULT 0,
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    product_id      BIGINT      NOT NULL,
    type            VARCHAR(20) NOT NULL,
    quantity_change INT         NOT NULL,
    balance_after   INT         NOT NULL,
    reference_code  VARCHAR(30),
    note            VARCHAR(500),
    CONSTRAINT fk_stock_movements_product FOREIGN KEY (product_id) REFERENCES products (id),
    INDEX idx_stock_movements_product (product_id, created_at),
    INDEX idx_stock_movements_created_at (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
