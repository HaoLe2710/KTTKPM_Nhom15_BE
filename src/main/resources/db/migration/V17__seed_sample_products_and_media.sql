-- Seed sample catalog products and media for shared dev environments.
-- This migration is additive and attempts to be idempotent for repeated local runs.

-- =========================================================
-- 1) SAMPLE MASTER DATA
-- =========================================================

WITH src(id, code, name) AS (
    VALUES
        ('pt_sample_cleanser', 'sua-rua-mat', 'Sữa rửa mặt'),
        ('pt_sample_serum', 'serum', 'Serum'),
        ('pt_sample_moisturizer', 'kem-duong', 'Kem dưỡng'),
        ('pt_sample_sunscreen', 'chong-nang', 'Chống nắng'),
        ('pt_sample_toner', 'toner', 'Toner')
)
INSERT INTO product_types (id, code, name)
SELECT s.id, s.code, s.name
FROM src s
WHERE NOT EXISTS (
    SELECT 1 FROM product_types t WHERE LOWER(t.code) = LOWER(s.code)
);

WITH src(id, code, name, slug, description, logo_url, is_active) AS (
    VALUES
        ('br_sample_dermalab', 'sample-dermalab', 'DermaLab', 'dermalab', 'Clinical skincare brand', NULL, TRUE),
        ('br_sample_lumora', 'sample-lumora', 'Lumora', 'lumora', 'Brightening skincare brand', NULL, TRUE),
        ('br_sample_calmist', 'sample-calmist', 'Calmist', 'calmist', 'Sensitive skin care brand', NULL, TRUE),
        ('br_sample_solure', 'sample-solure', 'Solure', 'solure', 'Daily UV care brand', NULL, TRUE)
)
INSERT INTO brands (id, code, name, slug, description, logo_url, is_active, created_at, updated_at)
SELECT s.id, s.code, s.name, s.slug, s.description, s.logo_url, s.is_active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM src s
WHERE NOT EXISTS (
    SELECT 1 FROM brands b
    WHERE LOWER(b.code) = LOWER(s.code) OR LOWER(b.slug) = LOWER(s.slug)
);

WITH src(id, code, name, normalized_name, inci_name, description, is_active) AS (
    VALUES
        ('ing_sample_niacinamide', 'niacinamide', 'Niacinamide', 'niacinamide', 'Niacinamide', 'Helps brighten and control oil', TRUE),
        ('ing_sample_hyaluronic_acid', 'hyaluronic-acid', 'Hyaluronic Acid', 'hyaluronic acid', 'Sodium Hyaluronate', 'Hydrates skin', TRUE),
        ('ing_sample_ceramide', 'ceramide', 'Ceramide', 'ceramide', 'Ceramide NP', 'Supports skin barrier', TRUE),
        ('ing_sample_salicylic_acid', 'salicylic-acid', 'Salicylic Acid', 'salicylic acid', 'Salicylic Acid', 'Helps clear pores', TRUE),
        ('ing_sample_panthenol', 'panthenol', 'Panthenol', 'panthenol', 'Panthenol', 'Soothes and repairs skin', TRUE),
        ('ing_sample_glycerin', 'glycerin', 'Glycerin', 'glycerin', 'Glycerin', 'Basic humectant', TRUE),
        ('ing_sample_centella', 'centella', 'Centella Asiatica', 'centella asiatica', 'Centella Asiatica Extract', 'Calms sensitive skin', TRUE),
        ('ing_sample_zinc_pca', 'zinc-pca', 'Zinc PCA', 'zinc pca', 'Zinc PCA', 'Helps reduce excess oil', TRUE)
)
INSERT INTO ingredients (id, code, name, normalized_name, inci_name, description, is_active, created_at, updated_at)
SELECT s.id, s.code, s.name, s.normalized_name, s.inci_name, s.description, s.is_active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM src s
WHERE NOT EXISTS (
    SELECT 1 FROM ingredients i WHERE LOWER(i.code) = LOWER(s.code)
);

