package fit.iuh.kttkpm_nhom15_be.catalog.application.services;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.BrandWriteRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.CatalogHealthResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.CreatedResourceResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.IngredientWriteRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.OptionListItemResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.OptionValueListItemResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.OptionValueWriteRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.OptionWriteRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.ProductCreateRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.ProductDetailResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.ProductListItemResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.ProductUpdateRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.SearchStatusResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.SemanticMasterListItemResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.SemanticMasterWriteRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.ToggleActiveRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.VariantCreateRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.VariantOptionAssignmentRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.VariantStockPatchRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.VariantSummaryResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.VariantUpdateRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.events.CatalogProductChangedEvent;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.SemanticMasterKind;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.CatalogAdminRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.presentation.requests.admin.BrandMultipartWriteRequest;
import fit.iuh.kttkpm_nhom15_be.search.application.support.SearchNormalizer;
import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest;
import fit.iuh.kttkpm_nhom15_be.shared.application.cache.CacheNames;
import fit.iuh.kttkpm_nhom15_be.shared.application.storage.FileStoragePort;
import fit.iuh.kttkpm_nhom15_be.shared.application.storage.StoredFile;
import fit.iuh.kttkpm_nhom15_be.shared.application.storage.UploadFileCommand;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiConflictException;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiNotFoundException;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.audit.AdminAuditService;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class CatalogAdminService {

  private final CatalogAdminRepository catalogAdminRepository;
  private final CatalogMasterDataCacheService catalogMasterDataCacheService;
  private final ApplicationEventPublisher eventPublisher;
  private final AdminAuditService adminAuditService;
  private final FileStoragePort fileStoragePort;

  public Page<ProductListItemResponse> getProducts(String query, String typeId, Boolean active, AdminPageRequest pageRequest) {
    return catalogAdminRepository.findProducts(query, typeId, active, pageRequest);
  }

  public ProductDetailResponse getProductDetail(String productId) {
    return catalogAdminRepository.findProductDetail(productId)
      .orElseThrow(() -> new ApiNotFoundException("Product not found"));
  }

  public SearchStatusResponse getSearchStatus(String productId) {
    if (!catalogAdminRepository.existsProduct(productId)) {
      throw new ApiNotFoundException("Product not found");
    }
    return catalogAdminRepository.findSearchStatus(productId)
      .orElse(new SearchStatusResponse(false, null, null, 0, "MISSING"));
  }

  @Transactional
  public CreatedResourceResponse createProduct(ProductCreateRequest request) {
    validateProductType(request.typeId());
    String resolvedSlug = resolveSlug(request.slug(), request.name());
    validateSlugUniqueness(resolvedSlug, null);

    String brandId = normalizeOptionalId(request.brandId());
    validateOptionalSemanticMaster(SemanticMasterKind.BRAND, brandId, "Brand does not exist");
    List<String> ingredientIds = normalizeSemanticIds(request.ingredientIds(), "ingredientIds");
    List<String> skinTypeIds = normalizeSemanticIds(request.skinTypeIds(), "skinTypeIds");
    List<String> concernIds = normalizeSemanticIds(request.concernIds(), "concernIds");
    List<String> tagIds = normalizeSemanticIds(request.tagIds(), "tagIds");
    validateSemanticMasterIds(SemanticMasterKind.INGREDIENT, ingredientIds, "One or more ingredients do not exist");
    validateSemanticMasterIds(SemanticMasterKind.SKIN_TYPE, skinTypeIds, "One or more skin types do not exist");
    validateSemanticMasterIds(SemanticMasterKind.CONCERN, concernIds, "One or more concerns do not exist");
    validateSemanticMasterIds(SemanticMasterKind.TAG, tagIds, "One or more tags do not exist");

    Set<String> requestSkus = new HashSet<>();
    Set<String> requestCombinations = new HashSet<>();
    for (var variant : request.variants()) {
      validateVariantPayload(null, variant.sku(), variant.price(), variant.stockQuantity(), variant.options(), requestSkus, requestCombinations, null);
    }

    String productId = catalogAdminRepository.insertProduct(
      request.typeId().trim(),
      request.name().trim(),
      resolvedSlug,
      trimToNull(request.descriptionMd()),
      trimToNull(request.shortDescription()),
      brandId,
      request.isCustomizable(),
      request.isActive()
    );
    replaceProductSemanticReferences(productId, ingredientIds, skinTypeIds, concernIds, tagIds);

    for (var variant : request.variants()) {
      String variantId = catalogAdminRepository.insertVariant(
        productId,
        variant.sku().trim(),
        variant.price(),
        variant.stockQuantity(),
        variant.isActive()
      );
      catalogAdminRepository.replaceVariantOptions(variantId, extractOptionValueIds(variant.options()));
    }

    publishCatalogChanged(productId, "ADMIN_PRODUCT_CREATED");
    adminAuditService.log("PRODUCT_CREATED", "PRODUCT", productId, "{\"slug\":\"" + resolvedSlug + "\"}");
    return new CreatedResourceResponse(productId);
  }

  @Transactional
  public ProductDetailResponse updateProduct(String productId, ProductUpdateRequest request) {
    ProductDetailResponse existing = getProductDetail(productId);
    validateProductType(request.typeId());
    String resolvedSlug = request.slug() == null || request.slug().isBlank()
      ? existing.slug()
      : resolveSlug(request.slug(), request.name());
    validateSlugUniqueness(resolvedSlug, productId);

    String brandId = normalizeOptionalId(request.brandId());
    validateOptionalSemanticMaster(SemanticMasterKind.BRAND, brandId, "Brand does not exist");
    List<String> ingredientIds = normalizeSemanticIds(request.ingredientIds(), "ingredientIds");
    List<String> skinTypeIds = normalizeSemanticIds(request.skinTypeIds(), "skinTypeIds");
    List<String> concernIds = normalizeSemanticIds(request.concernIds(), "concernIds");
    List<String> tagIds = normalizeSemanticIds(request.tagIds(), "tagIds");
    validateSemanticMasterIds(SemanticMasterKind.INGREDIENT, ingredientIds, "One or more ingredients do not exist");
    validateSemanticMasterIds(SemanticMasterKind.SKIN_TYPE, skinTypeIds, "One or more skin types do not exist");
    validateSemanticMasterIds(SemanticMasterKind.CONCERN, concernIds, "One or more concerns do not exist");
    validateSemanticMasterIds(SemanticMasterKind.TAG, tagIds, "One or more tags do not exist");
    if (Boolean.TRUE.equals(request.isActive()) && catalogAdminRepository.countActiveVariants(productId) == 0) {
      throw new ApiValidationException("Active products must have at least one active variant");
    }

    catalogAdminRepository.updateProduct(
      productId,
      request.typeId().trim(),
      request.name().trim(),
      resolvedSlug,
      trimToNull(request.descriptionMd()),
      trimToNull(request.shortDescription()),
      brandId,
      request.isCustomizable(),
      request.isActive()
    );
    replaceProductSemanticReferences(productId, ingredientIds, skinTypeIds, concernIds, tagIds);
    publishCatalogChanged(productId, "ADMIN_PRODUCT_UPDATED");
    adminAuditService.log("PRODUCT_UPDATED", "PRODUCT", productId, "{\"slug\":\"" + resolvedSlug + "\"}");
    return getProductDetail(productId);
  }

  @Transactional
  public void toggleProductActive(String productId, ToggleActiveRequest request) {
    ensureProductExists(productId);
    if (Boolean.TRUE.equals(request.active()) && catalogAdminRepository.countActiveVariants(productId) == 0) {
      throw new ApiValidationException("Active products must have at least one active variant");
    }
    catalogAdminRepository.updateProductActive(productId, request.active());
    publishCatalogChanged(productId, "ADMIN_PRODUCT_ACTIVE_TOGGLED");
    adminAuditService.log("PRODUCT_ACTIVE_TOGGLED", "PRODUCT", productId, "{\"active\":" + request.active() + "}");
  }

  @Transactional
  public VariantSummaryResponse createVariant(VariantCreateRequest request) {
    ensureProductExists(request.productId());
    validateVariantPayload(request.productId(), request.sku(), request.price(), request.stockQuantity(), request.options(), new HashSet<>(),
      new HashSet<>(), null);

    String variantId = catalogAdminRepository.insertVariant(
      request.productId(),
      request.sku().trim(),
      request.price(),
      request.stockQuantity(),
      request.isActive()
    );
    catalogAdminRepository.replaceVariantOptions(variantId, extractOptionValueIds(request.options()));
    publishCatalogChanged(request.productId(), "ADMIN_VARIANT_CREATED");
    adminAuditService.log("VARIANT_CREATED", "VARIANT", variantId, "{\"productId\":\"" + request.productId() + "\"}");
    return findVariantSummary(request.productId(), variantId);
  }

  @Transactional
  public VariantSummaryResponse updateVariant(String variantId, VariantUpdateRequest request) {
    String productId = ensureVariantExists(variantId);
    validateVariantPayload(productId, request.sku(), request.price(), request.stockQuantity(), request.options(), new HashSet<>(),
      new HashSet<>(), variantId);
    if (Boolean.FALSE.equals(request.isActive()) && catalogAdminRepository.countActiveVariants(productId) <= 1) {
      ProductDetailResponse product = getProductDetail(productId);
      if (product.active()) {
        throw new ApiValidationException("Cannot deactivate the last active variant of an active product");
      }
    }

    catalogAdminRepository.updateVariant(variantId, request.sku().trim(), request.price(), request.stockQuantity(), request.isActive());
    catalogAdminRepository.replaceVariantOptions(variantId, extractOptionValueIds(request.options()));
    publishCatalogChanged(productId, "ADMIN_VARIANT_UPDATED");
    adminAuditService.log("VARIANT_UPDATED", "VARIANT", variantId, "{\"productId\":\"" + productId + "\"}");
    return findVariantSummary(productId, variantId);
  }

  @Transactional
  public void patchVariantStock(String variantId, VariantStockPatchRequest request) {
    String productId = ensureVariantExists(variantId);
    catalogAdminRepository.updateVariantStock(variantId, request.stockQuantity());
    publishCatalogChanged(productId, "ADMIN_VARIANT_STOCK_PATCHED");
    adminAuditService.log("VARIANT_STOCK_PATCHED", "VARIANT", variantId, "{\"stockQuantity\":" + request.stockQuantity() + "}");
  }

  @Transactional
  public void toggleVariantActive(String variantId, ToggleActiveRequest request) {
    String productId = ensureVariantExists(variantId);
    if (Boolean.FALSE.equals(request.active()) && catalogAdminRepository.countActiveVariants(productId) <= 1) {
      ProductDetailResponse product = getProductDetail(productId);
      if (product.active()) {
        throw new ApiValidationException("Cannot deactivate the last active variant of an active product");
      }
    }
    catalogAdminRepository.updateVariantActive(variantId, request.active());
    publishCatalogChanged(productId, "ADMIN_VARIANT_ACTIVE_TOGGLED");
    adminAuditService.log("VARIANT_ACTIVE_TOGGLED", "VARIANT", variantId, "{\"active\":" + request.active() + "}");
  }

  public Page<OptionListItemResponse> getOptions(Boolean active, AdminPageRequest pageRequest) {
    return catalogMasterDataCacheService.getOptions(active, pageRequest).toPage();
  }

  public Page<SemanticMasterListItemResponse> getBrands(Boolean active, AdminPageRequest pageRequest) {
    return catalogMasterDataCacheService.getSemanticMasters(SemanticMasterKind.BRAND, active, pageRequest).toPage();
  }

  public Page<SemanticMasterListItemResponse> getIngredients(Boolean active, AdminPageRequest pageRequest) {
    return catalogMasterDataCacheService.getSemanticMasters(SemanticMasterKind.INGREDIENT, active, pageRequest).toPage();
  }

  public Page<SemanticMasterListItemResponse> getSkinTypes(Boolean active, AdminPageRequest pageRequest) {
    return catalogMasterDataCacheService.getSemanticMasters(SemanticMasterKind.SKIN_TYPE, active, pageRequest).toPage();
  }

  public Page<SemanticMasterListItemResponse> getConcerns(Boolean active, AdminPageRequest pageRequest) {
    return catalogMasterDataCacheService.getSemanticMasters(SemanticMasterKind.CONCERN, active, pageRequest).toPage();
  }

  public Page<SemanticMasterListItemResponse> getTags(Boolean active, AdminPageRequest pageRequest) {
    return catalogMasterDataCacheService.getSemanticMasters(SemanticMasterKind.TAG, active, pageRequest).toPage();
  }

  public Page<OptionValueListItemResponse> getOptionValues(String optionId, Boolean active, AdminPageRequest pageRequest) {
    if (optionId != null && !optionId.isBlank() && !catalogAdminRepository.existsOption(optionId)) {
      throw new ApiNotFoundException("Option not found");
    }
    return catalogMasterDataCacheService.getOptionValues(optionId, active, pageRequest).toPage();
  }

  @Transactional
  @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
  public CreatedResourceResponse createOption(OptionWriteRequest request) {
    validateOptionCode(request.code(), null);
    String optionId = catalogAdminRepository.insertOption(request.code().trim(), request.name().trim(), resolveActive(request.isActive()));
    if (request.values() != null) {
      int index = 0;
      for (OptionValueWriteRequest value : request.values()) {
        validateOptionValue(optionId, value.value(), null);
        catalogAdminRepository.insertOptionValue(optionId, value.value().trim(), resolveSortOrder(value.sortOrder(), index), resolveActive(value.isActive()));
        index++;
      }
    }
    adminAuditService.log("OPTION_CREATED", "OPTION", optionId, "{\"code\":\"" + request.code().trim() + "\"}");
    return new CreatedResourceResponse(optionId);
  }

  @Transactional
  @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
  public void updateOption(String optionId, OptionWriteRequest request) {
    ensureOptionExists(optionId);
    validateOptionCode(request.code(), optionId);
    if (!resolveActive(request.isActive()) && catalogAdminRepository.countActiveVariantAssignmentsForOption(optionId) > 0) {
      throw new ApiValidationException("Cannot deactivate option while it is used by active variants on active products");
    }
    catalogAdminRepository.updateOption(optionId, request.code().trim(), request.name().trim(), resolveActive(request.isActive()));
    adminAuditService.log("OPTION_UPDATED", "OPTION", optionId, "{\"code\":\"" + request.code().trim() + "\"}");
  }

  @Transactional
  @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
  public CreatedResourceResponse createOptionValue(OptionValueWriteRequest request, String optionId) {
    ensureOptionExists(optionId);
    validateOptionValue(optionId, request.value(), null);
    String optionValueId = catalogAdminRepository.insertOptionValue(
      optionId,
      request.value().trim(),
      resolveSortOrder(request.sortOrder(), 0),
      resolveActive(request.isActive())
    );
    adminAuditService.log("OPTION_VALUE_CREATED", "OPTION_VALUE", optionValueId, "{\"optionId\":\"" + optionId + "\"}");
    return new CreatedResourceResponse(optionValueId);
  }

  @Transactional
  @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
  public void updateOptionValue(String optionValueId, OptionValueWriteRequest request) {
    OptionValueListItemResponse existing = catalogAdminRepository.findOptionValueById(optionValueId)
      .orElseThrow(() -> new ApiNotFoundException("Option value not found"));
    validateOptionValue(existing.optionId(), request.value(), optionValueId);
    boolean active = resolveActive(request.isActive());
    int sortOrder = resolveSortOrder(request.sortOrder(), existing.sortOrder());
    boolean changed = !existing.value().equals(request.value().trim())
      || existing.sortOrder() != sortOrder
      || existing.active() != active;
    if (changed && catalogAdminRepository.countActiveVariantAssignmentsForOptionValue(optionValueId) > 0) {
      throw new ApiValidationException("Cannot modify an option value that is used by active variants on active products");
    }
    catalogAdminRepository.updateOptionValue(optionValueId, request.value().trim(), sortOrder, active);
    adminAuditService.log("OPTION_VALUE_UPDATED", "OPTION_VALUE", optionValueId, "{\"optionId\":\"" + existing.optionId() + "\"}");
  }

  @Transactional
  @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
  public void toggleOptionValueActive(String optionValueId, ToggleActiveRequest request) {
    ensureOptionValueExists(optionValueId);
    if (Boolean.FALSE.equals(request.active()) && catalogAdminRepository.countActiveVariantAssignmentsForOptionValue(optionValueId) > 0) {
      throw new ApiValidationException("Cannot deactivate an option value that is used by active variants on active products");
    }
    catalogAdminRepository.updateOptionValueActive(optionValueId, request.active());
    adminAuditService.log("OPTION_VALUE_ACTIVE_TOGGLED", "OPTION_VALUE", optionValueId, "{\"active\":" + request.active() + "}");
  }

  @Transactional
  @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
  public CreatedResourceResponse createBrand(BrandWriteRequest request) {
    validateSemanticMasterCode(SemanticMasterKind.BRAND, request.code(), null, "Brand code already exists");
    String slug = resolveSlug(request.slug(), request.name());
    validateBrandSlug(slug, null);
    String id = catalogAdminRepository.insertBrand(
      request.code().trim(),
      request.name().trim(),
      slug,
      trimToNull(request.description()),
      trimToNull(request.logoUrl()),
      resolveActive(request.isActive())
    );
    adminAuditService.log("BRAND_CREATED", "BRAND", id, "{\"code\":\"" + request.code().trim() + "\"}");
    return new CreatedResourceResponse(id);
  }

  @Transactional
  @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
  public CreatedResourceResponse createBrandWithLogo(BrandMultipartWriteRequest request) {
    String logoUrl = resolveBrandLogoUrl(request.getLogoFile(), request.getLogoUrl());
    return createBrand(new BrandWriteRequest(
      request.getCode(),
      request.getName(),
      request.getSlug(),
      request.getDescription(),
      logoUrl,
      request.getIsActive()
    ));
  }

  @Transactional
  @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
  public void updateBrand(String id, BrandWriteRequest request) {
    ensureSemanticMasterExists(SemanticMasterKind.BRAND, id, "Brand not found");
    validateSemanticMasterCode(SemanticMasterKind.BRAND, request.code(), id, "Brand code already exists");
    String slug = resolveSlug(request.slug(), request.name());
    validateBrandSlug(slug, id);
    catalogAdminRepository.updateBrand(
      id,
      request.code().trim(),
      request.name().trim(),
      slug,
      trimToNull(request.description()),
      trimToNull(request.logoUrl()),
      resolveActive(request.isActive())
    );
    publishSemanticMasterChanged(SemanticMasterKind.BRAND, id, "ADMIN_BRAND_UPDATED");
    adminAuditService.log("BRAND_UPDATED", "BRAND", id, "{\"code\":\"" + request.code().trim() + "\"}");
  }

  @Transactional
  @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
  public void updateBrandWithLogo(String id, BrandMultipartWriteRequest request) {
    String logoUrl = resolveBrandLogoUrl(request.getLogoFile(), request.getLogoUrl());
    updateBrand(id, new BrandWriteRequest(
      request.getCode(),
      request.getName(),
      request.getSlug(),
      request.getDescription(),
      logoUrl,
      request.getIsActive()
    ));
  }

  @Transactional
  @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
  public void toggleBrandActive(String id, ToggleActiveRequest request) {
    ensureSemanticMasterExists(SemanticMasterKind.BRAND, id, "Brand not found");
    catalogAdminRepository.updateBrandActive(id, request.active());
    publishSemanticMasterChanged(SemanticMasterKind.BRAND, id, "ADMIN_BRAND_ACTIVE_TOGGLED");
    adminAuditService.log("BRAND_ACTIVE_TOGGLED", "BRAND", id, "{\"active\":" + request.active() + "}");
  }

  @Transactional
  @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
  public CreatedResourceResponse createIngredient(IngredientWriteRequest request) {
    validateSemanticMasterCode(SemanticMasterKind.INGREDIENT, request.code(), null, "Ingredient code already exists");
    String id = catalogAdminRepository.insertIngredient(
      request.code().trim(),
      request.name().trim(),
      resolveNormalizedText(request.normalizedName(), request.name()),
      trimToNull(request.inciName()),
      trimToNull(request.description()),
      resolveActive(request.isActive())
    );
    adminAuditService.log("INGREDIENT_CREATED", "INGREDIENT", id, "{\"code\":\"" + request.code().trim() + "\"}");
    return new CreatedResourceResponse(id);
  }

  @Transactional
  @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
  public void updateIngredient(String id, IngredientWriteRequest request) {
    ensureSemanticMasterExists(SemanticMasterKind.INGREDIENT, id, "Ingredient not found");
    validateSemanticMasterCode(SemanticMasterKind.INGREDIENT, request.code(), id, "Ingredient code already exists");
    catalogAdminRepository.updateIngredient(
      id,
      request.code().trim(),
      request.name().trim(),
      resolveNormalizedText(request.normalizedName(), request.name()),
      trimToNull(request.inciName()),
      trimToNull(request.description()),
      resolveActive(request.isActive())
    );
    publishSemanticMasterChanged(SemanticMasterKind.INGREDIENT, id, "ADMIN_INGREDIENT_UPDATED");
    adminAuditService.log("INGREDIENT_UPDATED", "INGREDIENT", id, "{\"code\":\"" + request.code().trim() + "\"}");
  }

  @Transactional
  @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
  public void toggleIngredientActive(String id, ToggleActiveRequest request) {
    ensureSemanticMasterExists(SemanticMasterKind.INGREDIENT, id, "Ingredient not found");
    catalogAdminRepository.updateIngredientActive(id, request.active());
    publishSemanticMasterChanged(SemanticMasterKind.INGREDIENT, id, "ADMIN_INGREDIENT_ACTIVE_TOGGLED");
    adminAuditService.log("INGREDIENT_ACTIVE_TOGGLED", "INGREDIENT", id, "{\"active\":" + request.active() + "}");
  }

  @Transactional
  @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
  public CreatedResourceResponse createSkinType(SemanticMasterWriteRequest request) {
    return createSemanticMaster(SemanticMasterKind.SKIN_TYPE, request, "SKIN_TYPE", "Skin type code already exists");
  }

  @Transactional
  @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
  public void updateSkinType(String id, SemanticMasterWriteRequest request) {
    updateSemanticMaster(SemanticMasterKind.SKIN_TYPE, id, request, "SKIN_TYPE", "Skin type not found", "Skin type code already exists");
  }

  @Transactional
  @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
  public void toggleSkinTypeActive(String id, ToggleActiveRequest request) {
    toggleSemanticMasterActive(SemanticMasterKind.SKIN_TYPE, id, request, "SKIN_TYPE", "Skin type not found");
  }

  @Transactional
  @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
  public CreatedResourceResponse createConcern(SemanticMasterWriteRequest request) {
    return createSemanticMaster(SemanticMasterKind.CONCERN, request, "CONCERN", "Concern code already exists");
  }

  @Transactional
  @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
  public void updateConcern(String id, SemanticMasterWriteRequest request) {
    updateSemanticMaster(SemanticMasterKind.CONCERN, id, request, "CONCERN", "Concern not found", "Concern code already exists");
  }

  @Transactional
  @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
  public void toggleConcernActive(String id, ToggleActiveRequest request) {
    toggleSemanticMasterActive(SemanticMasterKind.CONCERN, id, request, "CONCERN", "Concern not found");
  }

  @Transactional
  @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
  public CreatedResourceResponse createTag(SemanticMasterWriteRequest request) {
    return createSemanticMaster(SemanticMasterKind.TAG, request, "TAG", "Tag code already exists");
  }

  @Transactional
  @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
  public void updateTag(String id, SemanticMasterWriteRequest request) {
    updateSemanticMaster(SemanticMasterKind.TAG, id, request, "TAG", "Tag not found", "Tag code already exists");
  }

  @Transactional
  @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
  public void toggleTagActive(String id, ToggleActiveRequest request) {
    toggleSemanticMasterActive(SemanticMasterKind.TAG, id, request, "TAG", "Tag not found");
  }

  public CatalogHealthResponse getCatalogHealth() {
    return catalogAdminRepository.findCatalogHealth();
  }

  private void validateProductType(String typeId) {
    if (!catalogAdminRepository.existsProductType(typeId.trim())) {
      throw new ApiValidationException("Product type does not exist");
    }
  }

  private void validateSlugUniqueness(String slug, String excludeProductId) {
    if (catalogAdminRepository.existsProductSlugIgnoreCase(slug, excludeProductId)) {
      throw new ApiConflictException("Slug already exists");
    }
  }

  private void validateOptionCode(String code, String excludeOptionId) {
    if (catalogAdminRepository.existsOptionCodeIgnoreCase(code.trim(), excludeOptionId)) {
      throw new ApiConflictException("Option code already exists");
    }
  }

  private void validateOptionValue(String optionId, String value, String excludeOptionValueId) {
    if (catalogAdminRepository.existsOptionValueForOptionIgnoreCase(optionId, value.trim(), excludeOptionValueId)) {
      throw new ApiConflictException("Option value already exists for this option");
    }
  }

  private void validateSemanticMasterCode(SemanticMasterKind kind, String code, String excludeId, String message) {
    if (catalogAdminRepository.existsSemanticMasterCodeIgnoreCase(kind, code.trim(), excludeId)) {
      throw new ApiConflictException(message);
    }
  }

  private void validateBrandSlug(String slug, String excludeId) {
    if (catalogAdminRepository.existsBrandSlugIgnoreCase(slug, excludeId)) {
      throw new ApiConflictException("Brand slug already exists");
    }
  }

  private void validateVariantPayload(String productId,
                                      String sku,
                                      BigDecimal price,
                                      Integer stockQuantity,
                                      List<VariantOptionAssignmentRequest> options,
                                      Set<String> requestSkus,
                                      Set<String> requestCombinations,
                                      String excludeVariantId) {
    if (price.compareTo(BigDecimal.ZERO) < 0) {
      throw new ApiValidationException("price must be greater than or equal to 0");
    }
    if (stockQuantity < 0) {
      throw new ApiValidationException("stockQuantity must be greater than or equal to 0");
    }

    String normalizedSku = SearchNormalizer.normalizeSku(sku);
    if (normalizedSku == null || normalizedSku.isBlank()) {
      throw new ApiValidationException("SKU is invalid after normalization");
    }
    if (!requestSkus.add(normalizedSku)) {
      throw new ApiConflictException("Duplicate SKU in request payload");
    }
    if (catalogAdminRepository.existsNormalizedSku(normalizedSku, excludeVariantId)) {
      throw new ApiConflictException("SKU already exists");
    }

    List<String> optionValueIds = extractOptionValueIds(options);
    if (!optionValueIds.isEmpty() && catalogAdminRepository.countActiveAssignableOptionValues(optionValueIds) != optionValueIds.stream().distinct().count()) {
      throw new ApiValidationException("One or more option values are invalid or inactive");
    }
    String combinationKey = String.join("|", optionValueIds);
    if (!requestCombinations.add(combinationKey)) {
      throw new ApiConflictException("Duplicate option combination in request payload");
    }
    if (productId != null && catalogAdminRepository.existsVariantOptionCombination(productId, optionValueIds, excludeVariantId)) {
      throw new ApiConflictException("Variant option combination already exists for this product");
    }
  }

  private void validateOptionalSemanticMaster(SemanticMasterKind kind, String id, String message) {
    if (id != null && !catalogAdminRepository.existsSemanticMaster(kind, id)) {
      throw new ApiValidationException(message);
    }
  }

  private void validateSemanticMasterIds(SemanticMasterKind kind, List<String> ids, String message) {
    if (!ids.isEmpty() && catalogAdminRepository.countSemanticMasters(kind, ids) != ids.size()) {
      throw new ApiValidationException(message);
    }
  }

  private void ensureProductExists(String productId) {
    if (!catalogAdminRepository.existsProduct(productId)) {
      throw new ApiNotFoundException("Product not found");
    }
  }

  private void ensureSemanticMasterExists(SemanticMasterKind kind, String id, String message) {
    if (!catalogAdminRepository.existsSemanticMaster(kind, id)) {
      throw new ApiNotFoundException(message);
    }
  }

  private String ensureVariantExists(String variantId) {
    if (!catalogAdminRepository.existsVariant(variantId)) {
      throw new ApiNotFoundException("Variant not found");
    }
    return catalogAdminRepository.findProductIdByVariantId(variantId);
  }

  private void ensureOptionExists(String optionId) {
    if (!catalogAdminRepository.existsOption(optionId)) {
      throw new ApiNotFoundException("Option not found");
    }
  }

  private void ensureOptionValueExists(String optionValueId) {
    if (!catalogAdminRepository.existsOptionValue(optionValueId)) {
      throw new ApiNotFoundException("Option value not found");
    }
  }

  private VariantSummaryResponse findVariantSummary(String productId, String variantId) {
    return getProductDetail(productId).variants().stream()
      .filter(variant -> variant.id().equals(variantId))
      .findFirst()
      .orElseThrow(() -> new ApiNotFoundException("Variant not found"));
  }

  private void publishCatalogChanged(String productId, String reason) {
    eventPublisher.publishEvent(new CatalogProductChangedEvent(productId, reason, LocalDateTime.now()));
  }

  private void publishSemanticMasterChanged(SemanticMasterKind kind, String masterId, String reason) {
    catalogAdminRepository.findProductIdsBySemanticMaster(masterId, kind)
      .forEach(productId -> publishCatalogChanged(productId, reason));
  }

  private String resolveSlug(String slug, String name) {
    String candidate = slug == null || slug.isBlank() ? name : slug;
    String normalized = SearchNormalizer.normalizeText(candidate);
    if (normalized == null || normalized.isBlank()) {
      throw new ApiValidationException("Unable to derive slug from product name");
    }
    return normalized.replace(' ', '-');
  }

  private String resolveNormalizedText(String explicitNormalized, String fallbackName) {
    String candidate = explicitNormalized == null || explicitNormalized.isBlank() ? fallbackName : explicitNormalized;
    String normalized = SearchNormalizer.normalizeText(candidate);
    return normalized.isBlank() ? null : normalized;
  }

  private List<String> extractOptionValueIds(List<VariantOptionAssignmentRequest> options) {
    if (options == null || options.isEmpty()) {
      return List.of();
    }
    Set<String> duplicates = new HashSet<>();
    for (VariantOptionAssignmentRequest option : options) {
      if (!duplicates.add(option.valueId())) {
        throw new ApiConflictException("Duplicate option value in variant assignment");
      }
    }
    return options.stream().map(VariantOptionAssignmentRequest::valueId).sorted().toList();
  }

  private List<String> normalizeSemanticIds(List<String> ids, String fieldName) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    Set<String> normalized = new LinkedHashSet<>();
    for (String id : ids) {
      String resolved = normalizeOptionalId(id);
      if (resolved == null) {
        throw new ApiValidationException(fieldName + " contains blank values");
      }
      if (!normalized.add(resolved)) {
        throw new ApiConflictException("Duplicate semantic id in " + fieldName);
      }
    }
    return List.copyOf(normalized);
  }

  private void replaceProductSemanticReferences(String productId,
                                                List<String> ingredientIds,
                                                List<String> skinTypeIds,
                                                List<String> concernIds,
                                                List<String> tagIds) {
    catalogAdminRepository.replaceProductSemanticReferences(productId, SemanticMasterKind.INGREDIENT, ingredientIds);
    catalogAdminRepository.replaceProductSemanticReferences(productId, SemanticMasterKind.SKIN_TYPE, skinTypeIds);
    catalogAdminRepository.replaceProductSemanticReferences(productId, SemanticMasterKind.CONCERN, concernIds);
    catalogAdminRepository.replaceProductSemanticReferences(productId, SemanticMasterKind.TAG, tagIds);
  }

  private CreatedResourceResponse createSemanticMaster(SemanticMasterKind kind,
                                                       SemanticMasterWriteRequest request,
                                                       String resourceType,
                                                       String duplicateCodeMessage) {
    validateSemanticMasterCode(kind, request.code(), null, duplicateCodeMessage);
    String id = catalogAdminRepository.insertSemanticMaster(
      kind,
      request.code().trim(),
      request.name().trim(),
      trimToNull(request.description()),
      resolveActive(request.isActive())
    );
    adminAuditService.log(resourceType + "_CREATED", resourceType, id, "{\"code\":\"" + request.code().trim() + "\"}");
    return new CreatedResourceResponse(id);
  }

  private void updateSemanticMaster(SemanticMasterKind kind,
                                    String id,
                                    SemanticMasterWriteRequest request,
                                    String resourceType,
                                    String notFoundMessage,
                                    String duplicateCodeMessage) {
    ensureSemanticMasterExists(kind, id, notFoundMessage);
    validateSemanticMasterCode(kind, request.code(), id, duplicateCodeMessage);
    catalogAdminRepository.updateSemanticMaster(
      kind,
      id,
      request.code().trim(),
      request.name().trim(),
      trimToNull(request.description()),
      resolveActive(request.isActive())
    );
    publishSemanticMasterChanged(kind, id, "ADMIN_" + resourceType + "_UPDATED");
    adminAuditService.log(resourceType + "_UPDATED", resourceType, id, "{\"code\":\"" + request.code().trim() + "\"}");
  }

  private void toggleSemanticMasterActive(SemanticMasterKind kind,
                                          String id,
                                          ToggleActiveRequest request,
                                          String resourceType,
                                          String notFoundMessage) {
    ensureSemanticMasterExists(kind, id, notFoundMessage);
    catalogAdminRepository.updateSemanticMasterActive(kind, id, request.active());
    publishSemanticMasterChanged(kind, id, "ADMIN_" + resourceType + "_ACTIVE_TOGGLED");
    adminAuditService.log(resourceType + "_ACTIVE_TOGGLED", resourceType, id, "{\"active\":" + request.active() + "}");
  }

  private String normalizeOptionalId(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private boolean resolveActive(Boolean active) {
    return active == null || active;
  }

  private int resolveSortOrder(Integer sortOrder, int defaultValue) {
    return sortOrder == null ? defaultValue : sortOrder;
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String resolveBrandLogoUrl(MultipartFile logoFile, String fallbackLogoUrl) {
    if (logoFile == null || logoFile.isEmpty()) {
      return trimToNull(fallbackLogoUrl);
    }
    StoredFile storedFile = uploadBrandLogo(logoFile);
    return storedFile.url();
  }

  private StoredFile uploadBrandLogo(MultipartFile file) {
    try {
      return fileStoragePort.upload(new UploadFileCommand(
        "catalog/brands",
        file.getOriginalFilename(),
        file.getContentType(),
        file.getBytes()
      ));
    } catch (IOException ex) {
      throw new IllegalStateException("Cannot read uploaded brand logo bytes", ex);
    }
  }
}
