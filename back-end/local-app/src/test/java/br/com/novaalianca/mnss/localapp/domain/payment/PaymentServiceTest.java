package br.com.novaalianca.mnss.localapp.domain.payment;

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
import br.com.novaalianca.mnss.localapp.domain.cash.CashRegisterEntity;
import br.com.novaalianca.mnss.localapp.domain.cash.CashRegisterRepository;
import br.com.novaalianca.mnss.localapp.domain.cash.CashRegisterService;
import br.com.novaalianca.mnss.localapp.domain.cash.CashRegisterStatus;
import br.com.novaalianca.mnss.localapp.domain.catalog.CategoryEntity;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductEntity;
import br.com.novaalianca.mnss.localapp.domain.order.DeliveryType;
import br.com.novaalianca.mnss.localapp.domain.order.OrderEntity;
import br.com.novaalianca.mnss.localapp.domain.order.OrderItemEntity;
import br.com.novaalianca.mnss.localapp.domain.order.OrderItemRepository;
import br.com.novaalianca.mnss.localapp.domain.order.OrderItemStatus;
import br.com.novaalianca.mnss.localapp.domain.order.OrderOrigin;
import br.com.novaalianca.mnss.localapp.domain.order.OrderRepository;
import br.com.novaalianca.mnss.localapp.domain.order.OrderStatus;
import br.com.novaalianca.mnss.sharedinfra.web.error.BusinessException;
import br.com.novaalianca.mnss.core.payment.PaymentMethod;
import br.com.novaalianca.mnss.core.payment.PaymentStatus;
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
class PaymentServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CashRegisterRepository cashRegisterRepository;

    @Mock
    private CashRegisterService cashRegisterService;

    @Mock
    private AuditService auditService;

    @Test
    void paymentFinalizesSaleAndRecordsCashMovement() {
        UUID actorId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID cashRegisterId = UUID.randomUUID();
        ProductEntity product = product(UUID.randomUUID(), PreparationSector.SEM_PREPARO);
        OrderEntity order = sale(orderId, new BigDecimal("2.40"));
        OrderItemEntity item = item(order, product, new BigDecimal("2.000"));
        CashRegisterEntity cashRegister = cashRegister(actorId, cashRegisterId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderIdOrderByCreatedAtAsc(orderId)).thenReturn(List.of(item));
        when(paymentRepository.findByOrderIdOrderByCreatedAtAsc(orderId)).thenReturn(List.of());
        when(cashRegisterRepository.findFirstByOperatorIdAndStatusOrderByOpenedAtDesc(actorId, CashRegisterStatus.OPEN))
                .thenReturn(Optional.of(cashRegister));
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> {
            PaymentEntity payment = invocation.getArgument(0);
            ReflectionTestUtils.setField(payment, "id", UUID.randomUUID());
            return payment;
        });
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = service().payOrder(
                orderId,
                new CreatePaymentRequest(PaymentMethod.PIX, new BigDecimal("2.40"), "tx-123", "manual"),
                actorId);

        assertThat(response.status()).isEqualTo(PaymentStatus.PAID);
        assertThat(response.orderStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(response.orderPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(response.transactionId()).isEqualTo("tx-123");
        verify(cashRegisterService).recordSaleMovement(cashRegisterId, PaymentMethod.PIX, new BigDecimal("2.40"), orderId, actorId);
    }

    @Test
    void paymentSendsOrderToStoreWhenItemRequiresPreparation() {
        UUID actorId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        ProductEntity product = product(UUID.randomUUID(), PreparationSector.CHAPA);
        OrderEntity order = sale(orderId, new BigDecimal("10.00"));
        OrderItemEntity item = item(order, product, BigDecimal.ONE);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderIdOrderByCreatedAtAsc(orderId)).thenReturn(List.of(item));
        when(paymentRepository.findByOrderIdOrderByCreatedAtAsc(orderId)).thenReturn(List.of());
        when(cashRegisterRepository.findFirstByOperatorIdAndStatusOrderByOpenedAtDesc(actorId, CashRegisterStatus.OPEN))
                .thenReturn(Optional.of(cashRegister(actorId, UUID.randomUUID())));
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = service().payOrder(
                orderId,
                new CreatePaymentRequest(PaymentMethod.CREDIT_CARD, new BigDecimal("10.00"), null, null),
                actorId);

        assertThat(response.orderStatus()).isEqualTo(OrderStatus.SENT_TO_STORE);
    }

    @Test
    void paymentRejectsAmountMismatch() {
        UUID actorId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderEntity order = sale(orderId, new BigDecimal("9.90"));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderIdOrderByCreatedAtAsc(orderId))
                .thenReturn(List.of(item(order, product(UUID.randomUUID(), PreparationSector.SEM_PREPARO), BigDecimal.ONE)));
        when(paymentRepository.findByOrderIdOrderByCreatedAtAsc(orderId)).thenReturn(List.of());

        assertThatThrownBy(() -> service().payOrder(
                        orderId,
                        new CreatePaymentRequest(PaymentMethod.CASH, new BigDecimal("5.00"), null, null),
                        actorId))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void paymentRejectsAlreadyPaidOrder() {
        UUID actorId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderEntity order = sale(orderId, new BigDecimal("9.90"));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderIdOrderByCreatedAtAsc(orderId))
                .thenReturn(List.of(item(order, product(UUID.randomUUID(), PreparationSector.SEM_PREPARO), BigDecimal.ONE)));
        PaymentEntity pastPayment = new PaymentEntity(order, PaymentMethod.CASH, PaymentStatus.PAID, new BigDecimal("9.90"));
        when(paymentRepository.findByOrderIdOrderByCreatedAtAsc(orderId)).thenReturn(List.of(pastPayment));

        assertThatThrownBy(() -> service().payOrder(
                        orderId,
                        new CreatePaymentRequest(PaymentMethod.CASH, new BigDecimal("9.90"), null, null),
                        actorId))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void paymentRequiresOpenCashRegister() {
        UUID actorId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderEntity order = sale(orderId, new BigDecimal("9.90"));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderIdOrderByCreatedAtAsc(orderId))
                .thenReturn(List.of(item(order, product(UUID.randomUUID(), PreparationSector.SEM_PREPARO), BigDecimal.ONE)));
        when(paymentRepository.findByOrderIdOrderByCreatedAtAsc(orderId)).thenReturn(List.of());
        when(cashRegisterRepository.findFirstByOperatorIdAndStatusOrderByOpenedAtDesc(actorId, CashRegisterStatus.OPEN))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().payOrder(
                        orderId,
                        new CreatePaymentRequest(PaymentMethod.CASH, new BigDecimal("9.90"), null, null),
                        actorId))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void paymentRejectsEmptySale() {
        UUID actorId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(sale(orderId, BigDecimal.ZERO)));
        when(orderItemRepository.findByOrderIdOrderByCreatedAtAsc(orderId)).thenReturn(List.of());

        assertThatThrownBy(() -> service().payOrder(
                        orderId,
                        new CreatePaymentRequest(PaymentMethod.CASH, new BigDecimal("1.00"), null, null),
                        actorId))
                .isInstanceOf(BusinessException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void paymentPersistsPaymentDetails() {
        UUID actorId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderEntity order = sale(orderId, new BigDecimal("3.00"));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderIdOrderByCreatedAtAsc(orderId))
                .thenReturn(List.of(item(order, product(UUID.randomUUID(), PreparationSector.SEM_PREPARO), BigDecimal.ONE)));
        when(paymentRepository.findByOrderIdOrderByCreatedAtAsc(orderId)).thenReturn(List.of());
        when(cashRegisterRepository.findFirstByOperatorIdAndStatusOrderByOpenedAtDesc(actorId, CashRegisterStatus.OPEN))
                .thenReturn(Optional.of(cashRegister(actorId, UUID.randomUUID())));
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service().payOrder(
                orderId,
                new CreatePaymentRequest(PaymentMethod.DEBIT_CARD, new BigDecimal("3.00"), " auth ", " stone "),
                actorId);

        ArgumentCaptor<PaymentEntity> captor = ArgumentCaptor.forClass(PaymentEntity.class);
        verify(paymentRepository).save(captor.capture());
        assertThat(captor.getValue().getMethod()).isEqualTo(PaymentMethod.DEBIT_CARD);
        assertThat(captor.getValue().getTransactionId()).isEqualTo("auth");
        assertThat(captor.getValue().getGateway()).isEqualTo("stone");
        assertThat(captor.getValue().getPaidAt()).isNotNull();
        verify(auditService).record(any(AuditLogRequest.class));
    }

    @Test
    void paymentCalculatesChangeForCashMethod() {
        UUID actorId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderEntity order = sale(orderId, new BigDecimal("10.00"));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderIdOrderByCreatedAtAsc(orderId))
                .thenReturn(List.of(item(order, product(UUID.randomUUID(), PreparationSector.SEM_PREPARO), BigDecimal.ONE)));
        when(paymentRepository.findByOrderIdOrderByCreatedAtAsc(orderId)).thenReturn(List.of());
        when(cashRegisterRepository.findFirstByOperatorIdAndStatusOrderByOpenedAtDesc(actorId, CashRegisterStatus.OPEN))
                .thenReturn(Optional.of(cashRegister(actorId, UUID.randomUUID())));
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = service().payOrder(
                orderId,
                new CreatePaymentRequest(PaymentMethod.CASH, new BigDecimal("15.00"), null, null),
                actorId);

        assertThat(response.remainingAmount()).isEqualByComparingTo("0.00");
        assertThat(response.changeAmount()).isEqualByComparingTo("5.00");
        assertThat(response.recordedAmount()).isEqualByComparingTo("10.00");
    }

    @Test
    void paymentSupportsPartialPayments() {
        UUID actorId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderEntity order = sale(orderId, new BigDecimal("50.00"));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderIdOrderByCreatedAtAsc(orderId))
                .thenReturn(List.of(item(order, product(UUID.randomUUID(), PreparationSector.SEM_PREPARO), BigDecimal.ONE)));
        when(paymentRepository.findByOrderIdOrderByCreatedAtAsc(orderId)).thenReturn(List.of());
        when(cashRegisterRepository.findFirstByOperatorIdAndStatusOrderByOpenedAtDesc(actorId, CashRegisterStatus.OPEN))
                .thenReturn(Optional.of(cashRegister(actorId, UUID.randomUUID())));
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = service().payOrder(
                orderId,
                new CreatePaymentRequest(PaymentMethod.CREDIT_CARD, new BigDecimal("20.00"), null, null),
                actorId);

        assertThat(response.remainingAmount()).isEqualByComparingTo("30.00");
        assertThat(response.changeAmount()).isEqualByComparingTo("0.00");
        assertThat(response.recordedAmount()).isEqualByComparingTo("20.00");
        assertThat(response.orderStatus()).isEqualTo(OrderStatus.CREATED);
        verify(orderRepository, never()).save(any());
    }

    private PaymentService service() {
        return new PaymentService(
                Optional.of(orderRepository),
                Optional.of(orderItemRepository),
                Optional.of(paymentRepository),
                Optional.of(cashRegisterRepository),
                cashRegisterService,
                auditService);
    }

    private OrderEntity sale(UUID orderId, BigDecimal total) {
        OrderEntity order = new OrderEntity(
                OrderOrigin.PDV,
                OrderStatus.CREATED,
                PaymentStatus.PENDING,
                DeliveryType.LOCAL_CONSUMPTION);
        ReflectionTestUtils.setField(order, "id", orderId);
        order.updateTotals(total, BigDecimal.ZERO, BigDecimal.ZERO, total);
        return order;
    }

    private OrderItemEntity item(OrderEntity order, ProductEntity product, BigDecimal quantity) {
        return new OrderItemEntity(
                order,
                product,
                product.getName(),
                quantity,
                product.getPrice(),
                product.getPrice().multiply(quantity),
                OrderItemStatus.CREATED,
                product.getPreparationSector());
    }

    private ProductEntity product(UUID productId, PreparationSector preparationSector) {
        CategoryEntity category = new CategoryEntity("Categoria");
        ReflectionTestUtils.setField(category, "id", UUID.randomUUID());
        ProductEntity product = new ProductEntity(
                category,
                "Produto",
                new BigDecimal(preparationSector == PreparationSector.CHAPA ? "10.00" : "3.00"),
                UnitType.UNIT,
                preparationSector);
        ReflectionTestUtils.setField(product, "id", productId);
        return product;
    }

    private CashRegisterEntity cashRegister(UUID actorId, UUID cashRegisterId) {
        CashRegisterEntity cashRegister = new CashRegisterEntity(actorId, BigDecimal.ZERO, CashRegisterStatus.OPEN);
        ReflectionTestUtils.setField(cashRegister, "id", cashRegisterId);
        return cashRegister;
    }
}