WITH src(id, code, name, description, is_active) AS (
    VALUES
        ('st_sample_oily', 'da-dau', 'Da dầu', 'Da tiết nhiều bã nhờn', TRUE),
        ('st_sample_dry', 'da-kho', 'Da khô', 'Da dễ bong tróc và thiếu ẩm', TRUE),
        ('st_sample_combo', 'da-hon-hop', 'Da hỗn hợp', 'Da vừa dầu vừa khô', TRUE),
        ('st_sample_sensitive', 'da-nhay-cam', 'Da nhạy cảm', 'Da dễ kích ứng', TRUE)
)
INSERT INTO skin_types (id, code, name, description, is_active, created_at, updated_at)
SELECT s.id, s.code, s.name, s.description, s.is_active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM src s
WHERE NOT EXISTS (
    SELECT 1 FROM skin_types t WHERE LOWER(t.code) = LOWER(s.code)
);

WITH src(id, code, name, description, is_active) AS (
    VALUES
        ('co_sample_acne', 'mun', 'Mụn', 'Hỗ trợ da dễ nổi mụn', TRUE),
        ('co_sample_dehydrated', 'mat-nuoc', 'Mất nước', 'Da thiếu nước và thiếu ẩm', TRUE),
        ('co_sample_irritated', 'kich-ung', 'Kích ứng', 'Da đang nhạy cảm hoặc tổn thương', TRUE),
        ('co_sample_darkspots', 'tham-sam', 'Thâm sạm', 'Da không đều màu và có thâm', TRUE)
)
INSERT INTO concerns (id, code, name, description, is_active, created_at, updated_at)
SELECT s.id, s.code, s.name, s.description, s.is_active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM src s
WHERE NOT EXISTS (
    SELECT 1 FROM concerns c WHERE LOWER(c.code) = LOWER(s.code)
);

WITH src(id, code, name, description, is_active) AS (
    VALUES
        ('tag_sample_best_seller', 'ban-chay', 'Bán chạy', 'Top doanh số', TRUE),
        ('tag_sample_new', 'san-pham-moi', 'Sản phẩm mới', 'Mới ra mắt', TRUE),
        ('tag_sample_fragrance_free', 'khong-huong-lieu', 'Không hương liệu', 'Phù hợp da nhạy cảm', TRUE),
        ('tag_sample_vegan', 'thuan-chay', 'Thuần chay', 'Vegan friendly', TRUE)
)
INSERT INTO tags (id, code, name, description, is_active, created_at, updated_at)
SELECT s.id, s.code, s.name, s.description, s.is_active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM src s
WHERE NOT EXISTS (
    SELECT 1 FROM tags t WHERE LOWER(t.code) = LOWER(s.code)
);

WITH src(id, code, name, is_active) AS (
    VALUES
        ('opt_sample_volume', 'volume', 'Dung tích', TRUE)
)
INSERT INTO options (id, code, name, is_active)
SELECT s.id, s.code, s.name, s.is_active
FROM src s
WHERE NOT EXISTS (
    SELECT 1 FROM options o WHERE LOWER(o.code) = LOWER(s.code)
);

WITH src(id, option_code, value, sort_order, is_active) AS (
    VALUES
        ('ov_sample_30ml', 'volume', '30ml', 0, TRUE),
        ('ov_sample_50ml', 'volume', '50ml', 1, TRUE),
        ('ov_sample_100ml', 'volume', '100ml', 2, TRUE),
        ('ov_sample_150ml', 'volume', '150ml', 3, TRUE),
        ('ov_sample_200ml', 'volume', '200ml', 4, TRUE)
)
INSERT INTO option_values (id, option_id, value, is_active, sort_order)
SELECT s.id, o.id, s.value, s.is_active, s.sort_order
FROM src s
         JOIN options o ON LOWER(o.code) = LOWER(s.option_code)
WHERE NOT EXISTS (
    SELECT 1
    FROM option_values ov
    WHERE ov.option_id = o.id
      AND LOWER(ov.value) = LOWER(s.value)
);

