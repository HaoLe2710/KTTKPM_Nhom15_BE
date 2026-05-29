-- Seed data for cosmetics catalog + search testing
-- Assumptions:
--   - Existing tables: product_types, products, variants, options, option_values, variant_options, media
--   - Search projection tables exist but are populated by application listeners / rebuild job, not directly seeded here.
--   - IDs are VARCHAR and can safely store human-readable seed keys.
--   - This script is idempotent enough for one-time local/dev seeding; adjust ON CONFLICT clauses to your database rules.

BEGIN;

-- =========================
-- 1) MASTER DATA
-- =========================

INSERT INTO product_types (id, code, name)
VALUES
    ('ptype_cleanser', 'CLEANSER', 'Sữa rửa mặt'),
    ('ptype_toner', 'TONER', 'Toner'),
    ('ptype_serum', 'SERUM', 'Serum'),
    ('ptype_moisturizer', 'MOISTURIZER', 'Kem dưỡng ẩm'),
    ('ptype_sunscreen', 'SUNSCREEN', 'Kem chống nắng'),
    ('ptype_mask', 'MASK', 'Mặt nạ')
ON CONFLICT (id) DO NOTHING;

INSERT INTO options (id, code, name)
VALUES
    ('opt_size', 'SIZE', 'Dung tích'),
    ('opt_finish', 'FINISH', 'Kết cấu'),
    ('opt_version', 'VERSION', 'Phiên bản')
ON CONFLICT (id) DO NOTHING;

INSERT INTO option_values (id, option_id, value, is_active)
VALUES
    ('ov_size_30ml', 'opt_size', '30ml', TRUE),
    ('ov_size_50ml', 'opt_size', '50ml', TRUE),
    ('ov_size_80ml', 'opt_size', '80ml', TRUE),
    ('ov_size_100ml', 'opt_size', '100ml', TRUE),
    ('ov_size_150ml', 'opt_size', '150ml', TRUE),
    ('ov_finish_gel', 'opt_finish', 'Gel', TRUE),
    ('ov_finish_cream', 'opt_finish', 'Cream', TRUE),
    ('ov_finish_watery', 'opt_finish', 'Watery', TRUE),
    ('ov_version_standard', 'opt_version', 'Standard', TRUE),
    ('ov_version_sensitive', 'opt_version', 'Sensitive', TRUE)
ON CONFLICT (id) DO NOTHING;

-- =========================
-- 2) PRODUCTS
-- 24 products, 48 variants total
-- =========================

