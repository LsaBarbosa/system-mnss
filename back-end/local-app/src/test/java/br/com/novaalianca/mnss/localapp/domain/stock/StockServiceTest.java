package br.com.novaalianca.mnss.localapp.domain.stock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.core.catalog.UnitType;
import br.com.novaalianca.mnss.core.payment.PaymentStatus;
import br.com.novaalianca.mnss.localapp.domain.audit.AuditLogRequest;
import br.com.novaalianca.mnss.localapp.domain.audit.AuditService;
import br.com.novaalianca.mnss.localapp.domain.catalog.CategoryEntity;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductEntity;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductRepository;
import br.com.novaalianca.mnss.localapp.domain.order.DeliveryType;
import br.com.novaalianca.mnss.localapp.domain.order.OrderEntity;
import br.com.novaalianca.mnss.localapp.domain.order.OrderOrigin;
import br.com.novaalianca.mnss.localapp.domain.order.OrderRepository;
import br.com.novaalianca.mnss.localapp.domain.order.OrderStatus;
import br.com.novaalianca.mnss.sharedinfra.web.error.BusinessException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {
    @Mock private ProductRepository productRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private StockMovementRepository stockMovementRepository;
    @Mock private StockBalanceRepository stockBalanceRepository;
    @Mock private StockSyncEventService syncEventService;
    @Mock private AuditService auditService;

    @Test
    void entryRequiresPositiveQuantityProductAndUser() {
        assertThatThrownBy(() -> service().createMovement(new CreateStockMovementRequest(
                        UUID.randomUUID(), StockMovementType.IN, BigDecimal.ZERO, null, null),
                UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);

        assertThatThrownBy(() -> service().createMovement(new CreateStockMovementRequest(
                        null, StockMovementType.IN, new BigDecimal("1.000"), null, null),
                UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);

        assertThatThrownBy(() -> service().createMovement(new CreateStockMovementRequest(
                        UUID.randomUUID(), StockMovementType.IN, new BigDecimal("1.000"), null, null),
                null))
                .isInstanceOf(BusinessException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void saleTypeIsBlockedInManualEntry() {
        assertThatThrownBy(() -> service().createMovement(new CreateStockMovementRequest(
                        UUID.randomUUID(), StockMovementType.SALE, new BigDecimal("1.000"), null, null),
                UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void entrySavesMovementWithUserAuditAndSyncEvent() {
        UUID productId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        ProductEntity product = product(productId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(stockBalanceRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.empty());
        when(stockBalanceRepository.save(any(StockBalanceEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockMovementRepository.save(any(StockMovementEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditService.record(any(AuditLogRequest.class))).thenReturn(null);

        StockMovementResponse response = service().createMovement(new CreateStockMovementRequest(
                productId, StockMovementType.IN, new BigDecimal("10.000"), null, null), actorUserId);

        ArgumentCaptor<StockMovementEntity> movementCaptor = ArgumentCaptor.forClass(StockMovementEntity.class);
        ArgumentCaptor<StockMovementEntity> syncMovementCaptor = ArgumentCaptor.forClass(StockMovementEntity.class);
        ArgumentCaptor<BigDecimal> balanceCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(stockMovementRepository).save(movementCaptor.capture());
        verify(syncEventService).recordStockMovementEvent(syncMovementCaptor.capture(), balanceCaptor.capture());
        assertThat(response.type()).isEqualTo(StockMovementType.IN);
        assertThat(response.createdBy()).isEqualTo(actorUserId);
        assertThat(syncMovementCaptor.getValue()).isSameAs(movementCaptor.getValue());
        assertThat(balanceCaptor.getValue()).isEqualByComparingTo("10.000");

        ArgumentCaptor<AuditLogRequest> auditCaptor = ArgumentCaptor.forClass(AuditLogRequest.class);
        verify(auditService).record(auditCaptor.capture());
        assertThat(auditCaptor.getValue().actorUserId()).isEqualTo(actorUserId);
        assertThat(auditCaptor.getValue().details()).containsEntry("balanceAfter", "10.000");
    }

    @Test
    void lossRequiresReasonAndDoesNotAllowNegativeBalance() {
        UUID productId = UUID.randomUUID();
        ProductEntity product = product(productId);

        assertThatThrownBy(() -> service().createMovement(new CreateStockMovementRequest(
                        productId, StockMovementType.LOSS, new BigDecimal("1.000"), " ", null),
                UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);

        ReflectionTestUtils.setField(product, "stockControlled", true);
        StockBalanceEntity balance = new StockBalanceEntity(product);
        balance.adjust(new BigDecimal("2.000"));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(stockBalanceRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.of(balance));

        assertThatThrownBy(() -> service().createMovement(new CreateStockMovementRequest(
                        productId, StockMovementType.LOSS, new BigDecimal("3.000"), "Quebra", null),
                UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
        verify(stockMovementRepository, never()).save(any(StockMovementEntity.class));
    }

    @Test
    void saleMovementSubtractsStock() {
        UUID productId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        ProductEntity product = product(productId);
        StockBalanceEntity balance = new StockBalanceEntity(product);
        balance.adjust(new BigDecimal("5.000"));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(stockBalanceRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.of(balance));
        when(stockBalanceRepository.save(any(StockBalanceEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockMovementRepository.save(any(StockMovementEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditService.record(any(AuditLogRequest.class))).thenReturn(null);

        StockMovementResponse response = service().recordSaleMovement(
                productId, new BigDecimal("2.000"), null, actorUserId);

        ArgumentCaptor<BigDecimal> balanceCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        ArgumentCaptor<StockMovementEntity> movementCaptor = ArgumentCaptor.forClass(StockMovementEntity.class);
        verify(syncEventService).recordStockMovementEvent(movementCaptor.capture(), balanceCaptor.capture());
        assertThat(response.type()).isEqualTo(StockMovementType.SALE);
        assertThat(movementCaptor.getValue().getReason()).isEqualTo("Venda finalizada");
        assertThat(balanceCaptor.getValue()).isEqualByComparingTo("3.000");
    }

    @Test
    void balancesAreReadFromPersistedStockBalances() {
        UUID productId = UUID.randomUUID();
        UUID emptyProductId = UUID.randomUUID();
        ProductEntity product = product(productId);
        ProductEntity emptyProduct = product(emptyProductId);
        ReflectionTestUtils.setField(emptyProduct, "name", "Cafe");

        StockBalanceEntity balance = new StockBalanceEntity(product);
        balance.adjust(new BigDecimal("7.000"));

        when(productRepository.findAllByOrderByNameAsc(any())).thenReturn(List.of(product, emptyProduct));
        when(stockBalanceRepository.findByProductIdIn(List.of(productId, emptyProductId)))
                .thenReturn(List.of(balance));

        List<StockBalanceResponse> balances = service().listBalances();

        assertThat(balances).hasSize(2);
        assertThat(balances.get(0).quantity()).isEqualByComparingTo("7.000");
        assertThat(balances.get(1).quantity()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calculateBalanceUsesPersistedBalance() {
        UUID productId = UUID.randomUUID();
        ProductEntity product = product(productId);
        StockBalanceEntity balance = new StockBalanceEntity(product);
        balance.adjust(new BigDecimal("15.000"));

        when(stockBalanceRepository.findByProductId(productId)).thenReturn(Optional.of(balance));

        BigDecimal result = service().calculateBalance(productId);

        assertThat(result).isEqualByComparingTo("15.000");
    }

    // S05-H01: stockControlled flag
    @Test
    void nonControlledProductDoesNotBlockOnInsufficientBalance() {
        UUID productId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        ProductEntity product = product(productId);
        // stockControlled defaults to false — no block on negative balance
        StockBalanceEntity balance = new StockBalanceEntity(product);
        balance.adjust(new BigDecimal("1.000"));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(stockBalanceRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.of(balance));
        when(stockBalanceRepository.save(any(StockBalanceEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockMovementRepository.save(any(StockMovementEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditService.record(any(AuditLogRequest.class))).thenReturn(null);

        // Sale of 5 on a balance of 1 — allowed because stockControlled=false
        StockMovementResponse response = service().recordSaleMovement(
                productId, new BigDecimal("5.000"), null, actorUserId);

        assertThat(response.type()).isEqualTo(StockMovementType.SALE);
    }

    @Test
    void controlledProductBlocksSaleWhenBalanceInsufficient() {
        UUID productId = UUID.randomUUID();
        ProductEntity product = product(productId);
        ReflectionTestUtils.setField(product, "stockControlled", true);
        StockBalanceEntity balance = new StockBalanceEntity(product);
        balance.adjust(new BigDecimal("2.000"));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(stockBalanceRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.of(balance));

        assertThatThrownBy(() -> service().recordSaleMovement(
                        productId, new BigDecimal("5.000"), null, UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void saleMovementRecordsPreviousAndResultingQuantity() {
        UUID productId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        ProductEntity product = product(productId);
        StockBalanceEntity balance = new StockBalanceEntity(product);
        balance.adjust(new BigDecimal("10.000"));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(stockBalanceRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.of(balance));
        when(stockBalanceRepository.save(any(StockBalanceEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockMovementRepository.save(any(StockMovementEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditService.record(any(AuditLogRequest.class))).thenReturn(null);

        service().recordSaleMovement(productId, new BigDecimal("3.000"), null, actorUserId);

        ArgumentCaptor<StockMovementEntity> captor = ArgumentCaptor.forClass(StockMovementEntity.class);
        verify(stockMovementRepository).save(captor.capture());
        StockMovementEntity saved = captor.getValue();
        assertThat(saved.getPreviousQuantity()).isEqualByComparingTo("10.000");
        assertThat(saved.getResultingQuantity()).isEqualByComparingTo("7.000");
    }

    @Test
    void saleMovementUsesLogicalIdempotencyBeforeChangingBalance() {
        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        String idempotencyKey = "SALE:" + orderId + ":" + productId;
        ProductEntity product = product(productId);
        OrderEntity order = order(orderId);
        StockBalanceEntity balance = new StockBalanceEntity(product);
        balance.adjust(new BigDecimal("5.000"));
        AtomicReference<StockMovementEntity> savedMovement = new AtomicReference<>();

        when(stockMovementRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.empty())
                .thenAnswer(invocation -> Optional.of(savedMovement.get()));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(stockBalanceRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.of(balance));
        when(stockBalanceRepository.save(any(StockBalanceEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockMovementRepository.save(any(StockMovementEntity.class))).thenAnswer(inv -> {
            StockMovementEntity movement = inv.getArgument(0);
            savedMovement.set(movement);
            return movement;
        });
        when(auditService.record(any(AuditLogRequest.class))).thenReturn(null);

        StockMovementResponse first = service().recordSaleMovement(
                productId, new BigDecimal("2.000"), orderId, actorUserId);
        StockMovementResponse second = service().recordSaleMovement(
                productId, new BigDecimal("2.000"), orderId, actorUserId);

        assertThat(first.resultingQuantity()).isEqualByComparingTo("3.000");
        assertThat(second.resultingQuantity()).isEqualByComparingTo("3.000");
        assertThat(balance.getQuantity()).isEqualByComparingTo("3.000");
        verify(stockBalanceRepository, times(1)).save(any(StockBalanceEntity.class));
        verify(stockMovementRepository, times(1)).save(any(StockMovementEntity.class));
        verify(syncEventService, times(1)).recordStockMovementEvent(any(StockMovementEntity.class), any(BigDecimal.class));
        verify(auditService, times(1)).record(any(AuditLogRequest.class));
    }

    @Test
    void saleMovementStoresIdempotencyKey() {
        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        ProductEntity product = product(productId);
        StockBalanceEntity balance = new StockBalanceEntity(product);
        balance.adjust(new BigDecimal("4.000"));
        when(stockMovementRepository.findByIdempotencyKey("SALE:" + orderId + ":" + productId))
                .thenReturn(Optional.empty());
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order(orderId)));
        when(stockBalanceRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.of(balance));
        when(stockBalanceRepository.save(any(StockBalanceEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockMovementRepository.save(any(StockMovementEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditService.record(any(AuditLogRequest.class))).thenReturn(null);

        service().recordSaleMovement(productId, new BigDecimal("1.000"), orderId, actorUserId);

        ArgumentCaptor<StockMovementEntity> captor = ArgumentCaptor.forClass(StockMovementEntity.class);
        verify(stockMovementRepository).save(captor.capture());
        assertThat(captor.getValue().getIdempotencyKey()).isEqualTo("SALE:" + orderId + ":" + productId);
    }

    @Test
    void saleMovementsWithDifferentIdempotencyKeysSubtractNormally() {
        UUID productId = UUID.randomUUID();
        UUID firstOrderId = UUID.randomUUID();
        UUID secondOrderId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        ProductEntity product = product(productId);
        StockBalanceEntity balance = new StockBalanceEntity(product);
        balance.adjust(new BigDecimal("10.000"));

        when(stockMovementRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderRepository.findById(firstOrderId)).thenReturn(Optional.of(order(firstOrderId)));
        when(orderRepository.findById(secondOrderId)).thenReturn(Optional.of(order(secondOrderId)));
        when(stockBalanceRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.of(balance));
        when(stockBalanceRepository.save(any(StockBalanceEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockMovementRepository.save(any(StockMovementEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditService.record(any(AuditLogRequest.class))).thenReturn(null);

        service().recordSaleMovement(productId, new BigDecimal("2.000"), firstOrderId, actorUserId);
        service().recordSaleMovement(productId, new BigDecimal("3.000"), secondOrderId, actorUserId);

        assertThat(balance.getQuantity()).isEqualByComparingTo("5.000");
        verify(stockMovementRepository, times(2)).save(any(StockMovementEntity.class));
        verify(syncEventService, times(2)).recordStockMovementEvent(any(StockMovementEntity.class), any(BigDecimal.class));
    }

    // S05-H03: ajuste manual auditável
    @Test
    void adjustmentRequiresReason() {
        UUID productId = UUID.randomUUID();
        assertThatThrownBy(() -> service().createMovement(new CreateStockMovementRequest(
                        productId, StockMovementType.ADJUSTMENT, new BigDecimal("1.000"), null, null),
                UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void adjustmentWithReasonSavesMovementAndRecordsAudit() {
        UUID productId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        ProductEntity product = product(productId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(stockBalanceRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.empty());
        when(stockBalanceRepository.save(any(StockBalanceEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockMovementRepository.save(any(StockMovementEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditService.record(any(AuditLogRequest.class))).thenReturn(null);

        StockMovementResponse response = service().createMovement(
                new CreateStockMovementRequest(productId, StockMovementType.ADJUSTMENT,
                        new BigDecimal("5.000"), "Contagem física", null),
                actorUserId);

        assertThat(response.type()).isEqualTo(StockMovementType.ADJUSTMENT);
        verify(auditService).record(any(AuditLogRequest.class));
    }

    // S05-H04: sync event is generated on every movement
    @Test
    void saleMovementGeneratesSyncEvent() {
        UUID productId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        ProductEntity product = product(productId);
        StockBalanceEntity balance = new StockBalanceEntity(product);
        balance.adjust(new BigDecimal("10.000"));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(stockBalanceRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.of(balance));
        when(stockBalanceRepository.save(any(StockBalanceEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockMovementRepository.save(any(StockMovementEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditService.record(any(AuditLogRequest.class))).thenReturn(null);

        service().recordSaleMovement(productId, new BigDecimal("3.000"), null, actorUserId);

        verify(syncEventService).recordStockMovementEvent(any(StockMovementEntity.class), any(BigDecimal.class));
    }

    @Test
    void entryMovementGeneratesSyncEvent() {
        UUID productId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        ProductEntity product = product(productId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(stockBalanceRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.empty());
        when(stockBalanceRepository.save(any(StockBalanceEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockMovementRepository.save(any(StockMovementEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditService.record(any(AuditLogRequest.class))).thenReturn(null);

        service().createMovement(new CreateStockMovementRequest(
                productId, StockMovementType.IN, new BigDecimal("10.000"), null, null), actorUserId);

        verify(syncEventService).recordStockMovementEvent(any(StockMovementEntity.class), any(BigDecimal.class));
    }

    private StockService service() {
        return new StockService(
                productRepository,
                orderRepository,
                stockMovementRepository,
                stockBalanceRepository,
                syncEventService,
                auditService);
    }

    private ProductEntity product(UUID id) {
        ProductEntity product = new ProductEntity(
                new CategoryEntity("Paes"),
                "Pao frances",
                new BigDecimal("1.20"),
                UnitType.UNIT,
                PreparationSector.SEM_PREPARO);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    private OrderEntity order(UUID id) {
        OrderEntity order = new OrderEntity(
                OrderOrigin.PDV,
                OrderStatus.CREATED,
                PaymentStatus.PENDING,
                DeliveryType.LOCAL_CONSUMPTION);
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }
}
