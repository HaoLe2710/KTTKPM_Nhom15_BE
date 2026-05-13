-- Reset and seed catalog data from FE seed set (2026-05-11)
-- This migration is intended for shared dev/testing environments so that
-- every teammate pulling latest code gets a consistent e-commerce catalog dataset.

-- 1) Clear dependent data first to satisfy FK constraints
DELETE FROM search_click_logs;
DELETE FROM search_projection_failures;
DELETE FROM search_projection_tasks;
DELETE FROM search_product_boost_overrides;
DELETE FROM search_boost_rules;
DELETE FROM product_search_facet_values;
DELETE FROM product_search_skus;
DELETE FROM product_search_documents;

DELETE FROM cart_items;
DELETE FROM order_items;
DELETE FROM reviews;
DELETE FROM media;
DELETE FROM variant_options;
DELETE FROM product_ingredients;
DELETE FROM product_skin_types;
DELETE FROM product_concerns;
DELETE FROM product_tags;
DELETE FROM variants;
DELETE FROM products;
DELETE FROM option_values;
DELETE FROM options;
DELETE FROM product_types;
DELETE FROM brands;
DELETE FROM ingredients;
DELETE FROM skin_types;
DELETE FROM concerns;
DELETE FROM tags;

-- 2) Seed master data
INSERT INTO product_types (id, code, name) VALUES
  ('pt_sua_rua_mat', 'sua-rua-mat', 'Sữa rửa mặt'),
  ('pt_serum', 'serum', 'Serum'),
  ('pt_kem_duong', 'kem-duong', 'Kem dưỡng'),
  ('pt_chong_nang', 'chong-nang', 'Kem chống nắng'),
  ('pt_toner', 'toner', 'Toner');

