-- V3__add_order_cancel_reason.sql
-- Bổ sung cột cancel_reason vào bảng orders để hỗ trợ State Pattern (CancelledState)

ALTER TABLE orders ADD COLUMN cancel_reason TEXT;
