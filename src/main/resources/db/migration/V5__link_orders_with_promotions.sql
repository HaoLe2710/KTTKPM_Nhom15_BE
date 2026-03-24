ALTER TABLE orders
    ADD COLUMN promotion_id VARCHAR(255),
    ADD COLUMN promotion_code VARCHAR(50);

CREATE INDEX idx_orders_promotion_id ON orders(promotion_id);