INSERT INTO products (id, created_at, updated_at, type_id, name, slug, description_md, is_customizable, is_active)
VALUES
    ('prod_cleanser_001', NOW(), NOW(), 'ptype_cleanser', 'Aqua Calm Hydrating Cleanser', 'aqua-calm-hydrating-cleanser', 'Sữa rửa mặt dịu nhẹ cho da khô và da nhạy cảm.', FALSE, TRUE),
    ('prod_cleanser_002', NOW(), NOW(), 'ptype_cleanser', 'Pure Foam Acne Cleanser', 'pure-foam-acne-cleanser', 'Sữa rửa mặt hỗ trợ làm sạch bã nhờn, phù hợp da dầu mụn.', FALSE, TRUE),
    ('prod_cleanser_003', NOW(), NOW(), 'ptype_cleanser', 'Low pH Daily Cleanser', 'low-ph-daily-cleanser', 'Sữa rửa mặt pH thấp dùng hằng ngày.', FALSE, TRUE),
    ('prod_cleanser_004', NOW(), NOW(), 'ptype_cleanser', 'Green Tea Deep Cleanser', 'green-tea-deep-cleanser', 'Sữa rửa mặt chiết xuất trà xanh cho da dầu.', FALSE, TRUE),

    ('prod_toner_001', NOW(), NOW(), 'ptype_toner', 'Aqua Balance Hydrating Toner', 'aqua-balance-hydrating-toner', 'Toner cấp ẩm, làm dịu và cân bằng da.', FALSE, TRUE),
    ('prod_toner_002', NOW(), NOW(), 'ptype_toner', 'BHA Clarifying Toner', 'bha-clarifying-toner', 'Toner hỗ trợ da dầu mụn và lỗ chân lông.', FALSE, TRUE),
    ('prod_toner_003', NOW(), NOW(), 'ptype_toner', 'Centella Soothing Toner', 'centella-soothing-toner', 'Toner rau má làm dịu da nhạy cảm.', FALSE, TRUE),
    ('prod_toner_004', NOW(), NOW(), 'ptype_toner', 'Rice Brightening Toner', 'rice-brightening-toner', 'Toner gạo hỗ trợ làm sáng da xỉn màu.', FALSE, TRUE),

    ('prod_serum_001', NOW(), NOW(), 'ptype_serum', 'Niacinamide 10 Repair Serum', 'niacinamide-10-repair-serum', 'Serum niacinamide hỗ trợ giảm dầu và mờ thâm.', FALSE, TRUE),
    ('prod_serum_002', NOW(), NOW(), 'ptype_serum', 'Hyaluronic Hydra Serum', 'hyaluronic-hydra-serum', 'Serum HA cấp ẩm chuyên sâu.', FALSE, TRUE),
    ('prod_serum_003', NOW(), NOW(), 'ptype_serum', 'Retinol Night Renewal Serum', 'retinol-night-renewal-serum', 'Serum retinol phục hồi và hỗ trợ chống lão hóa.', FALSE, TRUE),
    ('prod_serum_004', NOW(), NOW(), 'ptype_serum', 'Vitamin C Glow Serum', 'vitamin-c-glow-serum', 'Serum vitamin C hỗ trợ làm sáng và đều màu.', FALSE, TRUE),

    ('prod_moist_001', NOW(), NOW(), 'ptype_moisturizer', 'Ceramide Barrier Cream', 'ceramide-barrier-cream', 'Kem dưỡng ceramide phục hồi hàng rào bảo vệ da.', FALSE, TRUE),
    ('prod_moist_002', NOW(), NOW(), 'ptype_moisturizer', 'Oil Free Aqua Gel', 'oil-free-aqua-gel', 'Gel dưỡng ẩm nhẹ mặt cho da dầu.', FALSE, TRUE),
    ('prod_moist_003', NOW(), NOW(), 'ptype_moisturizer', 'Panthenol Repair Cream', 'panthenol-repair-cream', 'Kem dưỡng phục hồi sau treatment.', FALSE, TRUE),
    ('prod_moist_004', NOW(), NOW(), 'ptype_moisturizer', 'Cica Soothing Gel Cream', 'cica-soothing-gel-cream', 'Gel cream rau má làm dịu da.', FALSE, TRUE),

    ('prod_sun_001', NOW(), NOW(), 'ptype_sunscreen', 'UV Shield Daily Sunscreen', 'uv-shield-daily-sunscreen', 'Kem chống nắng dùng hằng ngày.', FALSE, TRUE),
    ('prod_sun_002', NOW(), NOW(), 'ptype_sunscreen', 'Matte Air Sunscreen', 'matte-air-sunscreen', 'Kem chống nắng finish ráo mịn cho da dầu.', FALSE, TRUE),
    ('prod_sun_003', NOW(), NOW(), 'ptype_sunscreen', 'Hydra Dew Sunscreen', 'hydra-dew-sunscreen', 'Kem chống nắng dưỡng ẩm cho da khô.', FALSE, TRUE),
    ('prod_sun_004', NOW(), NOW(), 'ptype_sunscreen', 'Sensitive Mineral Sunscreen', 'sensitive-mineral-sunscreen', 'Chống nắng khoáng dịu nhẹ cho da nhạy cảm.', FALSE, TRUE),

    ('prod_mask_001', NOW(), NOW(), 'ptype_mask', 'Clay Pore Reset Mask', 'clay-pore-reset-mask', 'Mặt nạ đất sét hỗ trợ hút dầu.', FALSE, TRUE),
    ('prod_mask_002', NOW(), NOW(), 'ptype_mask', 'Hydra Sheet Mask', 'hydra-sheet-mask', 'Mặt nạ giấy cấp ẩm tức thì.', FALSE, TRUE),
    ('prod_mask_003', NOW(), NOW(), 'ptype_mask', 'Cica Recovery Mask', 'cica-recovery-mask', 'Mặt nạ phục hồi làm dịu da.', FALSE, TRUE),
    ('prod_mask_004', NOW(), NOW(), 'ptype_mask', 'Bright Rice Sleep Mask', 'bright-rice-sleep-mask', 'Mặt nạ ngủ hỗ trợ làm sáng.', FALSE, TRUE)
