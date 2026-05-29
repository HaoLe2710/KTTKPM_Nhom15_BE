ALTER TABLE carts
    DROP CONSTRAINT IF EXISTS fk_carts_user;

ALTER TABLE orders
    DROP CONSTRAINT IF EXISTS fk_orders_user;

CREATE INDEX IF NOT EXISTS idx_carts_user_status
    ON carts (user_id, status);

CREATE INDEX IF NOT EXISTS idx_orders_user_created_at
    ON orders (user_id, created_at DESC);