-- =========================================================
-- 2) SAMPLE PRODUCTS
-- =========================================================

WITH src(
         id, type_code, name, slug, description_md, short_description, brand_code, is_customizable, is_active
    ) AS (
    VALUES
        (
            'prd_sample_gentle_cleanser',
            'sua-rua-mat',
            'Gentle Amino Cleanser',
            'gentle-amino-cleanser-sample',
            'Low-foam cleanser for dry and sensitive skin.',
            'Gentle cleanser that removes dirt without stripping moisture.',
            'sample-dermalab',
            FALSE,
            TRUE
        ),
        (
            'prd_sample_niacinamide_serum',
            'serum',
            'Niacinamide 10 Serum',
            'niacinamide-10-serum-sample',
            'Daily serum for brightening and oil control.',
            'Lightweight niacinamide serum for acne prone skin.',
            'sample-lumora',
            FALSE,
            TRUE
        ),
        (
            'prd_sample_barrier_cream',
            'kem-duong',
            'Barrier Repair Cream',
            'barrier-repair-cream-sample',
            'Rich cream to support the skin barrier overnight.',
            'Moisturizer for dry and irritated skin.',
            'sample-calmist',
            FALSE,
            TRUE
        ),
        (
            'prd_sample_uv_shield',
            'chong-nang',
            'Daily UV Shield SPF50+',
            'daily-uv-shield-spf50-sample',
            'Broad spectrum sunscreen with comfortable finish.',
            'Daily sunscreen for combination and oily skin.',
            'sample-solure',
            FALSE,
            TRUE
        ),
        (
            'prd_sample_pore_toner',
            'toner',
            'Pore Reset Toner',
            'pore-reset-toner-sample',
            'Balancing toner that helps reduce excess oil.',
            'Refreshing toner for oily and acne prone skin.',
            'sample-lumora',
            FALSE,
            TRUE
        ),
        (
            'prd_sample_cica_gel',
            'kem-duong',
            'Cica B5 Recovery Gel',
            'cica-b5-recovery-gel-sample',
            'Cooling gel moisturizer for stressed skin.',
            'Light gel cream for sensitive and dehydrated skin.',
            'sample-calmist',
            FALSE,
            TRUE
        )
)
INSERT INTO products (
    id, created_at, updated_at, type_id, name, slug, description_md, short_description, brand_id, is_customizable, is_active
)
SELECT
    s.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    pt.id,
    s.name,
    s.slug,
    s.description_md,
    s.short_description,
    b.id,
    s.is_customizable,
    s.is_active
FROM src s
         JOIN product_types pt ON LOWER(pt.code) = LOWER(s.type_code)
         LEFT JOIN brands b ON LOWER(b.code) = LOWER(s.brand_code)
WHERE NOT EXISTS (
    SELECT 1 FROM products p WHERE LOWER(p.slug) = LOWER(s.slug)
);

-- =========================================================
-- 3) SAMPLE VARIANTS
-- =========================================================

WITH src(id, product_slug, sku, price, stock_quantity, is_active) AS (
    VALUES
        ('var_sample_gentle_cleanser_150', 'gentle-amino-cleanser-sample', 'SMP-CLN-GENTLE-150', 189000, 120, TRUE),
        ('var_sample_gentle_cleanser_200', 'gentle-amino-cleanser-sample', 'SMP-CLN-GENTLE-200', 229000, 90, TRUE),
        ('var_sample_niacinamide_30', 'niacinamide-10-serum-sample', 'SMP-SER-NIA-030', 259000, 110, TRUE),
        ('var_sample_niacinamide_50', 'niacinamide-10-serum-sample', 'SMP-SER-NIA-050', 349000, 80, TRUE),
        ('var_sample_barrier_50', 'barrier-repair-cream-sample', 'SMP-CRM-BARRIER-050', 289000, 95, TRUE),
        ('var_sample_uv_50', 'daily-uv-shield-spf50-sample', 'SMP-SUN-UVSHIELD-050', 319000, 140, TRUE),
        ('var_sample_toner_150', 'pore-reset-toner-sample', 'SMP-TON-PORE-150', 219000, 100, TRUE),
        ('var_sample_cica_50', 'cica-b5-recovery-gel-sample', 'SMP-GEL-CICA-050', 249000, 85, TRUE),
        ('var_sample_cica_100', 'cica-b5-recovery-gel-sample', 'SMP-GEL-CICA-100', 339000, 60, TRUE)
)
INSERT INTO variants (
    id, created_at, updated_at, product_id, sku, price, stock_quantity, is_active
)
SELECT
    s.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    p.id,
    s.sku,
    s.price,
    s.stock_quantity,
    s.is_active
