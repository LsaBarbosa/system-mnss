package br.com.novaalianca.mnss.localapp.domain.catalog;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.core.catalog.SalesChannel;
import br.com.novaalianca.mnss.core.catalog.UnitType;
import br.com.novaalianca.mnss.localapp.domain.audit.AuditLogRequest;
import br.com.novaalianca.mnss.localapp.domain.audit.AuditService;
import br.com.novaalianca.mnss.sync.SyncEventStatus;
import br.com.novaalianca.mnss.sharedinfra.web.error.BusinessException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {
    private final Optional<CategoryRepository> categoryRepository;
    private final Optional<ProductRepository> productRepository;
    private final Optional<ProductAvailabilityRepository> productAvailabilityRepository;
    private final CatalogSyncEventService syncEventService;
    private final AuditService auditService;
    private final br.com.novaalianca.mnss.localapp.domain.store.StoreInfoProperties storeInfoProperties;

    public CatalogService(
            Optional<CategoryRepository> categoryRepository,
            Optional<ProductRepository> productRepository,
            Optional<ProductAvailabilityRepository> productAvailabilityRepository,
            CatalogSyncEventService syncEventService,
            AuditService auditService,
            br.com.novaalianca.mnss.localapp.domain.store.StoreInfoProperties storeInfoProperties) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productAvailabilityRepository = productAvailabilityRepository;
        this.syncEventService = syncEventService;
        this.auditService = auditService;
        this.storeInfoProperties = storeInfoProperties;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories(CatalogChannel channel) {
        CategoryRepository repository = categoryRepository();
        List<CategoryEntity> categories = channel == CatalogChannel.PDV
                ? repository.findByActiveTrueAndShowOnPdvTrueOrderByDisplayOrderAscNameAsc()
                : repository.findAllByOrderByDisplayOrderAscNameAsc();
        return categories.stream().map(CategoryResponse::from).toList();
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request, UUID actorUserId) {
        CategoryEntity category = new CategoryEntity(request.name());
        category.update(
                null,
                request.description(),
                request.displayOrder(),
                request.imageUrl(),
                request.active(),
                request.showOnline(),
                request.showOnPdv(),
                request.showOnWhatsapp());
        CategoryEntity saved = categoryRepository().save(category);
        syncEventService.recordCategoryEvent("CATEGORY_CREATED", saved);
        auditService.record(new AuditLogRequest(actorUserId, "CATEGORY_CREATED", "Category", saved.getId(), Map.of(), null));
        return CategoryResponse.from(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(UUID id, PatchCategoryRequest request, UUID actorUserId) {
        validateOptionalText(request.name(), "Nome obrigatorio.");
        CategoryEntity category = categoryRepository().findById(id)
                .orElseThrow(() -> notFound("CATEGORY_NOT_FOUND", "Categoria nao encontrada."));
        UUID originalId = category.getId();
        category.update(
                request.name(),
                request.description(),
                request.displayOrder(),
                request.imageUrl(),
                request.active(),
                request.showOnline(),
                request.showOnPdv(),
                request.showOnWhatsapp());
        CategoryEntity saved = categoryRepository().save(category);
        syncEventService.recordCategoryEvent("CATEGORY_UPDATED", saved);
        auditService.record(new AuditLogRequest(actorUserId, "CATEGORY_UPDATED", "Category", saved.getId(), Map.of(), null));
        if (!originalId.equals(saved.getId())) {
            throw new IllegalStateException("Category update changed immutable id.");
        }
        return CategoryResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listProducts(String name, UUID categoryId) {
        List<ProductEntity> products;
        ProductRepository repository = productRepository();
        boolean hasName = name != null && !name.isBlank();
        if (hasName && categoryId != null) {
            products = repository.findByCategoryIdAndNameContainingIgnoreCaseOrderByNameAsc(categoryId, name);
        } else if (hasName) {
            products = repository.findByNameContainingIgnoreCaseOrderByNameAsc(name);
        } else if (categoryId != null) {
            products = repository.findByCategoryIdOrderByNameAsc(categoryId);
        } else {
            products = repository.findAllByOrderByNameAsc();
        }
        return products.stream().map(ProductResponse::from).toList();
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request, UUID actorUserId) {
        CategoryEntity category = categoryRepository().findById(request.categoryId())
                .orElseThrow(() -> notFound("CATEGORY_NOT_FOUND", "Categoria nao encontrada."));
        ProductEntity product = new ProductEntity(
                category,
                request.name(),
                request.price(),
                request.unitType(),
                request.preparationSector());
        product.update(
                null,
                null,
                request.description(),
                null,
                request.promotionalPrice(),
                request.costPrice(),
                request.sku(),
                request.barcode(),
                request.imageUrl(),
                null,
                null,
                request.preparationTimeMinutes(),
                request.active(),
                request.available(),
                request.sellOnPdv(),
                request.sellOnline(),
                request.sellOnWhatsapp(),
                request.stockControlled());
        ProductEntity saved = productRepository().save(product);
        syncEventService.recordProductEvent("PRODUCT_CREATED", saved);
        auditService.record(new AuditLogRequest(actorUserId, "PRODUCT_CREATED", "Product", saved.getId(), Map.of(), null));
        return ProductResponse.from(saved);
    }

    @Transactional
    public ProductResponse updateProduct(UUID id, PatchProductRequest request, UUID actorUserId) {
        validateOptionalText(request.name(), "Nome obrigatorio.");
        ProductEntity product = productRepository().findById(id)
                .orElseThrow(() -> notFound("PRODUCT_NOT_FOUND", "Produto nao encontrado."));
        CategoryEntity category = request.categoryId() == null
                ? null
                : categoryRepository().findById(request.categoryId())
                        .orElseThrow(() -> notFound("CATEGORY_NOT_FOUND", "Categoria nao encontrada."));
        BigDecimal previousPrice = product.getPrice();
        product.update(
                category,
                request.name(),
                request.description(),
                request.price(),
                request.promotionalPrice(),
                request.costPrice(),
                request.sku(),
                request.barcode(),
                request.imageUrl(),
                request.unitType(),
                request.preparationSector(),
                request.preparationTimeMinutes(),
                request.active(),
                request.available(),
                request.sellOnPdv(),
                request.sellOnline(),
                request.sellOnWhatsapp(),
                request.stockControlled());
        ProductEntity saved = productRepository().save(product);
        boolean priceChanged = request.price() != null && previousPrice.compareTo(request.price()) != 0;
        syncEventService.recordProductEvent(priceChanged ? "PRODUCT_PRICE_CHANGED" : "PRODUCT_UPDATED", saved);
        auditService.record(new AuditLogRequest(
                actorUserId,
                priceChanged ? "PRODUCT_PRICE_CHANGED" : "PRODUCT_UPDATED",
                "Product",
                saved.getId(),
                priceChanged ? Map.of("previousPrice", previousPrice.toPlainString(), "newPrice", saved.getPrice().toPlainString()) : Map.of(),
                null));
        return ProductResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ProductResponse findSellableProductByBarcode(String barcode) {
        ProductEntity product = productRepository().findByBarcode(barcode)
                .orElseThrow(() -> notFound("PRODUCT_NOT_FOUND", "Produto nao encontrado."));
        if (!isSellableOnPdv(product)) {
            throw new BusinessException("PRODUCT_NOT_SELLABLE", "Produto nao disponivel para venda.", HttpStatus.NOT_FOUND);
        }
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductAvailabilityResponse updateProductAvailability(
            UUID productId,
            PatchProductAvailabilityRequest request,
            UUID actorUserId) {
        ProductEntity product = productRepository().findById(productId)
                .orElseThrow(() -> notFound("PRODUCT_NOT_FOUND", "Produto nao encontrado."));
        SalesChannel channel = request.channel() == null ? SalesChannel.ALL : request.channel();
        validateAvailabilityReason(request.status(), request.reason());

        Optional<ProductAvailabilityEntity> previousAvailability = latestAvailability(product.getId(), channel);
        Map<String, Object> oldValue = previousAvailability
                .map(this::availabilitySnapshot)
                .orElseGet(LinkedHashMap::new);
        ProductAvailabilityEntity availability = previousAvailability
                .orElseGet(() -> new ProductAvailabilityEntity(product, request.status(), channel));
        availability.update(request.status(), request.availableQuantity(), normalizeReason(request.reason()), actorUserId);
        ProductAvailabilityEntity saved = productAvailabilityRepository().save(availability);

        if (channel == SalesChannel.ALL) {
            product.changeAvailability(request.status() != AvailabilityStatus.UNAVAILABLE);
            productRepository().save(product);
        }

        SyncEventStatus syncStatus = SyncEventStatus.PENDING;
        try {
            syncEventService.recordAvailabilityEvent(availabilityEventType(request.status()), saved);
        } catch (RuntimeException exception) {
            syncStatus = SyncEventStatus.FAILED;
        }

        auditService.record(new AuditLogRequest(
                actorUserId,
                "PRODUCT_AVAILABILITY_CHANGED",
                "ProductAvailability",
                product.getId(),
                Map.of(
                        "oldValue", oldValue,
                        "newValue", availabilitySnapshot(saved)),
                null));
        return ProductAvailabilityResponse.from(saved, syncStatus);
    }

    @Transactional(readOnly = true)
    public List<CategoryProductsResponse> listPdvProducts() {
        return listPdvProducts(null, null);
    }

    @Transactional(readOnly = true)
    public List<CategoryProductsResponse> listPdvProducts(String name, UUID categoryId) {
        return groupedProducts(CatalogChannel.PDV, name, categoryId);
    }

    @Transactional(readOnly = true)
    public List<CategoryProductsResponse> listPublicMenu(String search) {
        return groupedProducts(CatalogChannel.SITE, search, null);
    }

    public br.com.novaalianca.mnss.localapp.domain.store.StoreInfoResponse getStoreInfo() {
        return new br.com.novaalianca.mnss.localapp.domain.store.StoreInfoResponse(
                storeInfoProperties.getName(),
                storeInfoProperties.getAddress(),
                storeInfoProperties.getHours(),
                storeInfoProperties.getPhone(),
                storeInfoProperties.getDescription());
    }

    private BusinessException notFound(String code, String message) {
        return new BusinessException(code, message, HttpStatus.NOT_FOUND);
    }

    private void validateOptionalText(String value, String message) {
        if (value != null && value.isBlank()) {
            throw new BusinessException("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
        }
    }

    private List<CategoryProductsResponse> groupedProducts(CatalogChannel channel, String name, UUID categoryId) {
        List<CategoryEntity> categories = categoryRepository().findAllByOrderByDisplayOrderAscNameAsc().stream()
                .filter(category -> isCategoryVisible(category, channel))
                .filter(category -> categoryId == null || categoryId.equals(category.getId()))
                .toList();
        Map<UUID, List<ProductResponse>> productsByCategory = productRepository().findAllByOrderByNameAsc().stream()
                .filter(product -> isProductVisible(product, channel))
                .filter(product -> matchesProductFilters(product, name, categoryId))
                .collect(Collectors.groupingBy(
                        product -> product.getCategory().getId(),
                        LinkedHashMap::new,
                        Collectors.mapping(ProductResponse::from, Collectors.toList())));

        return categories.stream()
                .map(category -> new CategoryProductsResponse(
                        CategoryResponse.from(category),
                        productsByCategory.getOrDefault(category.getId(), List.of()).stream()
                                .sorted(Comparator.comparing(ProductResponse::name))
                                .toList()))
                .filter(categoryProducts -> !categoryProducts.products().isEmpty())
                .toList();
    }

    private boolean matchesProductFilters(ProductEntity product, String name, UUID categoryId) {
        boolean matchesName = name == null
                || name.isBlank()
                || product.getName().toLowerCase().contains(name.trim().toLowerCase());
        boolean matchesCategory = categoryId == null
                || (product.getCategory() != null && categoryId.equals(product.getCategory().getId()));
        return matchesName && matchesCategory;
    }

    private boolean isCategoryVisible(CategoryEntity category, CatalogChannel channel) {
        if (!category.isActive()) {
            return false;
        }
        return switch (channel) {
            case PDV -> category.isShowOnPdv();
            case SITE -> category.isShowOnline();
            case WHATSAPP -> category.isShowOnWhatsapp();
            case ADMIN -> true;
        };
    }

    private boolean isProductVisible(ProductEntity product, CatalogChannel channel) {
        if (!product.isActive() || product.getCategory() == null || !isCategoryVisible(product.getCategory(), channel)) {
            return false;
        }
        return switch (channel) {
            case PDV -> isSellableOnPdv(product);
            case SITE -> product.isSellOnline() && product.isAvailable() && !hasUnavailableStatus(product, SalesChannel.SITE);
            case WHATSAPP -> product.isSellOnWhatsapp() && product.isAvailable() && !hasUnavailableStatus(product, SalesChannel.WHATSAPP);
            case ADMIN -> true;
        };
    }

    private boolean isSellableOnPdv(ProductEntity product) {
        return product.isActive()
                && product.isAvailable()
                && product.isSellOnPdv()
                && product.getCategory() != null
                && product.getCategory().isActive()
                && product.getCategory().isShowOnPdv()
                && !hasUnavailableStatus(product, SalesChannel.PDV);
    }

    private boolean hasUnavailableStatus(ProductEntity product, SalesChannel channel) {
        return latestAvailability(product.getId(), SalesChannel.ALL)
                .filter(availability -> availability.getStatus() == AvailabilityStatus.UNAVAILABLE)
                .isPresent()
                || latestAvailability(product.getId(), channel)
                        .filter(availability -> availability.getStatus() == AvailabilityStatus.UNAVAILABLE)
                        .isPresent();
    }

    private Optional<ProductAvailabilityEntity> latestAvailability(UUID productId, SalesChannel channel) {
        return productAvailabilityRepository()
                .findFirstByProductIdAndChannelOrderByUpdatedAtDesc(productId, channel);
    }

    private void validateAvailabilityReason(AvailabilityStatus status, String reason) {
        if (status == AvailabilityStatus.UNAVAILABLE && (reason == null || reason.isBlank())) {
            throw new BusinessException(
                    "AVAILABILITY_REASON_REQUIRED",
                    "Motivo obrigatorio para indisponibilidade.",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private String normalizeReason(String reason) {
        return reason == null || reason.isBlank() ? null : reason.trim();
    }

    private String availabilityEventType(AvailabilityStatus status) {
        return status == AvailabilityStatus.UNAVAILABLE ? "PRODUCT_UNAVAILABLE" : "PRODUCT_AVAILABLE";
    }

    private Map<String, Object> availabilitySnapshot(ProductAvailabilityEntity availability) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("status", availability.getStatus().name());
        snapshot.put("channel", availability.getChannel().name());
        snapshot.put("reason", availability.getReason());
        snapshot.put("updatedBy", availability.getUpdatedBy() == null ? null : availability.getUpdatedBy().toString());
        return snapshot;
    }

    private CategoryRepository categoryRepository() {
        return categoryRepository.orElseThrow(() -> new IllegalStateException("Category repository is not available."));
    }

    private ProductRepository productRepository() {
        return productRepository.orElseThrow(() -> new IllegalStateException("Product repository is not available."));
    }

    private ProductAvailabilityRepository productAvailabilityRepository() {
        return productAvailabilityRepository
                .orElseThrow(() -> new IllegalStateException("Product availability repository is not available."));
    }
}
