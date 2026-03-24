CREATE TABLE promotions (
                            id VARCHAR(255) PRIMARY KEY,
                            code VARCHAR(50) UNIQUE NOT NULL,       -- VD: SUMMER2024, FREESHIP
                            name VARCHAR(255) NOT NULL,             -- Tên hiển thị
                            type VARCHAR(50) NOT NULL,              -- Loại: ORDER_DISCOUNT, PRODUCT_DISCOUNT, BUY_X_GET_Y...
                            config JSONB NOT NULL,                  -- Chứa variantIds, percent, minOrderValue...
                            start_date TIMESTAMP NOT NULL,
                            end_date TIMESTAMP NOT NULL,
                            usage_limit INT,                        -- Tổng số lần được dùng
                            used_count INT DEFAULT 0,               -- Số lần đã dùng
                            is_active BOOLEAN DEFAULT TRUE,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP
);