FROM src s
         JOIN products p ON LOWER(p.slug) = LOWER(s.product_slug)
WHERE NOT EXISTS (
    SELECT 1 FROM variants v WHERE UPPER(v.sku) = UPPER(s.sku)
);

-- =========================================================
-- 4) SAMPLE VARIANT OPTIONS
-- =========================================================

WITH src(variant_sku, option_code, option_value) AS (
    VALUES
        ('SMP-CLN-GENTLE-150', 'volume', '150ml'),
        ('SMP-CLN-GENTLE-200', 'volume', '200ml'),
        ('SMP-SER-NIA-030', 'volume', '30ml'),
        ('SMP-SER-NIA-050', 'volume', '50ml'),
        ('SMP-CRM-BARRIER-050', 'volume', '50ml'),
        ('SMP-SUN-UVSHIELD-050', 'volume', '50ml'),
        ('SMP-TON-PORE-150', 'volume', '150ml'),
        ('SMP-GEL-CICA-050', 'volume', '50ml'),
        ('SMP-GEL-CICA-100', 'volume', '100ml')
)
INSERT INTO variant_options (id, variant_id, option_value_id)
SELECT
    md5(s.variant_sku || '|' || s.option_code || '|' || s.option_value),
    v.id,
    ov.id
FROM src s
         JOIN variants v ON UPPER(v.sku) = UPPER(s.variant_sku)
         JOIN options o ON LOWER(o.code) = LOWER(s.option_code)
         JOIN option_values ov ON ov.option_id = o.id AND LOWER(ov.value) = LOWER(s.option_value)
WHERE NOT EXISTS (
    SELECT 1
    FROM variant_options vo
    WHERE vo.variant_id = v.id
      AND vo.option_value_id = ov.id
);

-- =========================================================
-- 5) SAMPLE PRODUCT SEMANTIC RELATIONS
-- =========================================================

WITH src(product_slug, ingredient_code, concentration_label, is_highlighted, sort_order) AS (
    VALUES
        ('gentle-amino-cleanser-sample', 'ceramide', NULL, TRUE, 0),
        ('gentle-amino-cleanser-sample', 'hyaluronic-acid', NULL, TRUE, 1),
        ('gentle-amino-cleanser-sample', 'glycerin', NULL, FALSE, 2),
        ('niacinamide-10-serum-sample', 'niacinamide', '10%', TRUE, 0),
        ('niacinamide-10-serum-sample', 'zinc-pca', NULL, TRUE, 1),
        ('niacinamide-10-serum-sample', 'hyaluronic-acid', NULL, FALSE, 2),
        ('barrier-repair-cream-sample', 'ceramide', NULL, TRUE, 0),
        ('barrier-repair-cream-sample', 'panthenol', '5%', TRUE, 1),
        ('barrier-repair-cream-sample', 'glycerin', NULL, FALSE, 2),
        ('daily-uv-shield-spf50-sample', 'niacinamide', NULL, FALSE, 0),
        ('daily-uv-shield-spf50-sample', 'panthenol', NULL, TRUE, 1),
        ('pore-reset-toner-sample', 'salicylic-acid', '2%', TRUE, 0),
        ('pore-reset-toner-sample', 'zinc-pca', NULL, TRUE, 1),
        ('pore-reset-toner-sample', 'glycerin', NULL, FALSE, 2),
        ('cica-b5-recovery-gel-sample', 'centella', NULL, TRUE, 0),
        ('cica-b5-recovery-gel-sample', 'panthenol', '5%', TRUE, 1),
        ('cica-b5-recovery-gel-sample', 'hyaluronic-acid', NULL, FALSE, 2)
)
INSERT INTO product_ingredients (
    id, product_id, ingredient_id, concentration_label, is_highlighted, sort_order, created_at, updated_at
)
SELECT
    md5(s.product_slug || '|ingredient|' || s.ingredient_code),
    p.id,
    i.id,
    s.concentration_label,
    s.is_highlighted,
    s.sort_order,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM src s
         JOIN products p ON LOWER(p.slug) = LOWER(s.product_slug)
         JOIN ingredients i ON LOWER(i.code) = LOWER(s.ingredient_code)