ON CONFLICT (id) DO NOTHING;

-- =========================
-- 3) VARIANTS
-- Two variants per product to exercise pricing/stock/search/order flows
-- =========================

INSERT INTO variants (id, created_at, updated_at, product_id, sku, price, stock_quantity, is_active)
VALUES
    ('var_cleanser_001_100', NOW(), NOW(), 'prod_cleanser_001', 'AQCL-100-ST', 189000, 120, TRUE),
    ('var_cleanser_001_150', NOW(), NOW(), 'prod_cleanser_001', 'AQCL-150-ST', 249000, 80, TRUE),
    ('var_cleanser_002_100', NOW(), NOW(), 'prod_cleanser_002', 'PFAC-100-ST', 179000, 130, TRUE),
    ('var_cleanser_002_150', NOW(), NOW(), 'prod_cleanser_002', 'PFAC-150-ST', 239000, 85, TRUE),
    ('var_cleanser_003_100', NOW(), NOW(), 'prod_cleanser_003', 'LPHC-100-ST', 169000, 110, TRUE),
    ('var_cleanser_003_150', NOW(), NOW(), 'prod_cleanser_003', 'LPHC-150-ST', 229000, 70, TRUE),
    ('var_cleanser_004_100', NOW(), NOW(), 'prod_cleanser_004', 'GTDC-100-ST', 199000, 90, TRUE),
    ('var_cleanser_004_150', NOW(), NOW(), 'prod_cleanser_004', 'GTDC-150-ST', 259000, 60, TRUE),

    ('var_toner_001_100', NOW(), NOW(), 'prod_toner_001', 'ABHT-100-WT', 209000, 140, TRUE),
    ('var_toner_001_150', NOW(), NOW(), 'prod_toner_001', 'ABHT-150-WT', 269000, 100, TRUE),
    ('var_toner_002_100', NOW(), NOW(), 'prod_toner_002', 'BHAT-100-WT', 239000, 95, TRUE),
    ('var_toner_002_150', NOW(), NOW(), 'prod_toner_002', 'BHAT-150-WT', 309000, 55, TRUE),
    ('var_toner_003_100', NOW(), NOW(), 'prod_toner_003', 'CSTN-100-WT', 219000, 88, TRUE),
    ('var_toner_003_150', NOW(), NOW(), 'prod_toner_003', 'CSTN-150-WT', 289000, 47, TRUE),
    ('var_toner_004_100', NOW(), NOW(), 'prod_toner_004', 'RBRT-100-WT', 229000, 76, TRUE),
    ('var_toner_004_150', NOW(), NOW(), 'prod_toner_004', 'RBRT-150-WT', 299000, 42, TRUE),

    ('var_serum_001_30', NOW(), NOW(), 'prod_serum_001', 'N10R-030-ST', 289000, 150, TRUE),
    ('var_serum_001_50', NOW(), NOW(), 'prod_serum_001', 'N10R-050-ST', 399000, 90, TRUE),
    ('var_serum_002_30', NOW(), NOW(), 'prod_serum_002', 'HHDS-030-ST', 279000, 160, TRUE),
    ('var_serum_002_50', NOW(), NOW(), 'prod_serum_002', 'HHDS-050-ST', 389000, 100, TRUE),
    ('var_serum_003_30', NOW(), NOW(), 'prod_serum_003', 'RNRS-030-ST', 329000, 74, TRUE),
    ('var_serum_003_50', NOW(), NOW(), 'prod_serum_003', 'RNRS-050-ST', 449000, 39, TRUE),
    ('var_serum_004_30', NOW(), NOW(), 'prod_serum_004', 'VCGS-030-ST', 309000, 132, TRUE),
    ('var_serum_004_50', NOW(), NOW(), 'prod_serum_004', 'VCGS-050-ST', 429000, 78, TRUE),

    ('var_moist_001_50', NOW(), NOW(), 'prod_moist_001', 'CBCR-050-CR', 319000, 125, TRUE),
    ('var_moist_001_80', NOW(), NOW(), 'prod_moist_001', 'CBCR-080-CR', 419000, 71, TRUE),
    ('var_moist_002_50', NOW(), NOW(), 'prod_moist_002', 'OFAG-050-GE', 259000, 140, TRUE),
    ('var_moist_002_80', NOW(), NOW(), 'prod_moist_002', 'OFAG-080-GE', 349000, 82, TRUE),
    ('var_moist_003_50', NOW(), NOW(), 'prod_moist_003', 'PRCR-050-CR', 299000, 91, TRUE),
    ('var_moist_003_80', NOW(), NOW(), 'prod_moist_003', 'PRCR-080-CR', 389000, 54, TRUE),
    ('var_moist_004_50', NOW(), NOW(), 'prod_moist_004', 'CSGC-050-GE', 279000, 117, TRUE),
    ('var_moist_004_80', NOW(), NOW(), 'prod_moist_004', 'CSGC-080-GE', 369000, 66, TRUE),

    ('var_sun_001_50', NOW(), NOW(), 'prod_sun_001', 'USDS-050-CR', 249000, 180, TRUE),
    ('var_sun_001_80', NOW(), NOW(), 'prod_sun_001', 'USDS-080-CR', 339000, 112, TRUE),
    ('var_sun_002_50', NOW(), NOW(), 'prod_sun_002', 'MATS-050-CR', 269000, 166, TRUE),
    ('var_sun_002_80', NOW(), NOW(), 'prod_sun_002', 'MATS-080-CR', 359000, 98, TRUE),
    ('var_sun_003_50', NOW(), NOW(), 'prod_sun_003', 'HDSS-050-CR', 259000, 103, TRUE),
    ('var_sun_003_80', NOW(), NOW(), 'prod_sun_003', 'HDSS-080-CR', 349000, 61, TRUE),
    ('var_sun_004_50', NOW(), NOW(), 'prod_sun_004', 'SMSS-050-CR', 289000, 94, TRUE),
    ('var_sun_004_80', NOW(), NOW(), 'prod_sun_004', 'SMSS-080-CR', 379000, 44, TRUE),

    ('var_mask_001_80', NOW(), NOW(), 'prod_mask_001', 'CPRM-080-CR', 219000, 102, TRUE),
    ('var_mask_001_100', NOW(), NOW(), 'prod_mask_001', 'CPRM-100-CR', 259000, 58, TRUE),
    ('var_mask_002_30', NOW(), NOW(), 'prod_mask_002', 'HDSM-030-ST', 59000, 300, TRUE),
    ('var_mask_002_50', NOW(), NOW(), 'prod_mask_002', 'HDSM-050-ST', 99000, 190, TRUE),
    ('var_mask_003_30', NOW(), NOW(), 'prod_mask_003', 'CIRM-030-ST', 69000, 240, TRUE),
    ('var_mask_003_50', NOW(), NOW(), 'prod_mask_003', 'CIRM-050-ST', 109000, 160, TRUE),
    ('var_mask_004_80', NOW(), NOW(), 'prod_mask_004', 'BRSM-080-CR', 239000, 84, TRUE),
    ('var_mask_004_100', NOW(), NOW(), 'prod_mask_004', 'BRSM-100-CR', 289000, 49, TRUE)
