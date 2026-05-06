package br.com.novaalianca.mnss.localapp.domain.stock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.core.catalog.UnitType;
import br.com.novaalianca.mnss.localapp.domain.audit.AuditLogRequest;
import br.com.novaalianca.mnss.localapp.domain.audit.AuditService;
import br.com.novaalianca.mnss.localapp.domain.catalog.CategoryEntity;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductEntity;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductRepository;
import br.com.novaalianca.mnss.localapp.domain.order.OrderRepository;
import br.com.novaalianca.mnss.sharedinfra.web.error.BusinessException;
import java.math.BigDecimal;
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
class StockServiceTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private StockSyncEventService syncEventService;

    @Mock
    private AuditService auditService;

    @Test
    void entryRequiresPositiveQuantityProductAndUser() {
        assertThatThrownBy(() -> service().createMovement(new CreateStockMovementRequest(
                        UUID.randomUUID(),
                        StockMovementType.IN,
                        BigDecimal.ZERO,
                        null,
                        null), UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThatThrownBy(() -> service().createMovement(new CreateStockMovementRequest(
                        null,
                        StockMovementType.IN,
                        new BigDecimal("1.000"),
                        null,
                        null), UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThatThrownBy(() -> service().createMovement(new CreateStockMovementRequest(
                        UUID.randomUUID(),
                        StockMovementType.IN,
                        new BigDecimal("1.000"),
                        null,
                        null), null))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void entrySavesMovementWithUserAuditAndSyncEvent() {
        UUID productId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        ProductEntity product = product(productId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId)).thenReturn(List.of());
        when(stockMovementRepository.save(any(StockMovementEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditService.record(any(AuditLogRequest.class))).thenReturn(null);

        StockMovementResponse response = service().createMovement(new CreateStockMovementRequest(
                productId,
                StockMovementType.IN,
                new BigDecimal("10.000"),
                null,
                null), actorUserId);

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
                        productId,
                        StockMovementType.LOSS,
                        new BigDecimal("1.000"),
                        " ",
                        null), UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId))
                .thenReturn(List.of(new StockMovementEntity(product, StockMovementType.IN, new BigDecimal("2.000"))));

        assertThatThrownBy(() -> service().createMovement(new CreateStockMovementRequest(
                        productId,
                        StockMovementType.LOSS,
                        new BigDecimal("3.000"),
                        "Quebra",
                        null), UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
        verify(stockMovementRepository, never()).save(any(StockMovementEntity.class));
    }

    @Test
    void saleMovementSubtractsStock() {
        UUID productId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        ProductEntity product = product(productId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId))
                .thenReturn(List.of(new StockMovementEntity(product, StockMovementType.IN, new BigDecimal("5.000"))));
        when(stockMovementRepository.save(any(StockMovementEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditService.record(any(AuditLogRequest.class))).thenReturn(null);

        StockMovementResponse response = service().recordSaleMovement(
                productId,
                new BigDecimal("2.000"),
                null,
                actorUserId);

        ArgumentCaptor<BigDecimal> balanceCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        ArgumentCaptor<StockMovementEntity> movementCaptor = ArgumentCaptor.forClass(StockMovementEntity.class);
        verify(syncEventService).recordStockMovementEvent(movementCaptor.capture(), balanceCaptor.capture());
        assertThat(response.type()).isEqualTo(StockMovementType.SALE);
        assertThat(movementCaptor.getValue().getReason()).isEqualTo("Venda finalizada");
        assertThat(balanceCaptor.getValue()).isEqualByComparingTo("3.000");
    }

    @Test
    void balancesAreCalculatedFromMovementsByProduct() {
        UUID productId = UUID.randomUUID();
        UUID emptyProductId = UUID.randomUUID();
        ProductEntity product = product(productId);
        ProductEntity emptyProduct = product(emptyProductId);
        ReflectionTestUtils.setField(emptyProduct, "name", "Cafe");
        when(productRepository.findAllByOrderByNameAsc()).thenReturn(List.of(product, emptyProduct));
        when(stockMovementRepository.findByProductIdIn(List.of(productId, emptyProductId))).thenReturn(List.of(
                new StockMovementEntity(product, StockMovementType.IN, new BigDecimal("10.000")),
                new StockMovementEntity(product, StockMovementType.LOSS, new BigDecimal("3.000"))));

        List<StockBalanceResponse> balances = service().listBalances();

        assertThat(balances).hasSize(2);
        assertThat(balances.get(0).quantity()).isEqualByComparingTo("7.000");
        assertThat(balances.get(1).quantity()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private StockService service() {
        return new StockService(
                productRepository,
                orderRepository,
                stockMovementRepository,
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
}
