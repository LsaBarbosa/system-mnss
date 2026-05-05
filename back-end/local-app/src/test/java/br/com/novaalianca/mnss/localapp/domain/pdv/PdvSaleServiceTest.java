package br.com.novaalianca.mnss.localapp.domain.pdv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.core.catalog.UnitType;
import br.com.novaalianca.mnss.localapp.domain.cash.CashRegisterRepository;
import br.com.novaalianca.mnss.localapp.domain.cash.CashRegisterStatus;
import br.com.novaalianca.mnss.localapp.domain.catalog.CategoryEntity;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductAvailabilityRepository;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductEntity;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductRepository;
import br.com.novaalianca.mnss.localapp.domain.order.DeliveryType;
import br.com.novaalianca.mnss.localapp.domain.order.OrderEntity;
import br.com.novaalianca.mnss.localapp.domain.order.OrderItemEntity;
import br.com.novaalianca.mnss.localapp.domain.order.OrderItemRepository;
import br.com.novaalianca.mnss.localapp.domain.order.OrderItemStatus;
import br.com.novaalianca.mnss.localapp.domain.order.OrderOrigin;
import br.com.novaalianca.mnss.localapp.domain.order.OrderRepository;
import br.com.novaalianca.mnss.localapp.domain.order.OrderStatus;
import br.com.novaalianca.mnss.localapp.domain.payment.PaymentStatus;
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
class PdvSaleServiceTest {
    @Mock
    private CashRegisterRepository cashRegisterRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductAvailabilityRepository productAvailabilityRepository;

    @Test
    void createSaleRequiresOpenCashRegister() {
        UUID operatorId = UUID.randomUUID();
        when(cashRegisterRepository.existsByOperatorIdAndStatus(operatorId, CashRegisterStatus.OPEN)).thenReturn(false);

        assertThatThrownBy(() -> service().createSale(operatorId))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createSaleCreatesPdvOrderWithCreatedStatus() {
        UUID operatorId = UUID.randomUUID();
        UUID saleId = UUID.randomUUID();
        when(cashRegisterRepository.existsByOperatorIdAndStatus(operatorId, CashRegisterStatus.OPEN)).thenReturn(true);
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", saleId);
            return order;
        });
        when(orderItemRepository.findByOrderIdOrderByCreatedAtAsc(saleId)).thenReturn(List.of());

        PdvSaleResponse response = service().createSale(operatorId);