ON CONFLICT (id) DO NOTHING;

-- =========================
-- 4) VARIANT OPTIONS
-- =========================

INSERT INTO variant_options (id, variant_id, option_value_id)
VALUES
    ('vo_var_cleanser_001_100_size', 'var_cleanser_001_100', 'ov_size_100ml'),
    ('vo_var_cleanser_001_150_size', 'var_cleanser_001_150', 'ov_size_150ml'),
    ('vo_var_cleanser_002_100_size', 'var_cleanser_002_100', 'ov_size_100ml'),
    ('vo_var_cleanser_002_150_size', 'var_cleanser_002_150', 'ov_size_150ml'),
    ('vo_var_cleanser_003_100_size', 'var_cleanser_003_100', 'ov_size_100ml'),
    ('vo_var_cleanser_003_150_size', 'var_cleanser_003_150', 'ov_size_150ml'),
    ('vo_var_cleanser_004_100_size', 'var_cleanser_004_100', 'ov_size_100ml'),
    ('vo_var_cleanser_004_150_size', 'var_cleanser_004_150', 'ov_size_150ml'),

    ('vo_var_toner_001_100_size', 'var_toner_001_100', 'ov_size_100ml'),
    ('vo_var_toner_001_150_size', 'var_toner_001_150', 'ov_size_150ml'),
    ('vo_var_toner_002_100_size', 'var_toner_002_100', 'ov_size_100ml'),
    ('vo_var_toner_002_150_size', 'var_toner_002_150', 'ov_size_150ml'),
    ('vo_var_toner_003_100_size', 'var_toner_003_100', 'ov_size_100ml'),
    ('vo_var_toner_003_150_size', 'var_toner_003_150', 'ov_size_150ml'),
    ('vo_var_toner_004_100_size', 'var_toner_004_100', 'ov_size_100ml'),
    ('vo_var_toner_004_150_size', 'var_toner_004_150', 'ov_size_150ml'),

    ('vo_var_serum_001_30_size', 'var_serum_001_30', 'ov_size_30ml'),
    ('vo_var_serum_001_50_size', 'var_serum_001_50', 'ov_size_50ml'),
    ('vo_var_serum_002_30_size', 'var_serum_002_30', 'ov_size_30ml'),
    ('vo_var_serum_002_50_size', 'var_serum_002_50', 'ov_size_50ml'),
    ('vo_var_serum_003_30_size', 'var_serum_003_30', 'ov_size_30ml'),
    ('vo_var_serum_003_50_size', 'var_serum_003_50', 'ov_size_50ml'),
    ('vo_var_serum_004_30_size', 'var_serum_004_30', 'ov_size_30ml'),
    ('vo_var_serum_004_50_size', 'var_serum_004_50', 'ov_size_50ml'),

    ('vo_var_moist_001_50_size', 'var_moist_001_50', 'ov_size_50ml'),
    ('vo_var_moist_001_80_size', 'var_moist_001_80', 'ov_size_80ml'),
    ('vo_var_moist_002_50_size', 'var_moist_002_50', 'ov_size_50ml'),
    ('vo_var_moist_002_80_size', 'var_moist_002_80', 'ov_size_80ml'),
    ('vo_var_moist_003_50_size', 'var_moist_003_50', 'ov_size_50ml'),
    ('vo_var_moist_003_80_size', 'var_moist_003_80', 'ov_size_80ml'),
    ('vo_var_moist_004_50_size', 'var_moist_004_50', 'ov_size_50ml'),
    ('vo_var_moist_004_80_size', 'var_moist_004_80', 'ov_size_80ml'),

    ('vo_var_sun_001_50_size', 'var_sun_001_50', 'ov_size_50ml'),
    ('vo_var_sun_001_80_size', 'var_sun_001_80', 'ov_size_80ml'),
    ('vo_var_sun_002_50_size', 'var_sun_002_50', 'ov_size_50ml'),
    ('vo_var_sun_002_80_size', 'var_sun_002_80', 'ov_size_80ml'),
    ('vo_var_sun_003_50_size', 'var_sun_003_50', 'ov_size_50ml'),
    ('vo_var_sun_003_80_size', 'var_sun_003_80', 'ov_size_80ml'),
    ('vo_var_sun_004_50_size', 'var_sun_004_50', 'ov_size_50ml'),
    ('vo_var_sun_004_80_size', 'var_sun_004_80', 'ov_size_80ml'),

    ('vo_var_mask_001_80_size', 'var_mask_001_80', 'ov_size_80ml'),
    ('vo_var_mask_001_100_size', 'var_mask_001_100', 'ov_size_100ml'),
    ('vo_var_mask_002_30_size', 'var_mask_002_30', 'ov_size_30ml'),
    ('vo_var_mask_002_50_size', 'var_mask_002_50', 'ov_size_50ml'),
    ('vo_var_mask_003_30_size', 'var_mask_003_30', 'ov_size_30ml'),
    ('vo_var_mask_003_50_size', 'var_mask_003_50', 'ov_size_50ml'),
    ('vo_var_mask_004_80_size', 'var_mask_004_80', 'ov_size_80ml'),
    ('vo_var_mask_004_100_size', 'var_mask_004_100', 'ov_size_100ml')
