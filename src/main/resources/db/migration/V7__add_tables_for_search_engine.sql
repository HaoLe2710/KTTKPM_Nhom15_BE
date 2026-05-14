CREATE EXTENSION IF NOT EXISTS unaccent;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

ALTER TABLE order_items
    ADD COLUMN IF NOT EXISTS product_id VARCHAR(255);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_order_items_product'
          AND table_name = 'order_items'
    ) THEN
        ALTER TABLE order_items
            ADD CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id);
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS product_search_documents (
    product_id VARCHAR(255) PRIMARY KEY,
    locale VARCHAR(20) NOT NULL DEFAULT 'vi',
    slug VARCHAR(255) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_name_normalized VARCHAR(255) NOT NULL,
    brand_id VARCHAR(255),
    brand_name VARCHAR(255),
    brand_name_normalized VARCHAR(255),
    type_id VARCHAR(255),
    type_name VARCHAR(255),
    short_description VARCHAR(500),
    min_price DECIMAL(18,2),
    max_price DECIMAL(18,2),
    in_stock BOOLEAN NOT NULL DEFAULT FALSE,
    active_variant_count INT NOT NULL DEFAULT 0,
    average_rating DECIMAL(5,2) NOT NULL DEFAULT 0,
    review_count INT NOT NULL DEFAULT 0,
    sold_count INT NOT NULL DEFAULT 0,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    is_new BOOLEAN NOT NULL DEFAULT FALSE,
    is_best_seller BOOLEAN NOT NULL DEFAULT FALSE,
    manual_boost INT NOT NULL DEFAULT 0,
    source_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    projection_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    projection_version BIGINT NOT NULL DEFAULT 1,
    title_vector tsvector,
    keyword_vector tsvector,
    body_vector tsvector,
    document_text TEXT NOT NULL DEFAULT '',
    CONSTRAINT fk_product_search_documents_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_product_search_documents_slug ON product_search_documents (slug);
CREATE INDEX IF NOT EXISTS idx_product_search_documents_product_name_normalized_trgm ON product_search_documents USING GIN (product_name_normalized gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_product_search_documents_brand_name_normalized_trgm ON product_search_documents USING GIN (brand_name_normalized gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_product_search_documents_type_id ON product_search_documents (type_id);
CREATE INDEX IF NOT EXISTS idx_product_search_documents_price ON product_search_documents (min_price, max_price);
CREATE INDEX IF NOT EXISTS idx_product_search_documents_stock ON product_search_documents (in_stock);
CREATE INDEX IF NOT EXISTS idx_product_search_documents_projection_updated_at ON product_search_documents (projection_updated_at);
CREATE INDEX IF NOT EXISTS idx_product_search_documents_source_updated_at ON product_search_documents (source_updated_at);
CREATE INDEX IF NOT EXISTS idx_product_search_documents_title_vector ON product_search_documents USING GIN (title_vector);
CREATE INDEX IF NOT EXISTS idx_product_search_documents_keyword_vector ON product_search_documents USING GIN (keyword_vector);
CREATE INDEX IF NOT EXISTS idx_product_search_documents_body_vector ON product_search_documents USING GIN (body_vector);

CREATE TABLE IF NOT EXISTS product_search_skus (
    variant_id VARCHAR(255) PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL,
    sku_raw VARCHAR(255) NOT NULL,
    sku_normalized VARCHAR(255) NOT NULL,
    CONSTRAINT fk_product_search_skus_variant FOREIGN KEY (variant_id) REFERENCES variants(id) ON DELETE CASCADE,
    CONSTRAINT fk_product_search_skus_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_product_search_skus_product_id ON product_search_skus (product_id);
CREATE INDEX IF NOT EXISTS idx_product_search_skus_normalized ON product_search_skus (sku_normalized);
CREATE INDEX IF NOT EXISTS idx_product_search_skus_normalized_trgm ON product_search_skus USING GIN (sku_normalized gin_trgm_ops);

CREATE TABLE IF NOT EXISTS product_search_facet_values (
    product_id VARCHAR(255) NOT NULL,
    facet_key VARCHAR(100) NOT NULL,
    facet_value VARCHAR(255) NOT NULL,
    facet_label VARCHAR(255) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    PRIMARY KEY (product_id, facet_key, facet_value),
    CONSTRAINT fk_product_search_facet_values_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_product_search_facet_values_lookup ON product_search_facet_values (facet_key, facet_value, product_id);
CREATE INDEX IF NOT EXISTS idx_product_search_facet_values_product_id ON product_search_facet_values (product_id);

CREATE TABLE IF NOT EXISTS search_synonym_groups (
    id VARCHAR(255) PRIMARY KEY,
    code VARCHAR(100) UNIQUE NOT NULL,
    locale VARCHAR(20) NOT NULL DEFAULT 'vi',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS search_synonym_terms (
    id VARCHAR(255) PRIMARY KEY,
    synonym_group_id VARCHAR(255) NOT NULL,
    term VARCHAR(255) NOT NULL,
    term_normalized VARCHAR(255) NOT NULL,
    CONSTRAINT fk_search_synonym_terms_group FOREIGN KEY (synonym_group_id) REFERENCES search_synonym_groups(id) ON DELETE CASCADE,
    CONSTRAINT uq_search_synonym_term UNIQUE (synonym_group_id, term_normalized)
);

CREATE INDEX IF NOT EXISTS idx_search_synonym_terms_term_normalized ON search_synonym_terms (term_normalized);

CREATE TABLE IF NOT EXISTS search_suggestions (
    id VARCHAR(255) PRIMARY KEY,
    keyword VARCHAR(255) NOT NULL,
    keyword_normalized VARCHAR(255) NOT NULL,
    locale VARCHAR(20) NOT NULL DEFAULT 'vi',
    weight INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_search_suggestions_keyword_normalized ON search_suggestions (keyword_normalized);

CREATE TABLE IF NOT EXISTS search_redirect_rules (
    id VARCHAR(255) PRIMARY KEY,
    query_pattern VARCHAR(255) NOT NULL,
    query_pattern_normalized VARCHAR(255) NOT NULL,
    locale VARCHAR(20) NOT NULL DEFAULT 'vi',
    exact_match BOOLEAN NOT NULL DEFAULT TRUE,
    redirect_type VARCHAR(50) NOT NULL,
    redirect_target VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_search_redirect_rules_pattern ON search_redirect_rules (query_pattern_normalized, locale, is_active);

CREATE TABLE IF NOT EXISTS search_boost_rules (
    id VARCHAR(255) PRIMARY KEY,
    query_pattern VARCHAR(255) NOT NULL,
    query_pattern_normalized VARCHAR(255) NOT NULL,
    locale VARCHAR(20) NOT NULL DEFAULT 'vi',
    exact_match BOOLEAN NOT NULL DEFAULT FALSE,
    facet_key VARCHAR(100),
    facet_value VARCHAR(255),
    product_id VARCHAR(255),
    boost_value INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_search_boost_rules_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_search_boost_rules_pattern ON search_boost_rules (query_pattern_normalized, locale, is_active);

CREATE TABLE IF NOT EXISTS search_product_boost_overrides (
    id VARCHAR(255) PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL,
    boost_value INT NOT NULL,
    reason VARCHAR(500) NOT NULL,
    starts_at TIMESTAMP,
    ends_at TIMESTAMP,
    updated_by VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_search_product_boost_overrides_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT ck_search_product_boost_overrides_value CHECK (boost_value BETWEEN -50 AND 50)
);

CREATE INDEX IF NOT EXISTS idx_search_product_boost_overrides_product_id ON search_product_boost_overrides (product_id);
CREATE INDEX IF NOT EXISTS idx_search_product_boost_overrides_active_window ON search_product_boost_overrides (starts_at, ends_at);

CREATE TABLE IF NOT EXISTS search_query_logs (
    id VARCHAR(255) PRIMARY KEY,
    query_text VARCHAR(500) NOT NULL,
    query_normalized VARCHAR(500),
    locale VARCHAR(20) NOT NULL DEFAULT 'vi',
    result_count INT NOT NULL DEFAULT 0,
    total_latency_ms INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_search_query_logs_created_at ON search_query_logs (created_at);

CREATE TABLE IF NOT EXISTS search_click_logs (
    id VARCHAR(255) PRIMARY KEY,
    query_log_id VARCHAR(255) NOT NULL,
    product_id VARCHAR(255) NOT NULL,
    clicked_position INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_search_click_logs_query FOREIGN KEY (query_log_id) REFERENCES search_query_logs(id) ON DELETE CASCADE,
    CONSTRAINT fk_search_click_logs_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS search_zero_result_queries (
    id VARCHAR(255) PRIMARY KEY,
    query_text VARCHAR(500) NOT NULL,
    query_normalized VARCHAR(500),
    locale VARCHAR(20) NOT NULL DEFAULT 'vi',
    occurrence_count INT NOT NULL DEFAULT 1,
    last_seen_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_search_zero_result_queries_unique ON search_zero_result_queries (query_normalized, locale);

CREATE TABLE IF NOT EXISTS search_projection_tasks (
    product_id VARCHAR(255) PRIMARY KEY,
    reason VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_error TEXT,
    last_attempt_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_search_projection_tasks_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_search_projection_tasks_status_next_attempt ON search_projection_tasks (status, next_attempt_at);

CREATE TABLE IF NOT EXISTS search_projection_runs (
    id VARCHAR(255) PRIMARY KEY,
    run_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    cursor_product_id VARCHAR(255),
    processed_count INT NOT NULL DEFAULT 0,
    failed_count INT NOT NULL DEFAULT 0,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS search_projection_failures (
    id VARCHAR(255) PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    error_message TEXT NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    failed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    next_reconciliation_at TIMESTAMP,
    resolved_at TIMESTAMP,
    CONSTRAINT fk_search_projection_failures_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_search_projection_failures_product_id ON search_projection_failures (product_id, resolved_at);