        ArgumentCaptor<OrderEntity> captor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getOrigin()).isEqualTo(OrderOrigin.PDV);
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(response.id()).isEqualTo(saleId);
    }

    @Test
    void addItemBlocksInactiveProduct() {
        UUID saleId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        ProductEntity product = product(productId);
        product.update(null, null, null, null, null, null, null, null, null, null, null, null, false, null, null, null, null);
        when(orderRepository.findById(saleId)).thenReturn(Optional.of(sale(saleId)));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> service().addItem(
                        saleId,
                        new CreatePdvSaleItemRequest(productId, BigDecimal.ONE, null)))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void addItemSavesSnapshotAndCalculatesTotal() {
        UUID saleId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        ProductEntity product = product(productId);
        OrderEntity sale = sale(saleId);
        when(orderRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderItemRepository.save(any(OrderItemEntity.class))).thenAnswer(invocation -> {
            OrderItemEntity item = invocation.getArgument(0);
            ReflectionTestUtils.setField(item, "id", UUID.randomUUID());
            return item;
        });
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderItemRepository.findByOrderIdOrderByCreatedAtAsc(saleId)).thenReturn(List.of());

        PdvSaleResponse response = service().addItem(
                saleId,
                new CreatePdvSaleItemRequest(productId, new BigDecimal("2.000"), " bem assado "));

        ArgumentCaptor<OrderItemEntity> captor = ArgumentCaptor.forClass(OrderItemEntity.class);
        verify(orderItemRepository).save(captor.capture());
        assertThat(captor.getValue().getProductNameSnapshot()).isEqualTo("Pao frances");
        assertThat(captor.getValue().getUnitPrice()).isEqualByComparingTo("1.20");
        assertThat(captor.getValue().getTotalPrice()).isEqualByComparingTo("2.40");
        assertThat(captor.getValue().getObservation()).isEqualTo("bem assado");
        assertThat(response.subtotal()).isEqualByComparingTo("2.40");
        assertThat(response.totalAmount()).isEqualByComparingTo("2.40");
    }

    @Test
    void updateQuantityRejectsZeroAndFinalizedSale() {
        UUID saleId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        OrderEntity sale = sale(saleId);
        when(orderRepository.findById(saleId)).thenReturn(Optional.of(sale));

        assertThatThrownBy(() -> service().updateItem(
                        saleId,
                        itemId,
                        new PatchPdvSaleItemRequest(BigDecimal.ZERO)))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);

        sale.changeStatus(OrderStatus.FINISHED);
        assertThatThrownBy(() -> service().updateItem(
                        saleId,
                        itemId,
                        new PatchPdvSaleItemRequest(BigDecimal.ONE)))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateQuantityRecalculatesTotals() {
        UUID saleId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        ProductEntity product = product(UUID.randomUUID());
        OrderEntity sale = sale(saleId);
        OrderItemEntity item = item(sale, product, itemId, BigDecimal.ONE);
        when(orderRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(orderItemRepository.findByIdAndOrderId(itemId, saleId)).thenReturn(Optional.of(item));
        when(orderItemRepository.save(item)).thenReturn(item);
        when(orderItemRepository.findByOrderIdOrderByCreatedAtAsc(saleId)).thenReturn(List.of(item));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PdvSaleResponse response = service().updateItem(
                saleId,
                itemId,
                new PatchPdvSaleItemRequest(new BigDecimal("3.000")));

        assertThat(item.getQuantity()).isEqualByComparingTo("3.000");
        assertThat(response.totalAmount()).isEqualByComparingTo("3.60");
    }

    @Test
    void removeItemReturnsErrorWhenMissingAndRecalculatesWhenFound() {
        UUID saleId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        OrderEntity sale = sale(saleId);
        ProductEntity product = product(UUID.randomUUID());
        OrderItemEntity item = item(sale, product, itemId, BigDecimal.ONE);
        when(orderRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(orderItemRepository.findByIdAndOrderId(itemId, saleId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service().removeItem(saleId, itemId))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.NOT_FOUND);

        when(orderItemRepository.findByOrderIdOrderByCreatedAtAsc(saleId)).thenReturn(List.of());
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PdvSaleResponse response = service().removeItem(saleId, itemId);

        verify(orderItemRepository).delete(item);
        assertThat(response.totalAmount()).isEqualByComparingTo("0.00");
    }

    private PdvSaleService service() {
        return new PdvSaleService(
                Optional.of(cashRegisterRepository),
                Optional.of(orderRepository),
                Optional.of(orderItemRepository),
                Optional.of(productRepository),
                Optional.of(productAvailabilityRepository));
    }

    private OrderEntity sale(UUID saleId) {
        OrderEntity sale = new OrderEntity(
                OrderOrigin.PDV,
                OrderStatus.CREATED,
                PaymentStatus.PENDING,
                DeliveryType.LOCAL_CONSUMPTION);
        ReflectionTestUtils.setField(sale, "id", saleId);
        return sale;
    }

    private ProductEntity product(UUID productId) {
        CategoryEntity category = new CategoryEntity("Paes");
        ReflectionTestUtils.setField(category, "id", UUID.randomUUID());
        ProductEntity product = new ProductEntity(
                category,
                "Pao frances",
                new BigDecimal("1.20"),
                UnitType.UNIT,
                PreparationSector.SEM_PREPARO);
        ReflectionTestUtils.setField(product, "id", productId);
        return product;
    }

    private OrderItemEntity item(OrderEntity sale, ProductEntity product, UUID itemId, BigDecimal quantity) {
        OrderItemEntity item = new OrderItemEntity(
                sale,
                product,
                product.getName(),
                quantity,
                product.getPrice(),
                product.getPrice().multiply(quantity),
                OrderItemStatus.CREATED,
                product.getPreparationSector());
        ReflectionTestUtils.setField(item, "id", itemId);
        return item;
    }
}
