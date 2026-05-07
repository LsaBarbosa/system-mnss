package br.com.novaalianca.mnss.onlineapp.domain.payment;

import br.com.novaalianca.mnss.onlineapp.application.payment.PaymentGatewayPort;
import br.com.novaalianca.mnss.onlineapp.application.payment.PaymentGatewayPort.PaymentGatewayRequest;
import br.com.novaalianca.mnss.onlineapp.application.payment.PaymentGatewayPort.PaymentGatewayResponse;
import br.com.novaalianca.mnss.onlineapp.domain.order.OnlineOrderEntity;
import br.com.novaalianca.mnss.onlineapp.domain.order.OnlineOrderRepository;
import br.com.novaalianca.mnss.onlineapp.domain.order.OrderStatus;
import br.com.novaalianca.mnss.core.payment.PaymentStatus;
import br.com.novaalianca.mnss.onlineapp.domain.payment.dto.OnlinePaymentRequest;
import br.com.novaalianca.mnss.onlineapp.domain.payment.dto.OnlinePaymentResponse;
import br.com.novaalianca.mnss.onlineapp.domain.sync.SyncEventRepository;
import br.com.novaalianca.mnss.sync.SyncDirection;
import br.com.novaalianca.mnss.sync.SyncEnvironment;
import br.com.novaalianca.mnss.sync.SyncEventEntity;
import br.com.novaalianca.mnss.sync.SyncEventStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class OnlinePaymentService {

    private final OnlinePaymentRepository paymentRepository;
    private final OnlineOrderRepository orderRepository;
    private final PaymentGatewayPort paymentGatewayPort;
    private final SyncEventRepository syncEventRepository;
    @Value("${mnss.sync.default-store-id:store-001}")
    private String defaultStoreId = "store-001";

    public OnlinePaymentService(
            OnlinePaymentRepository paymentRepository,
            OnlineOrderRepository orderRepository,
            PaymentGatewayPort paymentGatewayPort,
            SyncEventRepository syncEventRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.paymentGatewayPort = paymentGatewayPort;
        this.syncEventRepository = syncEventRepository;
    }

    @Transactional
    public OnlinePaymentResponse createPayment(OnlinePaymentRequest request) {
        OnlineOrderEntity order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + request.orderId()));

        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            throw new IllegalStateException("Order is in an invalid status for payment: " + order.getStatus());
        }

        OnlinePaymentEntity payment = new OnlinePaymentEntity(
                order,
                request.method(),
                order.getTotalAmount(),
                "MockGateway"
        );

        PaymentGatewayResponse gatewayResponse = paymentGatewayPort.createPayment(new PaymentGatewayRequest(
                order.getId().toString(),
                order.getTotalAmount(),
                request.method(),
                order.getCustomer().getName(),
                order.getCustomer().getEmail(),
                order.getCustomer().getPhone()
        ));

        payment.registerGatewayReference(
                gatewayResponse.transactionId(),
                gatewayResponse.rawResponse() != null ? gatewayResponse.rawResponse().toString() : null);
        payment = paymentRepository.save(payment);

        order.updateStatus(OrderStatus.PAYMENT_PENDING);
        orderRepository.save(order);

        return new OnlinePaymentResponse(
                payment.getId(),
                order.getId(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getAmount(),
                gatewayResponse.transactionId(),
                gatewayResponse.qrCodeBase64(),
                gatewayResponse.qrCodeCopyPaste(),
                gatewayResponse.paymentUrl()
        );
    }

    @Transactional
    public void processWebhook(String transactionId, String status, String payload) {
        Optional<OnlinePaymentEntity> paymentOpt = paymentRepository.findByTransactionId(transactionId);
        
        // If not found by transactionId, we might need to find it by some other reference 
        // In this mock, we assume transactionId is known.
        if (paymentOpt.isEmpty()) {
            // Log and ignore or handle as new payment if possible
            return;
        }

        OnlinePaymentEntity payment = paymentOpt.get();
        if (payment.getStatus() == PaymentStatus.PAID) {
            // Already processed (Idempotency)
            return;
        }

        if ("PAID".equalsIgnoreCase(status)) {
            payment.markAsPaid(transactionId, payload);
            OnlineOrderEntity order = payment.getOrder();
            order.updateStatus(OrderStatus.SENT_TO_STORE);
            order.updatePaymentStatus(PaymentStatus.PAID);

            paymentRepository.save(payment);
            orderRepository.save(order);

            createSyncEvent(order);
        } else if ("REFUSED".equalsIgnoreCase(status)) {
            payment.markAsRefused(transactionId, payload);
            OnlineOrderEntity order = payment.getOrder();
            order.updatePaymentStatus(PaymentStatus.REFUSED);
            paymentRepository.save(payment);
            orderRepository.save(order);
        } else if ("EXPIRED".equalsIgnoreCase(status)) {
            payment.markAsExpired();
            OnlineOrderEntity order = payment.getOrder();
            order.updatePaymentStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);
            orderRepository.save(order);
        }
    }

    private void createSyncEvent(OnlineOrderEntity order) {
        String idempotencyKey = UUID.randomUUID().toString();
        
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("orderId", order.getId().toString());
        payload.put("orderNumber", order.getOrderNumber());
        payload.put("origin", order.getOrigin().name());
        payload.put("status", order.getStatus().name());
        payload.put("paymentStatus", order.getPaymentStatus().name());
        payload.put("deliveryType", order.getDeliveryType().name());
        payload.put("paymentMethod", order.getPaymentMethod().name());
        payload.put("storeId", defaultStoreId);
        payload.put("subtotal", order.getSubtotal());
        payload.put("discountAmount", order.getDiscountAmount());
        payload.put("deliveryFee", order.getDeliveryFee());
        payload.put("totalAmount", order.getTotalAmount());
        payload.put("notes", order.getNotes());
        
        java.util.List<Map<String, Object>> items = order.getItems().stream()
                .map(item -> {
                    Map<String, Object> itemMap = new java.util.LinkedHashMap<>();
                    itemMap.put("productId", item.getProduct().getId().toString());
                    itemMap.put("productName", item.getProductNameSnapshot());
                    itemMap.put("quantity", item.getQuantity());
                    itemMap.put("unitPrice", item.getUnitPrice());
                    itemMap.put("totalPrice", item.getTotalPrice());
                    itemMap.put("observation", item.getObservation());
                    return itemMap;
                }).toList();
        payload.put("items", items);

        SyncEventEntity event = new SyncEventEntity(
                idempotencyKey,
                SyncDirection.ONLINE_TO_LOCAL,
                SyncEnvironment.ONLINE,
                SyncEnvironment.LOCAL,
                "ORDER",
                "ORDER_CREATED",
                payload,
                SyncEventStatus.PENDING
        );
        event.assignAggregateId(order.getId());
        
        syncEventRepository.save(event);
    }
}
