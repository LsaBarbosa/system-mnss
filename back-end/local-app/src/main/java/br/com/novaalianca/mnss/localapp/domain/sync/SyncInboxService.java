package br.com.novaalianca.mnss.localapp.domain.sync;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.core.payment.PaymentMethod;
import br.com.novaalianca.mnss.core.payment.PaymentStatus;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductEntity;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductRepository;
import br.com.novaalianca.mnss.localapp.domain.kds.KdsService;
import br.com.novaalianca.mnss.localapp.domain.order.*;
import br.com.novaalianca.mnss.localapp.domain.payment.PaymentEntity;
import br.com.novaalianca.mnss.localapp.domain.payment.PaymentRepository;
import br.com.novaalianca.mnss.sync.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SyncInboxService {
    private static final Logger log = LoggerFactory.getLogger(SyncInboxService.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final KdsService kdsService;
    private final SyncEventRepository syncEventRepository;

    public SyncInboxService(
        OrderRepository orderRepository,
        OrderItemRepository orderItemRepository,
        ProductRepository productRepository,
        PaymentRepository paymentRepository,
        KdsService kdsService,
        SyncEventRepository syncEventRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.paymentRepository = paymentRepository;
        this.kdsService = kdsService;
        this.syncEventRepository = syncEventRepository;
    }

    @Transactional
    public void processEvent(SyncEventDto dto) {
        log.info("Processing inbox event: {} - {} (ID: {})", dto.aggregateType(), dto.eventType(), dto.id());

        String localIdempotencyKey = "IN:" + dto.id();
        if (syncEventRepository.findByIdempotencyKey(localIdempotencyKey).isPresent()) {
            log.info("Event {} already received and processed locally", dto.id());
            return;
        }

        SyncEventEntity inboxEvent = new SyncEventEntity(
            localIdempotencyKey,
            SyncDirection.ONLINE_TO_LOCAL,
            dto.sourceEnvironment(),
            SyncEnvironment.LOCAL,
            dto.aggregateType(),
            dto.eventType(),
            dto.payload(),
            SyncEventStatus.RECEIVED_BY_STORE
        );
        inboxEvent.assignAggregateId(dto.aggregateId());
        syncEventRepository.save(inboxEvent);

        if ("ORDER".equals(dto.aggregateType())) {
            switch (dto.eventType()) {
                case "ORDER_CREATED" -> processOrderCreated(dto.payload());
                case "ORDER_UPDATED" -> processOrderUpdated(dto.payload());
                case "ORDER_PAID" -> processOrderPaid(dto.payload());
                case "ORDER_CANCELED" -> processOrderCanceled(dto.payload());
                default -> log.warn("Unhandled ORDER event type: {}", dto.eventType());
            }
        } else {
            log.warn("Unknown event for inbox: {} - {}", dto.aggregateType(), dto.eventType());
        }
    }

    private void processOrderCreated(Map<String, Object> payload) {
        UUID orderId = UUID.fromString((String) payload.get("orderId"));

        if (orderRepository.findById(orderId).isPresent()) {
            log.info("Order already exists local: {}", orderId);
            return;
        }

        OrderOrigin origin = payload.containsKey("origin")
            ? OrderOrigin.valueOf(payload.get("origin").toString())
            : OrderOrigin.SITE;

        OrderStatus status = OrderStatus.valueOf((String) payload.get("status"));
        PaymentStatus paymentStatus = PaymentStatus.valueOf((String) payload.get("paymentStatus"));
        DeliveryType deliveryType = DeliveryType.valueOf((String) payload.get("deliveryType"));

        OrderEntity order = new OrderEntity(origin, status, paymentStatus, deliveryType);
        order.assignId(orderId);

        if (payload.containsKey("notes")) {
            order.setNotes((String) payload.get("notes"));
        }

        order = orderRepository.save(order);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsData = payload.get("items") instanceof List<?> rawList
            ? (List<Map<String, Object>>) rawList
            : List.of();

        List<OrderItemEntity> items = new ArrayList<>();

        for (Map<String, Object> itemData : itemsData) {
            UUID productId = UUID.fromString((String) itemData.get("productId"));
            ProductEntity product = productRepository.findById(productId).orElse(null);

            BigDecimal quantity = new BigDecimal(itemData.get("quantity").toString());
            BigDecimal unitPrice = new BigDecimal(itemData.get("unitPrice").toString());
            BigDecimal totalPrice = new BigDecimal(itemData.get("totalPrice").toString());
            String productName = (String) itemData.get("productName");
            String observation = (String) itemData.get("observation");

            OrderItemEntity item = new OrderItemEntity(
                order,
                product,
                productName,
                quantity,
                unitPrice,
                totalPrice,
                OrderItemStatus.CREATED,
                product != null ? product.getPreparationSector() : PreparationSector.SEM_PREPARO,
                observation
            );

            items.add(orderItemRepository.save(item));
        }

        BigDecimal subtotal = money(payload.get("subtotal"));
        BigDecimal discount = money(payload.get("discountAmount"));
        BigDecimal deliveryFee = money(payload.get("deliveryFee"));
        BigDecimal total = money(payload.get("totalAmount"));

        order.updateTotals(subtotal, discount, deliveryFee, total);
        orderRepository.save(order);

        registerOnlinePaymentIfPaid(order, payload, total);

        kdsService.createTicketsForOrder(order, items);

        log.info("Online order processed locally: {} (Number: {})", order.getId(), payload.get("orderNumber"));
    }

    private void processOrderUpdated(Map<String, Object> payload) {
        UUID orderId = UUID.fromString((String) payload.get("orderId"));
        orderRepository.findById(orderId).ifPresentOrElse(order -> {
            if (payload.containsKey("status")) {
                OrderStatus newStatus = OrderStatus.valueOf((String) payload.get("status"));
                order.changeStatus(newStatus);
            }

            if (payload.containsKey("notes")) {
                order.setNotes((String) payload.get("notes"));
            }

            if (hasTotals(payload)) {
                order.updateTotals(
                    money(payload.get("subtotal")),
                    money(payload.get("discountAmount")),
                    money(payload.get("deliveryFee")),
                    money(payload.get("totalAmount"))
                );
            }

            orderRepository.save(order);
            log.info("Online order {} updated locally", orderId);
        }, () -> log.warn("ORDER_UPDATED: order {} not found locally — skipping", orderId));
    }

    private void processOrderPaid(Map<String, Object> payload) {
        UUID orderId = UUID.fromString((String) payload.get("orderId"));
        orderRepository.findById(orderId).ifPresentOrElse(order -> {
            order.markPaid(OrderStatus.PAID);
            orderRepository.save(order);

            registerOnlinePaymentIfPaid(order, payload, order.getTotalAmount());

            log.info("Online order {} marked as PAID locally", orderId);
        }, () -> log.warn("ORDER_PAID: order {} not found locally — skipping", orderId));
    }

    private void processOrderCanceled(Map<String, Object> payload) {
        UUID orderId = UUID.fromString((String) payload.get("orderId"));
        orderRepository.findById(orderId).ifPresentOrElse(order -> {
            order.changeStatus(OrderStatus.CANCELED);
            orderRepository.save(order);
            log.info("Online order {} canceled locally", orderId);
        }, () -> log.warn("ORDER_CANCELED: order {} not found locally — skipping", orderId));
    }

    private void registerOnlinePaymentIfPaid(
        OrderEntity order,
        Map<String, Object> payload,
        BigDecimal totalAmount) {

        Object paymentStatusValue = payload.get("paymentStatus");
        if (paymentStatusValue == null) {
            return;
        }

        PaymentStatus paymentStatus = PaymentStatus.valueOf(paymentStatusValue.toString());
        if (paymentStatus != PaymentStatus.PAID) {
            return;
        }

        if (paymentRepository.existsByOrderIdAndStatus(order.getId(), PaymentStatus.PAID)) {
            log.info("Paid payment already exists locally for online order {}", order.getId());
            return;
        }

        PaymentMethod method = PaymentMethod.ONLINE_PIX;
        Object paymentMethodValue = payload.get("paymentMethod");
        if (paymentMethodValue != null && !paymentMethodValue.toString().isBlank()) {
            method = PaymentMethod.valueOf(paymentMethodValue.toString());
        }

        PaymentEntity payment = new PaymentEntity(
            order,
            method,
            PaymentStatus.PAID,
            totalAmount
        );

        payment.markPaid(
            stringOrNull(payload.get("transactionId")),
            stringOrDefault(payload.get("gateway"), "ONLINE")
        );

        paymentRepository.save(payment);
    }

    private boolean hasTotals(Map<String, Object> payload) {
        return payload.containsKey("subtotal")
            && payload.containsKey("discountAmount")
            && payload.containsKey("deliveryFee")
            && payload.containsKey("totalAmount");
    }

    private BigDecimal money(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.toString());
    }

    private String stringOrNull(Object value) {
        return value == null || value.toString().isBlank() ? null : value.toString();
    }

    private String stringOrDefault(Object value, String defaultValue) {
        return value == null || value.toString().isBlank() ? defaultValue : value.toString();
    }
}