ON CONFLICT (id) DO NOTHING;

INSERT INTO variant_options (id, variant_id, option_value_id)
VALUES
    ('vo_var_serum_001_finish', 'var_serum_001_30', 'ov_finish_watery'),
    ('vo_var_serum_002_finish', 'var_serum_002_30', 'ov_finish_watery'),
    ('vo_var_serum_003_finish', 'var_serum_003_30', 'ov_finish_gel'),
    ('vo_var_serum_004_finish', 'var_serum_004_30', 'ov_finish_watery'),
    ('vo_var_moist_001_finish', 'var_moist_001_50', 'ov_finish_cream'),
    ('vo_var_moist_002_finish', 'var_moist_002_50', 'ov_finish_gel'),
    ('vo_var_moist_003_finish', 'var_moist_003_50', 'ov_finish_cream'),
    ('vo_var_moist_004_finish', 'var_moist_004_50', 'ov_finish_gel'),
    ('vo_var_sun_001_version', 'var_sun_001_50', 'ov_version_standard'),
    ('vo_var_sun_004_version', 'var_sun_004_50', 'ov_version_sensitive')
ON CONFLICT (id) DO NOTHING;

-- =========================
-- 5) MEDIA (1 primary per product)
-- =========================

INSERT INTO media (id, created_at, product_id, variant_id, url, public_id, type, is_primary)
VALUES
    ('media_prod_cleanser_001', NOW(), 'prod_cleanser_001', NULL, 'https://example.com/images/aqua-calm-hydrating-cleanser.jpg', 'seed/aqua-calm-hydrating-cleanser', 'IMAGE', TRUE),
    ('media_prod_cleanser_002', NOW(), 'prod_cleanser_002', NULL, 'https://example.com/images/pure-foam-acne-cleanser.jpg', 'seed/pure-foam-acne-cleanser', 'IMAGE', TRUE),
    ('media_prod_cleanser_003', NOW(), 'prod_cleanser_003', NULL, 'https://example.com/images/low-ph-daily-cleanser.jpg', 'seed/low-ph-daily-cleanser', 'IMAGE', TRUE),
    ('media_prod_cleanser_004', NOW(), 'prod_cleanser_004', NULL, 'https://example.com/images/green-tea-deep-cleanser.jpg', 'seed/green-tea-deep-cleanser', 'IMAGE', TRUE),
    ('media_prod_toner_001', NOW(), 'prod_toner_001', NULL, 'https://example.com/images/aqua-balance-hydrating-toner.jpg', 'seed/aqua-balance-hydrating-toner', 'IMAGE', TRUE),
    ('media_prod_toner_002', NOW(), 'prod_toner_002', NULL, 'https://example.com/images/bha-clarifying-toner.jpg', 'seed/bha-clarifying-toner', 'IMAGE', TRUE),
    ('media_prod_toner_003', NOW(), 'prod_toner_003', NULL, 'https://example.com/images/centella-soothing-toner.jpg', 'seed/centella-soothing-toner', 'IMAGE', TRUE),
    ('media_prod_toner_004', NOW(), 'prod_toner_004', NULL, 'https://example.com/images/rice-brightening-toner.jpg', 'seed/rice-brightening-toner', 'IMAGE', TRUE),
    ('media_prod_serum_001', NOW(), 'prod_serum_001', NULL, 'https://example.com/images/niacinamide-10-repair-serum.jpg', 'seed/niacinamide-10-repair-serum', 'IMAGE', TRUE),
    ('media_prod_serum_002', NOW(), 'prod_serum_002', NULL, 'https://example.com/images/hyaluronic-hydra-serum.jpg', 'seed/hyaluronic-hydra-serum', 'IMAGE', TRUE),
    ('media_prod_serum_003', NOW(), 'prod_serum_003', NULL, 'https://example.com/images/retinol-night-renewal-serum.jpg', 'seed/retinol-night-renewal-serum', 'IMAGE', TRUE),
    ('media_prod_serum_004', NOW(), 'prod_serum_004', NULL, 'https://example.com/images/vitamin-c-glow-serum.jpg', 'seed/vitamin-c-glow-serum', 'IMAGE', TRUE),
    ('media_prod_moist_001', NOW(), 'prod_moist_001', NULL, 'https://example.com/images/ceramide-barrier-cream.jpg', 'seed/ceramide-barrier-cream', 'IMAGE', TRUE),
    ('media_prod_moist_002', NOW(), 'prod_moist_002', NULL, 'https://example.com/images/oil-free-aqua-gel.jpg', 'seed/oil-free-aqua-gel', 'IMAGE', TRUE),
    ('media_prod_moist_003', NOW(), 'prod_moist_003', NULL, 'https://example.com/images/panthenol-repair-cream.jpg', 'seed/panthenol-repair-cream', 'IMAGE', TRUE),
    ('media_prod_moist_004', NOW(), 'prod_moist_004', NULL, 'https://example.com/images/cica-soothing-gel-cream.jpg', 'seed/cica-soothing-gel-cream', 'IMAGE', TRUE),
    ('media_prod_sun_001', NOW(), 'prod_sun_001', NULL, 'https://example.com/images/uv-shield-daily-sunscreen.jpg', 'seed/uv-shield-daily-sunscreen', 'IMAGE', TRUE),
    ('media_prod_sun_002', NOW(), 'prod_sun_002', NULL, 'https://example.com/images/matte-air-sunscreen.jpg', 'seed/matte-air-sunscreen', 'IMAGE', TRUE),
    ('media_prod_sun_003', NOW(), 'prod_sun_003', NULL, 'https://example.com/images/hydra-dew-sunscreen.jpg', 'seed/hydra-dew-sunscreen', 'IMAGE', TRUE),
    ('media_prod_sun_004', NOW(), 'prod_sun_004', NULL, 'https://example.com/images/sensitive-mineral-sunscreen.jpg', 'seed/sensitive-mineral-sunscreen', 'IMAGE', TRUE),
    ('media_prod_mask_001', NOW(), 'prod_mask_001', NULL, 'https://example.com/images/clay-pore-reset-mask.jpg', 'seed/clay-pore-reset-mask', 'IMAGE', TRUE),
    ('media_prod_mask_002', NOW(), 'prod_mask_002', NULL, 'https://example.com/images/hydra-sheet-mask.jpg', 'seed/hydra-sheet-mask', 'IMAGE', TRUE),
    ('media_prod_mask_003', NOW(), 'prod_mask_003', NULL, 'https://example.com/images/cica-recovery-mask.jpg', 'seed/cica-recovery-mask', 'IMAGE', TRUE),
    ('media_prod_mask_004', NOW(), 'prod_mask_004', NULL, 'https://example.com/images/bright-rice-sleep-mask.jpg', 'seed/bright-rice-sleep-mask', 'IMAGE', TRUE)
ON CONFLICT (id) DO NOTHING;

COMMIT;
