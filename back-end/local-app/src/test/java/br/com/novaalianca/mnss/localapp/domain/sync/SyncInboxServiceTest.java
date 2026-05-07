package br.com.novaalianca.mnss.localapp.domain.sync;

import br.com.novaalianca.mnss.core.payment.PaymentMethod;
import br.com.novaalianca.mnss.core.payment.PaymentStatus;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductRepository;
import br.com.novaalianca.mnss.localapp.domain.customer.CustomerAddressRepository;
import br.com.novaalianca.mnss.localapp.domain.kds.KdsService;
import br.com.novaalianca.mnss.localapp.domain.order.DeliveryType;
import br.com.novaalianca.mnss.localapp.domain.order.OrderEntity;
import br.com.novaalianca.mnss.localapp.domain.order.OrderItemRepository;
import br.com.novaalianca.mnss.localapp.domain.order.OrderOrigin;
import br.com.novaalianca.mnss.localapp.domain.order.OrderRepository;
import br.com.novaalianca.mnss.localapp.domain.order.OrderStatus;
import br.com.novaalianca.mnss.localapp.domain.payment.PaymentEntity;
import br.com.novaalianca.mnss.localapp.domain.payment.PaymentRepository;
import br.com.novaalianca.mnss.sync.SyncEventEntity;
import br.com.novaalianca.mnss.sync.SyncDirection;
import br.com.novaalianca.mnss.sync.SyncEnvironment;
import br.com.novaalianca.mnss.sync.SyncEventDto;
import br.com.novaalianca.mnss.sync.SyncEventStatus;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncInboxServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock OrderItemRepository orderItemRepository;
    @Mock ProductRepository productRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock CustomerAddressRepository addressRepository;
    @Mock KdsService kdsService;
    @Mock SyncEventRepository syncEventRepository;

    private SyncInboxService service;

    @BeforeEach
    void setUp() {
        service = new SyncInboxService(
                orderRepository, orderItemRepository, productRepository,
                paymentRepository, addressRepository, kdsService, syncEventRepository);
    }

    // ── Scenario 1: ORDER_CREATED origin=ONLINE → local origin=SITE ──────────

    @Test
    void orderCreated_originOnline_localOrderOriginIsSite() {
        assertOriginFor("ONLINE", OrderOrigin.SITE);
    }

    // ── Scenario 2: ORDER_CREATED origin=WHATSAPP → local origin=WHATSAPP ───

    @Test
    void orderCreated_originWhatsapp_localOrderOriginIsWhatsapp() {
        assertOriginFor("WHATSAPP", OrderOrigin.WHATSAPP);
    }

    // ── Scenario 3: ORDER_CREATED paid → PaymentEntity created ───────────────

    @Test
    void orderCreated_paidStatus_createsPaymentEntity() {
        UUID orderId = UUID.randomUUID();
        Map<String, Object> payload = minimalOrderPayload(orderId, "ONLINE");
        payload.put("paymentStatus", PaymentStatus.PAID.name());
        payload.put("paymentMethod", PaymentMethod.ONLINE_PIX.name());
        payload.put("transactionId", "txn-abc");
        payload.put("gateway", "MOCK");

        when(syncEventRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(syncEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentRepository.existsByOrderIdAndStatus(any(), eq(PaymentStatus.PAID))).thenReturn(false);
        ArgumentCaptor<PaymentEntity> payCaptor = ArgumentCaptor.forClass(PaymentEntity.class);
        when(paymentRepository.save(payCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.processEvent(orderCreatedDto(orderId, payload));

        assertThat(payCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payCaptor.getValue().getMethod()).isEqualTo(PaymentMethod.ONLINE_PIX);
    }

    // ── Scenario 4: Resend same event → no duplicate order or payment ─────────

    @Test
    void orderCreated_duplicateInboxEvent_skipsProcessing() {
        UUID orderId = UUID.randomUUID();
        when(syncEventRepository.findByIdempotencyKey(any()))
                .thenReturn(Optional.of(mock(SyncEventEntity.class)));

        service.processEvent(orderCreatedDto(orderId, minimalOrderPayload(orderId, "ONLINE")));

        verify(orderRepository, never()).save(any());
        verify(syncEventRepository, never()).save(any());
    }

    @Test
    void orderCreated_orderAlreadyExistsLocally_doesNotDuplicate() {
        UUID orderId = UUID.randomUUID();
        OrderEntity existing = new OrderEntity(
                OrderOrigin.SITE, OrderStatus.SENT_TO_STORE, PaymentStatus.PENDING, DeliveryType.PICKUP);

        when(syncEventRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(syncEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existing));

        service.processEvent(orderCreatedDto(orderId, minimalOrderPayload(orderId, "ONLINE")));

        verify(orderRepository, never()).save(any(OrderEntity.class));
    }

    // ── Scenario 5: ORDER_UPDATED with totals → totals updated ───────────────

    @Test
    void orderUpdated_withTotals_updatesTotalsOnOrder() {
        UUID orderId = UUID.randomUUID();
        OrderEntity existing = new OrderEntity(
                OrderOrigin.SITE, OrderStatus.SENT_TO_STORE, PaymentStatus.PENDING, DeliveryType.PICKUP);

        when(syncEventRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(syncEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existing));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", orderId.toString());
        payload.put("subtotal", "50.00");
        payload.put("discountAmount", "5.00");
        payload.put("deliveryFee", "3.50");
        payload.put("totalAmount", "48.50");

        service.processEvent(syncEventDto(orderId, "ORDER_UPDATED", payload));

        assertThat(existing.getSubtotal()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(existing.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(existing.getDeliveryFee()).isEqualByComparingTo(new BigDecimal("3.50"));
        assertThat(existing.getTotalAmount()).isEqualByComparingTo(new BigDecimal("48.50"));
    }

    // ── Scenario 6: ORDER_PAID → marks paid + creates payment ────────────────

    @Test
    void orderPaid_marksOrderAsPaidAndCreatesPayment() {
        UUID orderId = UUID.randomUUID();
        OrderEntity existing = new OrderEntity(
                OrderOrigin.SITE, OrderStatus.SENT_TO_STORE, PaymentStatus.PENDING, DeliveryType.PICKUP);

        when(syncEventRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(syncEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existing));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentRepository.existsByOrderIdAndStatus(any(), eq(PaymentStatus.PAID))).thenReturn(false);
        ArgumentCaptor<PaymentEntity> payCaptor = ArgumentCaptor.forClass(PaymentEntity.class);
        when(paymentRepository.save(payCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", orderId.toString());
        payload.put("paymentStatus", PaymentStatus.PAID.name());
        payload.put("paymentMethod", PaymentMethod.ONLINE_PIX.name());
        payload.put("totalAmount", "45.00");
        payload.put("transactionId", "txn-xyz");
        payload.put("gateway", "MOCK");

        service.processEvent(syncEventDto(orderId, "ORDER_PAID", payload));

        assertThat(existing.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(existing.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    void orderPaid_paymentAlreadyExists_doesNotDuplicatePayment() {
        UUID orderId = UUID.randomUUID();
        OrderEntity existing = new OrderEntity(
                OrderOrigin.SITE, OrderStatus.PAID, PaymentStatus.PAID, DeliveryType.PICKUP);

        when(syncEventRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(syncEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existing));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentRepository.existsByOrderIdAndStatus(any(), eq(PaymentStatus.PAID))).thenReturn(true);

        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", orderId.toString());
        payload.put("paymentStatus", PaymentStatus.PAID.name());
        payload.put("totalAmount", "45.00");

        service.processEvent(syncEventDto(orderId, "ORDER_PAID", payload));

        verify(paymentRepository, never()).save(any(PaymentEntity.class));
    }

    // ── parseOrderOrigin unit tests ───────────────────────────────────────────

    @Test
    void parseOrderOrigin_onlineMapsto_SITE() {
        assertOriginFor("ONLINE", OrderOrigin.SITE);
    }

    @Test
    void parseOrderOrigin_whatsappMapsto_WHATSAPP() {
        assertOriginFor("WHATSAPP", OrderOrigin.WHATSAPP);
    }

    @Test
    void parseOrderOrigin_nullMapsto_SITE() {
        assertOriginFor(null, OrderOrigin.SITE);
    }

    @Test
    void parseOrderOrigin_unknownValueMapsto_SITE() {
        assertOriginFor("UNKNOWN_FUTURE_VALUE", OrderOrigin.SITE);
    }

    @Test
    void parseOrderOrigin_pdvMapsto_PDV() {
        assertOriginFor("PDV", OrderOrigin.PDV);
    }

    @Test
    void parsePaymentMethod_validValue_returnsEnum() {
        assertPaymentMethodFor("CASH", PaymentMethod.CASH);
    }

    @Test
    void parsePaymentMethod_nullValue_returnsOnlinePix() {
        assertPaymentMethodFor(null, PaymentMethod.ONLINE_PIX);
    }

    @Test
    void parsePaymentMethod_unknownValue_fallsBackToOnlinePix() {
        assertPaymentMethodFor("CRYPTO_FUTURE", PaymentMethod.ONLINE_PIX);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void assertOriginFor(Object originValue, OrderOrigin expected) {
        UUID orderId = UUID.randomUUID();
        Map<String, Object> payload = minimalOrderPayload(orderId, originValue);

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        ArgumentCaptor<OrderEntity> captor = ArgumentCaptor.forClass(OrderEntity.class);
        when(orderRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));
        when(syncEventRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());

        service.processEvent(orderCreatedDto(orderId, payload));

        OrderEntity saved = captor.getAllValues().stream()
                .filter(e -> e.getOrigin() != null)
                .findFirst()
                .orElseThrow(() -> new AssertionError("OrderEntity not saved"));
        assertThat(saved.getOrigin()).isEqualTo(expected);
    }

    private void assertPaymentMethodFor(Object methodValue, PaymentMethod expected) {
        UUID orderId = UUID.randomUUID();
        Map<String, Object> payload = minimalOrderPayload(orderId, "ONLINE");
        payload.put("paymentStatus", PaymentStatus.PAID.name());
        if (methodValue != null) payload.put("paymentMethod", methodValue);

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(syncEventRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(paymentRepository.existsByOrderIdAndStatus(any(), any())).thenReturn(false);
        ArgumentCaptor<PaymentEntity> captor = ArgumentCaptor.forClass(PaymentEntity.class);
        when(paymentRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.processEvent(orderCreatedDto(orderId, payload));

        assertThat(captor.getValue().getMethod()).isEqualTo(expected);
    }

    private Map<String, Object> minimalOrderPayload(UUID orderId, Object origin) {
        Map<String, Object> p = new HashMap<>();
        p.put("orderId", orderId.toString());
        if (origin != null) p.put("origin", origin);
        p.put("status", OrderStatus.SENT_TO_STORE.name());
        p.put("paymentStatus", PaymentStatus.PENDING.name());
        p.put("deliveryType", DeliveryType.PICKUP.name());
        p.put("subtotal", BigDecimal.TEN);
        p.put("discountAmount", BigDecimal.ZERO);
        p.put("deliveryFee", BigDecimal.ZERO);
        p.put("totalAmount", BigDecimal.TEN);
        p.put("items", List.of());
        return p;
    }

    private SyncEventDto orderCreatedDto(UUID orderId, Map<String, Object> payload) {
        return syncEventDto(orderId, "ORDER_CREATED", payload);
    }

    private SyncEventDto syncEventDto(UUID aggregateId, String eventType, Map<String, Object> payload) {
        Instant now = Instant.now();
        return new SyncEventDto(
                UUID.randomUUID(),
                "test-key-" + UUID.randomUUID(),
                SyncDirection.ONLINE_TO_LOCAL,
                SyncEnvironment.ONLINE,
                SyncEnvironment.LOCAL,
                "ORDER",
                aggregateId,
                eventType,
                payload,
                SyncEventStatus.PENDING,
                0,
                null,
                null,
                null,
                now,
                now
        );
    }
}