WHERE NOT EXISTS (
    SELECT 1
    FROM product_ingredients pi
    WHERE pi.product_id = p.id
      AND pi.ingredient_id = i.id
);

WITH src(product_slug, skin_type_code) AS (
    VALUES
        ('gentle-amino-cleanser-sample', 'da-kho'),
        ('gentle-amino-cleanser-sample', 'da-nhay-cam'),
        ('niacinamide-10-serum-sample', 'da-dau'),
        ('niacinamide-10-serum-sample', 'da-hon-hop'),
        ('barrier-repair-cream-sample', 'da-kho'),
        ('barrier-repair-cream-sample', 'da-nhay-cam'),
        ('daily-uv-shield-spf50-sample', 'da-dau'),
        ('daily-uv-shield-spf50-sample', 'da-hon-hop'),
        ('pore-reset-toner-sample', 'da-dau'),
        ('pore-reset-toner-sample', 'da-hon-hop'),
        ('cica-b5-recovery-gel-sample', 'da-nhay-cam'),
        ('cica-b5-recovery-gel-sample', 'da-hon-hop')
)
INSERT INTO product_skin_types (id, product_id, skin_type_id, created_at, updated_at)
SELECT
    md5(s.product_slug || '|skin|' || s.skin_type_code),
    p.id,
    st.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM src s
         JOIN products p ON LOWER(p.slug) = LOWER(s.product_slug)
         JOIN skin_types st ON LOWER(st.code) = LOWER(s.skin_type_code)
WHERE NOT EXISTS (
    SELECT 1
    FROM product_skin_types pst
    WHERE pst.product_id = p.id
      AND pst.skin_type_id = st.id
);

WITH src(product_slug, concern_code) AS (
    VALUES
        ('gentle-amino-cleanser-sample', 'mat-nuoc'),
        ('gentle-amino-cleanser-sample', 'kich-ung'),
        ('niacinamide-10-serum-sample', 'mun'),
        ('niacinamide-10-serum-sample', 'tham-sam'),
        ('barrier-repair-cream-sample', 'kich-ung'),
        ('barrier-repair-cream-sample', 'mat-nuoc'),
        ('daily-uv-shield-spf50-sample', 'tham-sam'),
        ('daily-uv-shield-spf50-sample', 'kich-ung'),
        ('pore-reset-toner-sample', 'mun'),
        ('cica-b5-recovery-gel-sample', 'kich-ung'),
        ('cica-b5-recovery-gel-sample', 'mat-nuoc')
)
INSERT INTO product_concerns (id, product_id, concern_id, created_at, updated_at)
SELECT
    md5(s.product_slug || '|concern|' || s.concern_code),
    p.id,
    c.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM src s
         JOIN products p ON LOWER(p.slug) = LOWER(s.product_slug)
         JOIN concerns c ON LOWER(c.code) = LOWER(s.concern_code)
WHERE NOT EXISTS (
    SELECT 1
    FROM product_concerns pc
    WHERE pc.product_id = p.id
      AND pc.concern_id = c.id
);

