package br.com.novaalianca.mnss.localapp.domain.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.core.catalog.SalesChannel;
import br.com.novaalianca.mnss.core.catalog.UnitType;
import br.com.novaalianca.mnss.localapp.domain.audit.AuditLogRequest;
import br.com.novaalianca.mnss.localapp.domain.audit.AuditService;
import br.com.novaalianca.mnss.sharedinfra.web.error.BusinessException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductAvailabilityRepository productAvailabilityRepository;

    @Mock
    private CatalogSyncEventService syncEventService;

    @Mock
    private AuditService auditService;

    @Test
    void createCategoryRequiresName() {
        assertThatThrownBy(() -> service().createCategory(new CreateCategoryRequest(
                        " ",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name must not be blank");
    }

    @Test
    void createCategoryUsesDefaultOrderAndActiveState() {
        when(categoryRepository.save(any(CategoryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditService.record(any(AuditLogRequest.class))).thenReturn(null);

        CategoryResponse response = service().createCategory(new CreateCategoryRequest(
                "Paes",
                null,
                null,
                null,
                null,
                null,
                null,
                null), null);

        ArgumentCaptor<CategoryEntity> captor = ArgumentCaptor.forClass(CategoryEntity.class);
        verify(categoryRepository).save(captor.capture());
        assertThat(response.name()).isEqualTo("Paes");
        assertThat(captor.getValue().getDisplayOrder()).isZero();
        assertThat(captor.getValue().isActive()).isTrue();
        verify(syncEventService).recordCategoryEvent("CATEGORY_CREATED", captor.getValue());
    }

    @Test
    void updateCategoryRefusesMissingCategory() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().updateCategory(
                        id,
                        new PatchCategoryRequest("Bolos", null, null, null, null, null, null, null),
                        null))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateCategoryPreservesIdAndTouchesUpdatedAt() {
        UUID id = UUID.randomUUID();
        CategoryEntity category = new CategoryEntity("Paes");
        ReflectionTestUtils.setField(category, "id", id);
        Instant previousUpdatedAt = category.getUpdatedAt();
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(CategoryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditService.record(any(AuditLogRequest.class))).thenReturn(null);

        CategoryResponse response = service().updateCategory(
                id,
                new PatchCategoryRequest("Bolos", null, null, null, null, null, null, null),
                null);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.name()).isEqualTo("Bolos");
        assertThat(response.updatedAt()).isAfter(previousUpdatedAt);
    }

    @Test
    void hiddenCategoryDoesNotAppearInPdvQuery() {
        when(categoryRepository.findByActiveTrueAndShowOnPdvTrueOrderByDisplayOrderAscNameAsc())
                .thenReturn(List.of(new CategoryEntity("Visivel")));

        List<CategoryResponse> categories = service().listCategories(CatalogChannel.PDV);

        assertThat(categories).extracting(CategoryResponse::name).containsExactly("Visivel");
        verify(categoryRepository).findByActiveTrueAndShowOnPdvTrueOrderByDisplayOrderAscNameAsc();
    }

    @Test
    void createProductRequiresExistingCategory() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().createProduct(validCreateProductRequest(categoryId), null))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createProductRequiresUnitAndSector() {
        CategoryEntity category = new CategoryEntity("Paes");
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> service().createProduct(new CreateProductRequest(
                        categoryId,
                        "Pao frances",
                        null,
                        new BigDecimal("1.20"),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        PreparationSector.SEM_PREPARO,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("unitType must not be null");
    }

    @Test
    void updateProductPriceGeneratesPriceChangedEvent() {
        UUID id = UUID.randomUUID();
        ProductEntity product = product();
        ReflectionTestUtils.setField(product, "id", id);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditService.record(any(AuditLogRequest.class))).thenReturn(null);

        ProductResponse response = service().updateProduct(
                id,
                new PatchProductRequest(
                        null,
                        null,
                        null,
                        new BigDecimal("1.50"),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null),
                null);

        assertThat(response.price()).isEqualByComparingTo("1.50");
        verify(syncEventService).recordProductEvent("PRODUCT_PRICE_CHANGED", product);
    }

    @Test
    void updateProductRefusesMissingProduct() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().updateProduct(
                        id,
                        new PatchProductRequest(null, null, null, new BigDecimal("1.50"), null, null, null, null, null, null, null, null, null, null, null, null, null),
                        null))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void inactiveProductDoesNotReturnAsSellableByBarcode() {
        ProductEntity product = product();
        product.update(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "789100000001",
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null);
        when(productRepository.findByBarcode("789100000001")).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> service().findSellableProductByBarcode("789100000001"))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void missingBarcodeReturnsNotFound() {
        when(productRepository.findByBarcode("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().findSellableProductByBarcode("missing"))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void unavailableProductRequiresReason() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product()));

        assertThatThrownBy(() -> service().updateProductAvailability(
                        productId,
                        new PatchProductAvailabilityRequest(AvailabilityStatus.UNAVAILABLE, SalesChannel.ALL, null, " "),
                        UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void availabilityChangeSavesUserAndCreatesPendingSyncEvent() {
        UUID productId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        ProductEntity product = product();
        ReflectionTestUtils.setField(product, "id", productId);
        ProductAvailabilityEntity oldAvailability = new ProductAvailabilityEntity(
                product,
                AvailabilityStatus.AVAILABLE,
                SalesChannel.ALL);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productAvailabilityRepository.findFirstByProductIdAndChannelOrderByUpdatedAtDesc(productId, SalesChannel.ALL))
                .thenReturn(Optional.of(oldAvailability));
        when(productAvailabilityRepository.save(any(ProductAvailabilityEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditService.record(any(AuditLogRequest.class))).thenReturn(null);

        ProductAvailabilityResponse response = service().updateProductAvailability(
                productId,
                new PatchProductAvailabilityRequest(
                        AvailabilityStatus.UNAVAILABLE,
                        SalesChannel.ALL,
                        null,
                        "Acabou na loja"),
                actorUserId);

        assertThat(response.status()).isEqualTo(AvailabilityStatus.UNAVAILABLE);
        assertThat(response.updatedBy()).isEqualTo(actorUserId);
        assertThat(response.syncStatus()).isEqualTo("PENDING");
        assertThat(product.isAvailable()).isFalse();
        verify(syncEventService).recordAvailabilityEvent("PRODUCT_UNAVAILABLE", oldAvailability);
    }

    @Test
    @SuppressWarnings("unchecked")
    void availabilityAuditContainsUserOldValueNewValueAndEntityId() {
        UUID productId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        ProductEntity product = product();
        ReflectionTestUtils.setField(product, "id", productId);
        ProductAvailabilityEntity oldAvailability = new ProductAvailabilityEntity(
                product,
                AvailabilityStatus.AVAILABLE,
                SalesChannel.ALL);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productAvailabilityRepository.findFirstByProductIdAndChannelOrderByUpdatedAtDesc(productId, SalesChannel.ALL))
                .thenReturn(Optional.of(oldAvailability));
        when(productAvailabilityRepository.save(any(ProductAvailabilityEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditService.record(any(AuditLogRequest.class))).thenReturn(null);

        service().updateProductAvailability(
                productId,
                new PatchProductAvailabilityRequest(
                        AvailabilityStatus.UNAVAILABLE,
                        SalesChannel.ALL,
                        null,
                        "Acabou na loja"),
                actorUserId);

        ArgumentCaptor<AuditLogRequest> captor = ArgumentCaptor.forClass(AuditLogRequest.class);
        verify(auditService).record(captor.capture());
        AuditLogRequest audit = captor.getValue();
        assertThat(audit.actorUserId()).isEqualTo(actorUserId);
        assertThat(audit.entityId()).isEqualTo(productId);
        assertThat((Map<String, Object>) audit.details().get("oldValue")).containsEntry("status", "AVAILABLE");
        assertThat((Map<String, Object>) audit.details().get("newValue")).containsEntry("status", "UNAVAILABLE");
    }

    @Test
    void syncFailureDoesNotUndoLocalAvailabilityChange() {
        UUID productId = UUID.randomUUID();
        ProductEntity product = product();
        ReflectionTestUtils.setField(product, "id", productId);
        ProductAvailabilityEntity availability = new ProductAvailabilityEntity(
                product,
                AvailabilityStatus.AVAILABLE,
                SalesChannel.ALL);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productAvailabilityRepository.findFirstByProductIdAndChannelOrderByUpdatedAtDesc(productId, SalesChannel.ALL))
                .thenReturn(Optional.of(availability));
        when(productAvailabilityRepository.save(any(ProductAvailabilityEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditService.record(any(AuditLogRequest.class))).thenReturn(null);
        doThrow(new RuntimeException("sync down"))
                .when(syncEventService)
                .recordAvailabilityEvent("PRODUCT_UNAVAILABLE", availability);

        ProductAvailabilityResponse response = service().updateProductAvailability(
                productId,
                new PatchProductAvailabilityRequest(
                        AvailabilityStatus.UNAVAILABLE,
                        SalesChannel.ALL,
                        null,
                        "Acabou na loja"),
                UUID.randomUUID());

        assertThat(response.status()).isEqualTo(AvailabilityStatus.UNAVAILABLE);
        assertThat(response.syncStatus()).isEqualTo("FAILED");
        verify(productAvailabilityRepository).save(availability);
    }

    @Test
    void unavailableProductDoesNotAppearInOnlineMenu() {
        UUID categoryId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        CategoryEntity category = category(categoryId, "Paes");
        ProductEntity product = product(category, productId);
        ProductAvailabilityEntity unavailable = new ProductAvailabilityEntity(
                product,
                AvailabilityStatus.UNAVAILABLE,
                SalesChannel.ALL);
        when(categoryRepository.findAllByOrderByDisplayOrderAscNameAsc()).thenReturn(List.of(category));
        when(productRepository.findAllByOrderByNameAsc()).thenReturn(List.of(product));
        when(productAvailabilityRepository.findFirstByProductIdAndChannelOrderByUpdatedAtDesc(productId, SalesChannel.ALL))
                .thenReturn(Optional.of(unavailable));

        assertThat(service().listPublicMenu()).isEmpty();
    }

    @Test
    void pdvProductsRemoveInactiveAndProductsWithoutPdvChannel() {
        UUID categoryId = UUID.randomUUID();
        CategoryEntity category = category(categoryId, "Paes");
        ProductEntity sellable = product(category, UUID.randomUUID());
        ProductEntity inactive = product(category, UUID.randomUUID());
        inactive.update(null, null, null, null, null, null, null, null, null, null, null, null, false, null, null, null, null);
        ProductEntity withoutPdv = product(category, UUID.randomUUID());
        withoutPdv.update(null, null, null, null, null, null, null, null, null, null, null, null, null, null, false, null, null);
        when(categoryRepository.findAllByOrderByDisplayOrderAscNameAsc()).thenReturn(List.of(category));
        when(productRepository.findAllByOrderByNameAsc()).thenReturn(List.of(sellable, inactive, withoutPdv));

        List<CategoryProductsResponse> response = service().listPdvProducts();

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().products()).extracting(ProductResponse::id).containsExactly(sellable.getId());
    }

    private CatalogService service() {
        return new CatalogService(
                Optional.of(categoryRepository),
                Optional.of(productRepository),
                Optional.of(productAvailabilityRepository),
                syncEventService,
                auditService);
    }

    private CreateProductRequest validCreateProductRequest(UUID categoryId) {
        return new CreateProductRequest(
                categoryId,
                "Pao frances",
                null,
                new BigDecimal("1.20"),
                null,
                null,
                null,
                "789100000001",
                null,
                UnitType.UNIT,
                PreparationSector.SEM_PREPARO,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private ProductEntity product() {
        return product(category(UUID.randomUUID(), "Paes"), UUID.randomUUID());
    }

    private ProductEntity product(CategoryEntity category, UUID id) {
        ProductEntity product = new ProductEntity(
                category,
                "Pao frances",
                new BigDecimal("1.20"),
                UnitType.UNIT,
                PreparationSector.SEM_PREPARO);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    private CategoryEntity category(UUID id, String name) {
        CategoryEntity category = new CategoryEntity(name);
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }
}
