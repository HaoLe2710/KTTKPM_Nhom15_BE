CREATE TABLE product_ingredients (
    id VARCHAR(255) PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL,
    ingredient_id VARCHAR(255) NOT NULL,
    concentration_label VARCHAR(100),
    is_highlighted BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_product_ingredients_product
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_product_ingredients_ingredient
        FOREIGN KEY (ingredient_id) REFERENCES ingredients (id) ON DELETE CASCADE,
    CONSTRAINT uq_product_ingredient UNIQUE (product_id, ingredient_id)
);

CREATE TABLE product_skin_types (
    id VARCHAR(255) PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL,
    skin_type_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_product_skin_types_product
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_product_skin_types_skin_type
        FOREIGN KEY (skin_type_id) REFERENCES skin_types (id) ON DELETE CASCADE,
    CONSTRAINT uq_product_skin_type UNIQUE (product_id, skin_type_id)
);

CREATE TABLE product_concerns (
    id VARCHAR(255) PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL,
    concern_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_product_concerns_product
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_product_concerns_concern
        FOREIGN KEY (concern_id) REFERENCES concerns (id) ON DELETE CASCADE,
    CONSTRAINT uq_product_concern UNIQUE (product_id, concern_id)
);

CREATE TABLE product_tags (
    id VARCHAR(255) PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL,
    tag_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_product_tags_product
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_product_tags_tag
        FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE,
    CONSTRAINT uq_product_tag UNIQUE (product_id, tag_id)
);

CREATE INDEX idx_product_ingredients_product_id ON product_ingredients (product_id);
CREATE INDEX idx_product_ingredients_ingredient_id ON product_ingredients (ingredient_id);
CREATE INDEX idx_product_skin_types_product_id ON product_skin_types (product_id);
CREATE INDEX idx_product_skin_types_skin_type_id ON product_skin_types (skin_type_id);
CREATE INDEX idx_product_concerns_product_id ON product_concerns (product_id);
CREATE INDEX idx_product_concerns_concern_id ON product_concerns (concern_id);
CREATE INDEX idx_product_tags_product_id ON product_tags (product_id);
CREATE INDEX idx_product_tags_tag_id ON product_tags (tag_id);