WITH src(product_slug, tag_code) AS (
    VALUES
        ('gentle-amino-cleanser-sample', 'khong-huong-lieu'),
        ('gentle-amino-cleanser-sample', 'ban-chay'),
        ('niacinamide-10-serum-sample', 'ban-chay'),
        ('niacinamide-10-serum-sample', 'san-pham-moi'),
        ('barrier-repair-cream-sample', 'khong-huong-lieu'),
        ('daily-uv-shield-spf50-sample', 'san-pham-moi'),
        ('pore-reset-toner-sample', 'ban-chay'),
        ('cica-b5-recovery-gel-sample', 'san-pham-moi'),
        ('cica-b5-recovery-gel-sample', 'thuan-chay')
)
INSERT INTO product_tags (id, product_id, tag_id, created_at, updated_at)
SELECT
    md5(s.product_slug || '|tag|' || s.tag_code),
    p.id,
    t.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM src s
         JOIN products p ON LOWER(p.slug) = LOWER(s.product_slug)
         JOIN tags t ON LOWER(t.code) = LOWER(s.tag_code)
WHERE NOT EXISTS (
    SELECT 1
    FROM product_tags pt
    WHERE pt.product_id = p.id
      AND pt.tag_id = t.id
);

-- =========================================================
-- 6) MEDIA
-- =========================================================

