-- ==========================================
-- V1__init_schema.sql
-- Khởi tạo DB Mỹ Phẩm (Map 100% ERD Mermaid)
-- ==========================================

-- 1. MODULE USERS
CREATE TABLE users (
                       id VARCHAR(255) PRIMARY KEY,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP,
                       email VARCHAR(255) UNIQUE,
                       phone VARCHAR(255) UNIQUE,
                       password VARCHAR(255),
                       full_name VARCHAR(255),
                       avatar_url VARCHAR(500),
                       role VARCHAR(50) NOT NULL,
                       is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE addresses (
                           id VARCHAR(255) PRIMARY KEY,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP,
                           user_id VARCHAR(255) NOT NULL,
                           full_name VARCHAR(255) NOT NULL,
                           phone VARCHAR(50) NOT NULL,
                           address VARCHAR(500) NOT NULL,
                           city VARCHAR(100) NOT NULL,
                           district VARCHAR(100) NOT NULL,
                           ward VARCHAR(100) NOT NULL,
                           is_default BOOLEAN DEFAULT FALSE,
                           CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 2. MODULE CATALOG
CREATE TABLE product_types (
                               id VARCHAR(255) PRIMARY KEY,
                               code VARCHAR(100) UNIQUE NOT NULL,
                               name VARCHAR(255) NOT NULL
);

CREATE TABLE products (
                          id VARCHAR(255) PRIMARY KEY,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP,
                          type_id VARCHAR(255) NOT NULL,
                          name VARCHAR(255) NOT NULL,
                          slug VARCHAR(255) UNIQUE NOT NULL,
                          description_md TEXT,
                          is_customizable BOOLEAN DEFAULT FALSE,
                          is_active BOOLEAN DEFAULT TRUE,
                          CONSTRAINT fk_products_type FOREIGN KEY (type_id) REFERENCES product_types(id)
);

CREATE TABLE variants (
                          id VARCHAR(255) PRIMARY KEY,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP,
                          product_id VARCHAR(255) NOT NULL,
                          sku VARCHAR(100) UNIQUE NOT NULL,
                          price DECIMAL(18,2) NOT NULL,
                          stock_quantity INT DEFAULT 0,
                          is_active BOOLEAN DEFAULT TRUE,
                          CONSTRAINT fk_variants_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE options (
                         id VARCHAR(255) PRIMARY KEY,
                         code VARCHAR(100) UNIQUE NOT NULL,
                         name VARCHAR(255) NOT NULL
);

CREATE TABLE option_values (
                               id VARCHAR(255) PRIMARY KEY,
                               option_id VARCHAR(255) NOT NULL,
                               value VARCHAR(255) NOT NULL,
                               is_active BOOLEAN DEFAULT TRUE,
                               CONSTRAINT fk_option_values_option FOREIGN KEY (option_id) REFERENCES options(id) ON DELETE CASCADE
);

CREATE TABLE variant_options (
                                 id VARCHAR(255) PRIMARY KEY,
                                 variant_id VARCHAR(255) NOT NULL,
                                 option_value_id VARCHAR(255) NOT NULL,
                                 CONSTRAINT fk_vo_variant FOREIGN KEY (variant_id) REFERENCES variants(id) ON DELETE CASCADE,
                                 CONSTRAINT fk_vo_option_value FOREIGN KEY (option_value_id) REFERENCES option_values(id) ON DELETE CASCADE
);

CREATE TABLE media (
                       id VARCHAR(255) PRIMARY KEY,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       product_id VARCHAR(255) NOT NULL,
                       variant_id VARCHAR(255),
                       url VARCHAR(500) NOT NULL,
                       public_id VARCHAR(255) NOT NULL,
                       type VARCHAR(50) NOT NULL,
                       is_primary BOOLEAN DEFAULT FALSE,
                       CONSTRAINT fk_media_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
                       CONSTRAINT fk_media_variant FOREIGN KEY (variant_id) REFERENCES variants(id) ON DELETE CASCADE
);

-- 3. MODULE CARTS
CREATE TABLE carts (
                       id VARCHAR(255) PRIMARY KEY,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP,
                       user_id VARCHAR(255),
                       status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
                       CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE cart_items (
                            id VARCHAR(255) PRIMARY KEY,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP,
                            cart_id VARCHAR(255) NOT NULL,
                            variant_id VARCHAR(255) NOT NULL,
                            quantity INT NOT NULL,
                            unit_price DECIMAL(18,2) NOT NULL,
                            CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
                            CONSTRAINT fk_cart_items_variant FOREIGN KEY (variant_id) REFERENCES variants(id)
);

-- 4. MODULE ORDERS
CREATE TABLE orders (
                        id VARCHAR(255) PRIMARY KEY,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP,
                        order_no VARCHAR(100) UNIQUE NOT NULL,
                        user_id VARCHAR(255),

                        subtotal_amount DECIMAL(18,2) NOT NULL,
                        discount_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
                        shipping_fee DECIMAL(18,2) NOT NULL DEFAULT 0,
                        total_amount DECIMAL(18,2) NOT NULL,

                        status VARCHAR(50) NOT NULL,
                        payment_method VARCHAR(50) NOT NULL,
                        payment_status VARCHAR(50) NOT NULL,

                        ship_full_name VARCHAR(255) NOT NULL,
                        ship_phone VARCHAR(50) NOT NULL,
                        ship_address VARCHAR(500) NOT NULL,
                        ship_city VARCHAR(100) NOT NULL,
                        ship_district VARCHAR(100) NOT NULL,
                        ship_ward VARCHAR(100) NOT NULL,

                        shipping_mode VARCHAR(50) NOT NULL,
                        shipping_provider VARCHAR(50),
                        shipping_meta JSONB,

                        CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE order_items (
                             id VARCHAR(255) PRIMARY KEY,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP,
                             order_id VARCHAR(255) NOT NULL,
                             variant_id VARCHAR(255) NOT NULL,

                             sku VARCHAR(100),
                             name VARCHAR(255) NOT NULL,
                             options_snapshot JSONB,
                             image_url VARCHAR(500),

                             quantity INT NOT NULL,
                             unit_price DECIMAL(18,2) NOT NULL,
                             line_total DECIMAL(18,2) NOT NULL,

                             CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                             CONSTRAINT fk_order_items_variant FOREIGN KEY (variant_id) REFERENCES variants(id)
);

-- 5. MODULE PAYMENTS
CREATE TABLE payment_transactions (
                                      id VARCHAR(255) PRIMARY KEY,
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP,
                                      order_id VARCHAR(255) NOT NULL,
                                      provider VARCHAR(50) NOT NULL,
                                      method VARCHAR(50) NOT NULL,
                                      amount DECIMAL(18,2) NOT NULL,
                                      status VARCHAR(50) NOT NULL,
                                      txn_ref VARCHAR(255) UNIQUE NOT NULL,
                                      raw_payload JSONB,
                                      CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- 6. MODULE REVIEWS
CREATE TABLE reviews (
                         id VARCHAR(255) PRIMARY KEY,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         user_id VARCHAR(255) NOT NULL,
                         product_id VARCHAR(255) NOT NULL,
                         order_id VARCHAR(255) NOT NULL,
                         rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
                         content TEXT,
                         CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                         CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
                         CONSTRAINT fk_reviews_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- 7. MODULE CHAT
CREATE TABLE chat_rooms (
                            id VARCHAR(255) PRIMARY KEY,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP,
                            user_id VARCHAR(255) UNIQUE NOT NULL,
                            staff_id VARCHAR(255),
                            CONSTRAINT fk_chat_rooms_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            CONSTRAINT fk_chat_rooms_staff FOREIGN KEY (staff_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE chat_messages (
                               id VARCHAR(255) PRIMARY KEY,
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               room_id VARCHAR(255) NOT NULL,
                               sender_id VARCHAR(255) NOT NULL,
                               content TEXT NOT NULL,
                               CONSTRAINT fk_chat_messages_room FOREIGN KEY (room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE,
                               CONSTRAINT fk_chat_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
);