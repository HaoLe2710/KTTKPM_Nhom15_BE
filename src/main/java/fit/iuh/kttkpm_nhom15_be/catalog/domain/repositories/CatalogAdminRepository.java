package fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.CatalogHealthResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.OptionListItemResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.OptionValueListItemResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.ProductDetailResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.ProductListItemResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.SearchStatusResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.SemanticMasterListItemResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.SemanticReferenceResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.SemanticMasterKind;
import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface CatalogAdminRepository {

  Page<ProductListItemResponse> findProducts(String query, String typeId, Boolean active, AdminPageRequest pageRequest);

  Optional<ProductDetailResponse> findProductDetail(String productId);

  Optional<SearchStatusResponse> findSearchStatus(String productId);

  boolean existsProduct(String productId);

  boolean existsProductType(String typeId);

  boolean existsProductSlugIgnoreCase(String slug, String excludeProductId);

  String insertProduct(String typeId,
                       String name,
                       String slug,
                       String descriptionMd,
                       String shortDescription,
                       String brandId,
                       boolean customizable,
                       boolean active);

  void updateProduct(String productId,
                     String typeId,
                     String name,
                     String slug,
                     String descriptionMd,
                     String shortDescription,
                     String brandId,
                     boolean customizable,
                     boolean active);

  void updateProductActive(String productId, boolean active);

  long countActiveVariants(String productId);

  boolean existsVariant(String variantId);

  String findProductIdByVariantId(String variantId);

  boolean existsNormalizedSku(String normalizedSku, String excludeVariantId);

  String insertVariant(String productId, String sku, BigDecimal price, int stockQuantity, boolean active);

  void updateVariant(String variantId, String sku, BigDecimal price, int stockQuantity, boolean active);

  void updateVariantStock(String variantId, int stockQuantity);

  void updateVariantActive(String variantId, boolean active);

  void replaceVariantOptions(String variantId, List<String> optionValueIds);

  long countActiveAssignableOptionValues(List<String> optionValueIds);

  boolean existsVariantOptionCombination(String productId, List<String> optionValueIds, String excludeVariantId);

  Page<OptionListItemResponse> findOptions(Boolean active, AdminPageRequest pageRequest);

  Page<OptionValueListItemResponse> findOptionValues(String optionId, Boolean active, AdminPageRequest pageRequest);

  boolean existsOption(String optionId);

  boolean existsOptionCodeIgnoreCase(String code, String excludeOptionId);

  String insertOption(String code, String name, boolean active);

  void updateOption(String optionId, String code, String name, boolean active);

  void updateOptionActive(String optionId, boolean active);

  boolean existsOptionValue(String optionValueId);

  Optional<OptionValueListItemResponse> findOptionValueById(String optionValueId);

  boolean existsOptionValueForOptionIgnoreCase(String optionId, String value, String excludeOptionValueId);

  String insertOptionValue(String optionId, String value, int sortOrder, boolean active);

  void updateOptionValue(String optionValueId, String value, int sortOrder, boolean active);

  void updateOptionValueActive(String optionValueId, boolean active);

  long countActiveVariantAssignmentsForOptionValue(String optionValueId);

  long countActiveVariantAssignmentsForOption(String optionId);

  Page<SemanticMasterListItemResponse> findSemanticMasters(SemanticMasterKind kind, Boolean active, AdminPageRequest pageRequest);

  boolean existsSemanticMaster(SemanticMasterKind kind, String id);

  long countSemanticMasters(SemanticMasterKind kind, List<String> ids);

  boolean existsSemanticMasterCodeIgnoreCase(SemanticMasterKind kind, String code, String excludeId);

  boolean existsBrandSlugIgnoreCase(String slug, String excludeId);

  String insertBrand(String code, String name, String slug, String description, String logoUrl, boolean active);

  void updateBrand(String id, String code, String name, String slug, String description, String logoUrl, boolean active);

  void updateBrandActive(String id, boolean active);

  String insertIngredient(String code, String name, String normalizedName, String inciName, String description, boolean active);

  void updateIngredient(String id, String code, String name, String normalizedName, String inciName, String description, boolean active);

  void updateIngredientActive(String id, boolean active);

  String insertSemanticMaster(SemanticMasterKind kind, String code, String name, String description, boolean active);

  void updateSemanticMaster(SemanticMasterKind kind, String id, String code, String name, String description, boolean active);

  void updateSemanticMasterActive(SemanticMasterKind kind, String id, boolean active);

  void replaceProductSemanticReferences(String productId, SemanticMasterKind kind, List<String> referenceIds);

  List<SemanticReferenceResponse> findProductSemanticReferences(String productId, SemanticMasterKind kind);

  List<String> findProductIdsBySemanticMaster(String masterId, SemanticMasterKind kind);

  CatalogHealthResponse findCatalogHealth();
}