WITH src(id, product_id, url, public_id, type, is_primary) AS (
    VALUES
        ('med_sample_barrier_cream_primary', 'prd_sample_barrier_cream', 'https://koreanskincare.co.uk/cdn/shop/files/Product-page-sizes_f45fae42-9c3c-4296-9e3e-f528f2df25bd.jpg?v=1751281546', 'sample/barrier-cream/main', 'IMAGE', TRUE),
        ('med_sample_gentle_cleanser_alt_1', 'prd_sample_gentle_cleanser', 'https://koreanskincare.co.uk/cdn/shop/files/Product-page-sizes_88a1c34a-c66d-4c6c-ba2c-472089b19f4f.jpg?v=1751299537', 'sample/gentle-cleanser/alt-1', 'IMAGE', FALSE),
        ('med_sample_gentle_cleanser_primary', 'prd_sample_gentle_cleanser', 'https://koreanskincare.co.uk/cdn/shop/files/Product-page-sizes_8939f818-e5d8-475b-9879-699f028a716c.jpg?v=1751398528', 'sample/gentle-cleanser/main', 'IMAGE', FALSE),
        ('med_sample_niacinamide_primary', 'prd_sample_niacinamide_serum', 'https://www.larocheposay.vn/-/media/project/loreal/brand-sites/lrp/apac/vn/products/cicaplast/gel-b5-pro-recovery/damaged/la-roche-posay-productpage-damaged-cicaplast-gel-b5-pro-recovery-40ml-3337875586269-front.png?sc_lang=vi-vn', 'sample/niacinamide-serum/main', 'IMAGE', TRUE),
        ('med_sample_pore_toner_primary', 'prd_sample_pore_toner', 'https://koreanskincare.co.uk/cdn/shop/files/Korean-Skincare-Official_61a0dfd2-b112-4a88-84c5-1de97a63a4b1.jpg?v=1756820101', 'sample/pore-toner/main', 'IMAGE', TRUE),
        ('med_sample_niacinamide_alt_1', 'prd_sample_niacinamide_serum', 'https://koreanskincare.co.uk/cdn/shop/files/Product-page-sizes_53c7a07f-e1c7-4191-8585-77586b1195f8.jpg?v=1751299543', 'sample/niacinamide-serum/alt-1', 'IMAGE', FALSE),
        ('med_sample_cica_gel_alt_1', 'prd_sample_cica_gel', 'https://koreanskincare.co.uk/cdn/shop/files/Product-page-sizes_a3c3b0b2-3c93-4adc-8433-47b88f4492ca.jpg?v=1772094907', 'sample/cica-gel/alt-1', 'IMAGE', FALSE),
        ('med_prd_pure_niacinamide_lrp_primary', 'prd_pure_niacinamide_lrp', 'https://koreanskincare.co.uk/cdn/shop/files/Korean-Skincare-Official_91184dab-ee06-4e94-b1ab-53e7ab6e706a.jpg?v=1760510100', 'seed/prd_pure_niacinamide_lrp/main', 'IMAGE', TRUE),
        ('med_prd_cicaplast_b5_lrp_primary', 'prd_cicaplast_b5_lrp', 'https://koreanskincare.co.uk/cdn/shop/files/Product-page-sizes_620f5b36-6a8e-4401-a52d-1a52ea9e1401.jpg?v=1751281541', 'seed/prd_cicaplast_b5_lrp/main', 'IMAGE', TRUE),
        ('med_sample_barrier_cream_alt_1', 'prd_sample_barrier_cream', 'https://koreanskincare.co.uk/cdn/shop/files/Product-page-sizes_2deef43d-7db9-4f26-bbf4-5758618c19cc.jpg?v=1751308536', 'sample/barrier-cream/alt-1', 'IMAGE', FALSE),
        ('med_sample_cica_gel_primary', 'prd_sample_cica_gel', 'https://koreanskincare.co.uk/cdn/shop/files/DearKlairs_EGFBlueCalmingTonerPad_Packshot.jpg?v=1772613302', 'sample/cica-gel/main', 'IMAGE', TRUE),
        ('med_prd_sun_oil_control_eucerin_primary', 'prd_sun_oil_control_eucerin', 'https://koreanskincare.co.uk/cdn/shop/files/DearKlairs_EGFBlueACCalmingSerum_Packshot.jpg?v=1772613301', 'seed/prd_sun_oil_control_eucerin/main', 'IMAGE', TRUE),
        ('med_prd_sebium_lotion_bioderma_primary', 'prd_sebium_lotion_bioderma', 'https://koreanskincare.co.uk/cdn/shop/files/Product-page-sizes_46f463da-3430-4020-8277-12738fa8334f.jpg?v=1751567728', 'seed/prd_sebium_lotion_bioderma/main', 'IMAGE', TRUE),
        ('med_sample_uv_shield_alt_1', 'prd_sample_uv_shield', 'https://koreanskincare.co.uk/cdn/shop/files/Korean-Skincare-Official-Midnight-Romance.jpg?v=1756821916', 'sample/uv-shield/alt-1', 'IMAGE', FALSE),
        ('med_prd_hydrating_cleanser_cerave_primary', 'prd_hydrating_cleanser_cerave', 'https://koreanskincare.co.uk/cdn/shop/files/Korean-Skincare-Official-Midnight-Romance.jpg?v=1756821916', 'seed/prd_hydrating_cleanser_cerave/main', 'IMAGE', TRUE),
        ('med_sample_pore_toner_alt_1', 'prd_sample_pore_toner', 'https://koreanskincare.co.uk/cdn/shop/files/Korean-Skincare-Official-Midnight-Romance.jpg?v=1756821916', 'sample/pore-toner/alt-1', 'IMAGE', FALSE),
        ('med_prd_sample_gentle_cleanser_primary_v2', 'prd_sample_gentle_cleanser', 'https://koreanskincare.co.uk/cdn/shop/files/Product-page-sizes_b2ce102f-d6e6-4446-9293-0faa08f9899a.jpg?v=1751967335', 'seed/prd_sample_gentle_cleanser/main-v2', 'IMAGE', TRUE),
        ('med_sample_uv_shield_primary', 'prd_sample_uv_shield', 'https://koreanskincare.co.uk/cdn/shop/files/Product-page-sizes-200ml.jpg?v=1751322923', 'sample/uv-shield/main', 'IMAGE', TRUE)
)
INSERT INTO media (
    id,
    created_at,
    product_id,
    variant_id,
    url,
    public_id,
    type,
    is_primary
)
SELECT
    s.id,
    CURRENT_TIMESTAMP,
    s.product_id,
    NULL,
    s.url,
    s.public_id,
    s.type,
    s.is_primary
FROM src s
WHERE NOT EXISTS (
    SELECT 1
    FROM media m
    WHERE m.id = s.id
);