INSERT INTO brands (id, code, name, slug, description, logo_url, is_active, created_at, updated_at) VALUES
  ('br_cerave', 'cerave', 'CeraVe', 'cerave', 'Dermatology skincare brand', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('br_larocheposay', 'larocheposay', 'La Roche-Posay', 'la-roche-posay', 'Dermatology skincare brand', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('br_bioderma', 'bioderma', 'Bioderma', 'bioderma', 'French dermo-cosmetic brand', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('br_eucerin', 'eucerin', 'Eucerin', 'eucerin', 'Clinical skincare brand', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('br_vichy', 'vichy', 'Vichy', 'vichy', 'Mineral-based skincare brand', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ingredients (id, code, name, normalized_name, inci_name, description, is_active, created_at, updated_at) VALUES
  ('ing_niacinamide', 'niacinamide', 'Niacinamide', 'niacinamide', 'Niacinamide', 'Hỗ trợ làm sáng da, giảm dầu', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ing_hyaluronic_acid', 'hyaluronic-acid', 'Hyaluronic Acid', 'hyaluronic acid', 'Sodium Hyaluronate', 'Cấp nước và giữ ẩm', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ing_ceramide', 'ceramide', 'Ceramide', 'ceramide', 'Ceramide NP', 'Phục hồi hàng rào bảo vệ da', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ing_salicylic_acid', 'salicylic-acid', 'Salicylic Acid', 'salicylic acid', 'Salicylic Acid', 'Làm sạch lỗ chân lông', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ing_panthenol', 'panthenol', 'Panthenol (B5)', 'panthenol', 'Panthenol', 'Làm dịu và phục hồi da', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ing_glycerin', 'glycerin', 'Glycerin', 'glycerin', 'Glycerin', 'Dưỡng ẩm nền', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ing_madecassoside', 'madecassoside', 'Madecassoside', 'madecassoside', 'Madecassoside', 'Làm dịu da nhạy cảm', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ing_zinc_pca', 'zinc-pca', 'Zinc PCA', 'zinc pca', 'Zinc PCA', 'Giảm bã nhờn', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO skin_types (id, code, name, description, is_active, created_at, updated_at) VALUES
  ('st_da_dau', 'da-dau', 'Da dầu', 'Da tiết nhiều bã nhờn', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('st_da_kho', 'da-kho', 'Da khô', 'Da thiếu ẩm', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('st_da_hon_hop', 'da-hon-hop', 'Da hỗn hợp', 'Da kết hợp dầu và khô', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('st_da_nhay_cam', 'da-nhay-cam', 'Da nhạy cảm', 'Da dễ kích ứng', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('st_da_thuong', 'da-thuong', 'Da thường', 'Da ổn định', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO concerns (id, code, name, description, is_active, created_at, updated_at) VALUES
  ('co_mun', 'mun', 'Mụn', 'Da dễ nổi mụn', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('co_tham_sam', 'tham-sam', 'Thâm sạm', 'Da không đều màu', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('co_lao_hoa', 'lao-hoa', 'Lão hóa', 'Nếp nhăn và đàn hồi', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('co_mat_nuoc', 'mat-nuoc', 'Mất nước', 'Da thiếu nước', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('co_kich_ung', 'kich-ung', 'Kích ứng', 'Da đang tổn thương', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tags (id, code, name, description, is_active, created_at, updated_at) VALUES
  ('tag_ban_chay', 'ban-chay', 'Bán chạy', 'Top doanh số', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('tag_san_pham_moi', 'san-pham-moi', 'Sản phẩm mới', 'Mới ra mắt', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('tag_khong_con', 'khong-con', 'Không cồn', 'Alcohol-free', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('tag_khong_huong_lieu', 'khong-huong-lieu', 'Không hương liệu', 'Fragrance-free', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('tag_thuan_chay', 'thuan-chay', 'Thuần chay', 'Vegan-friendly', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO options (id, code, name, is_active) VALUES
  ('opt_container_type', 'container_type', 'Dạng bao bì', TRUE),
  ('opt_volume', 'volume', 'Dung tích', TRUE),
  ('opt_usage_time', 'usage_time', 'Thời điểm sử dụng', TRUE);

INSERT INTO option_values (id, option_id, value, sort_order, is_active) VALUES
  ('ov_container_chai', 'opt_container_type', 'Chai', 0, TRUE),
  ('ov_container_lo', 'opt_container_type', 'Lọ', 1, TRUE),
  ('ov_container_tuyp', 'opt_container_type', 'Tuýp', 2, TRUE),
  ('ov_container_hu', 'opt_container_type', 'Hũ', 3, TRUE),
  ('ov_volume_30ml', 'opt_volume', '30ml', 0, TRUE),
  ('ov_volume_50ml', 'opt_volume', '50ml', 1, TRUE),
  ('ov_volume_100ml', 'opt_volume', '100ml', 2, TRUE),
  ('ov_volume_150ml', 'opt_volume', '150ml', 3, TRUE),
  ('ov_volume_40ml', 'opt_volume', '40ml', 4, TRUE),
  ('ov_volume_200ml', 'opt_volume', '200ml', 5, TRUE),
  ('ov_volume_236ml', 'opt_volume', '236ml', 6, TRUE),
  ('ov_volume_473ml', 'opt_volume', '473ml', 7, TRUE),
  ('ov_usage_sang', 'opt_usage_time', 'Sáng', 0, TRUE),
  ('ov_usage_toi', 'opt_usage_time', 'Tối', 1, TRUE),
  ('ov_usage_sang_toi', 'opt_usage_time', 'Sáng/Tối', 2, TRUE);

-- 3) Seed products
INSERT INTO products (id, created_at, updated_at, type_id, name, slug, description_md, is_customizable, is_active, brand_id, short_description) VALUES
  ('prd_hydrating_cleanser_cerave', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'pt_sua_rua_mat', 'Hydrating Facial Cleanser', 'hydating-facial-cleanser-cerave',
   'Sản phẩm làm sạch phù hợp da thường đến da khô, chứa Ceramide và Hyaluronic Acid.', FALSE, TRUE, 'br_cerave',
   'Sữa rửa mặt dịu nhẹ giúp làm sạch và duy trì độ ẩm da.'),
  ('prd_pure_niacinamide_lrp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'pt_serum', 'Pure Niacinamide 10 Serum', 'pure-niacinamide-10-serum-la-roche-posay',
   'Công thức niacinamide nồng độ cao, hỗ trợ cải thiện đốm nâu và bề mặt da.', FALSE, TRUE, 'br_larocheposay',
   'Serum niacinamide hỗ trợ giảm thâm và đều màu da.'),
  ('prd_cicaplast_b5_lrp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'pt_kem_duong', 'Cicaplast Baume B5+', 'cicaplast-baume-b5-plus-la-roche-posay',
   'Phù hợp da nhạy cảm, hỗ trợ phục hồi hàng rào bảo vệ da với Panthenol và Madecassoside.', FALSE, TRUE, 'br_larocheposay',
   'Kem dưỡng phục hồi và làm dịu da kích ứng.'),
  ('prd_sun_oil_control_eucerin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'pt_chong_nang', 'Sun Oil Control Dry Touch SPF50+', 'sun-oil-control-dry-touch-spf50-eucerin',
   'Bảo vệ da phổ rộng UVA/UVB, bề mặt khô thoáng, phù hợp sử dụng hằng ngày.', FALSE, TRUE, 'br_eucerin',
   'Kem chống nắng kiểm soát dầu cho da dầu mụn.'),
  ('prd_sebium_lotion_bioderma', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'pt_toner', 'Sébium Lotion', 'sebium-lotion-bioderma',
   'Dành cho da hỗn hợp, da dầu, hỗ trợ làm mịn bề mặt da.', FALSE, TRUE, 'br_bioderma',
   'Nước cân bằng hỗ trợ giảm dầu và cân bằng pH da.');

INSERT INTO variants (id, created_at, updated_at, product_id, sku, price, stock_quantity, is_active) VALUES
  ('var_cera_hydr_236', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'prd_hydrating_cleanser_cerave', 'CERA-SUAR-HYDR-VOL-001', 289000, 120, TRUE),
  ('var_cera_hydr_473', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'prd_hydrating_cleanser_cerave', 'CERA-SUAR-HYDR-VOL-002', 389000, 90, TRUE),
  ('var_lrp_niac_30', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'prd_pure_niacinamide_lrp', 'LARO-SERU-PURE-VOL-001', 1290000, 70, TRUE),
  ('var_lrp_cica_40', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'prd_cicaplast_b5_lrp', 'LARO-KEMD-CICA-VOL-001', 429000, 85, TRUE),
  ('var_lrp_cica_100', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'prd_cicaplast_b5_lrp', 'LARO-KEMD-CICA-VOL-002', 559000, 60, TRUE),
  ('var_euce_sun_50', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'prd_sun_oil_control_eucerin', 'EUCE-CHON-SUNO-VOL-001', 549000, 95, TRUE),
  ('var_biod_sebi_200', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'prd_sebium_lotion_bioderma', 'BIOD-TONE-SEBI-VOL-001', 459000, 65, TRUE);

INSERT INTO variant_options (id, variant_id, option_value_id) VALUES
  ('vo_cera_236', 'var_cera_hydr_236', 'ov_volume_236ml'),
  ('vo_cera_473', 'var_cera_hydr_473', 'ov_volume_473ml'),
  ('vo_lrp_niac_30', 'var_lrp_niac_30', 'ov_volume_30ml'),
  ('vo_lrp_cica_40', 'var_lrp_cica_40', 'ov_volume_40ml'),
  ('vo_lrp_cica_100', 'var_lrp_cica_100', 'ov_volume_100ml'),
  ('vo_euce_50', 'var_euce_sun_50', 'ov_volume_50ml'),
  ('vo_biod_200', 'var_biod_sebi_200', 'ov_volume_200ml');

-- 4) Seed product semantic relations
INSERT INTO product_ingredients (id, product_id, ingredient_id, concentration_label, is_highlighted, sort_order, created_at, updated_at) VALUES
  ('pi_01', 'prd_hydrating_cleanser_cerave', 'ing_ceramide', NULL, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('pi_02', 'prd_hydrating_cleanser_cerave', 'ing_hyaluronic_acid', NULL, TRUE, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('pi_03', 'prd_hydrating_cleanser_cerave', 'ing_glycerin', NULL, FALSE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('pi_04', 'prd_pure_niacinamide_lrp', 'ing_niacinamide', NULL, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('pi_05', 'prd_pure_niacinamide_lrp', 'ing_hyaluronic_acid', NULL, FALSE, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('pi_06', 'prd_cicaplast_b5_lrp', 'ing_panthenol', NULL, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('pi_07', 'prd_cicaplast_b5_lrp', 'ing_madecassoside', NULL, TRUE, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('pi_08', 'prd_cicaplast_b5_lrp', 'ing_glycerin', NULL, FALSE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('pi_09', 'prd_sun_oil_control_eucerin', 'ing_niacinamide', NULL, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('pi_10', 'prd_sebium_lotion_bioderma', 'ing_zinc_pca', NULL, TRUE, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('pi_11', 'prd_sebium_lotion_bioderma', 'ing_glycerin', NULL, FALSE, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO product_skin_types (id, product_id, skin_type_id, created_at, updated_at) VALUES
  ('ps_01', 'prd_hydrating_cleanser_cerave', 'st_da_kho', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ps_02', 'prd_hydrating_cleanser_cerave', 'st_da_thuong', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ps_03', 'prd_hydrating_cleanser_cerave', 'st_da_nhay_cam', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ps_04', 'prd_pure_niacinamide_lrp', 'st_da_dau', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ps_05', 'prd_pure_niacinamide_lrp', 'st_da_hon_hop', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ps_06', 'prd_pure_niacinamide_lrp', 'st_da_nhay_cam', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ps_07', 'prd_cicaplast_b5_lrp', 'st_da_nhay_cam', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ps_08', 'prd_cicaplast_b5_lrp', 'st_da_kho', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ps_09', 'prd_cicaplast_b5_lrp', 'st_da_thuong', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ps_10', 'prd_sun_oil_control_eucerin', 'st_da_dau', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ps_11', 'prd_sun_oil_control_eucerin', 'st_da_hon_hop', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ps_12', 'prd_sebium_lotion_bioderma', 'st_da_dau', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ps_13', 'prd_sebium_lotion_bioderma', 'st_da_hon_hop', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO product_concerns (id, product_id, concern_id, created_at, updated_at) VALUES
  ('pc_01', 'prd_hydrating_cleanser_cerave', 'co_mat_nuoc', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('pc_02', 'prd_hydrating_cleanser_cerave', 'co_kich_ung', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('pc_03', 'prd_pure_niacinamide_lrp', 'co_tham_sam', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('pc_04', 'prd_pure_niacinamide_lrp', 'co_lao_hoa', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('pc_05', 'prd_cicaplast_b5_lrp', 'co_kich_ung', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('pc_06', 'prd_cicaplast_b5_lrp', 'co_mat_nuoc', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('pc_07', 'prd_sun_oil_control_eucerin', 'co_mun', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('pc_08', 'prd_sun_oil_control_eucerin', 'co_tham_sam', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('pc_09', 'prd_sebium_lotion_bioderma', 'co_mun', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO product_tags (id, product_id, tag_id, created_at, updated_at) VALUES
  ('ptag_01', 'prd_hydrating_cleanser_cerave', 'tag_ban_chay', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ptag_02', 'prd_hydrating_cleanser_cerave', 'tag_khong_huong_lieu', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ptag_03', 'prd_pure_niacinamide_lrp', 'tag_ban_chay', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ptag_04', 'prd_pure_niacinamide_lrp', 'tag_khong_con', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ptag_05', 'prd_cicaplast_b5_lrp', 'tag_ban_chay', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ptag_06', 'prd_cicaplast_b5_lrp', 'tag_khong_huong_lieu', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ptag_07', 'prd_sun_oil_control_eucerin', 'tag_ban_chay', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ptag_08', 'prd_sun_oil_control_eucerin', 'tag_san_pham_moi', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ptag_09', 'prd_sebium_lotion_bioderma', 'tag_khong_con', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

