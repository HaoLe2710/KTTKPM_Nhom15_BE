ALTER TABLE chat_messages
    ALTER COLUMN content DROP NOT NULL;

ALTER TABLE chat_messages
    ADD COLUMN message_type VARCHAR(50) NOT NULL DEFAULT 'TEXT',
    ADD COLUMN image_url TEXT,
    ADD COLUMN link_url TEXT,
    ADD COLUMN product_id VARCHAR(255),
    ADD COLUMN variant_id VARCHAR(255),
    ADD COLUMN product_name VARCHAR(255),
    ADD COLUMN product_image_url TEXT,
    ADD COLUMN product_price NUMERIC(19, 2);
