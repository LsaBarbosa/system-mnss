package br.com.novaalianca.mnss.onlineapp.domain.order;

import br.com.novaalianca.mnss.onlineapp.domain.catalog.OnlineProductEntity;
import br.com.novaalianca.mnss.onlineapp.domain.catalog.OnlineProductRepository;
import br.com.novaalianca.mnss.onlineapp.domain.customer.OnlineCustomerAddressEntity;
import br.com.novaalianca.mnss.onlineapp.domain.customer.OnlineCustomerAddressRepository;
import br.com.novaalianca.mnss.onlineapp.domain.customer.OnlineCustomerEntity;
import br.com.novaalianca.mnss.onlineapp.domain.customer.OnlineCustomerRepository;
import br.com.novaalianca.mnss.onlineapp.domain.order.dto.CreateOnlineOrderRequest;
import br.com.novaalianca.mnss.onlineapp.domain.order.dto.OnlineOrderItemRequest;
import br.com.novaalianca.mnss.onlineapp.domain.order.dto.OnlineOrderResponse;
import br.com.novaalianca.mnss.core.payment.PaymentMethod;
import br.com.novaalianca.mnss.core.payment.PaymentStatus;
import br.com.novaalianca.mnss.onlineapp.domain.sync.SyncEventRepository;
import br.com.novaalianca.mnss.sync.SyncDirection;
import br.com.novaalianca.mnss.sync.SyncEnvironment;
import br.com.novaalianca.mnss.sync.SyncEventEntity;
import br.com.novaalianca.mnss.sync.SyncEventStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class OnlineOrderService {

    private final OnlineOrderRepository orderRepository;
    private final OnlineCustomerRepository customerRepository;
    private final OnlineCustomerAddressRepository addressRepository;
    private final OnlineProductRepository productRepository;
    private final SyncEventRepository syncEventRepository;

    public OnlineOrderService(
            OnlineOrderRepository orderRepository,
            OnlineCustomerRepository customerRepository,
            OnlineCustomerAddressRepository addressRepository,
            OnlineProductRepository productRepository,
            SyncEventRepository syncEventRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.productRepository = productRepository;
        this.syncEventRepository = syncEventRepository;
    }

    @Transactional
    public OnlineOrderResponse createOnlineOrder(CreateOnlineOrderRequest request) {
        OnlineCustomerEntity customer = resolveCustomer(request);
        
        if (request.deliveryType() == DeliveryType.DELIVERY && request.address() == null) {
            throw new IllegalArgumentException("Address is required for DELIVERY");
        }

        if (request.deliveryType() == DeliveryType.DELIVERY) {
            resolveAddress(request, customer);
        }

        OnlineOrderEntity order = new OnlineOrderEntity(
                customer,
                OrderOrigin.ONLINE,
                request.deliveryType(),
                request.paymentMethod(),
                request.notes()
        );

        for (OnlineOrderItemRequest itemRequest : request.items()) {
            OnlineProductEntity product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + itemRequest.productId()));

            if (!product.isActive() || !product.isSellOnline() || !product.isAvailable()) {
                throw new IllegalArgumentException("Product is not available for online sale: " + product.getName());
            }

            OnlineOrderItemEntity item = new OnlineOrderItemEntity(product, itemRequest.quantity(), itemRequest.observation());
            order.addItem(item);
        }

        // Determine status and sync based on payment method
        br.com.novaalianca.mnss.core.payment.PaymentMethod method = request.paymentMethod();
        boolean isOnlinePayment = method == br.com.novaalianca.mnss.core.payment.PaymentMethod.ONLINE_PIX 
                || method == br.com.novaalianca.mnss.core.payment.PaymentMethod.ONLINE_CREDIT_CARD 
                || method == br.com.novaalianca.mnss.core.payment.PaymentMethod.ONLINE_DEBIT_CARD;

        if (isOnlinePayment) {
            order.updateStatus(OrderStatus.PAYMENT_PENDING);
        } else {
            order.updateStatus(OrderStatus.SENT_TO_STORE);
        }

        order = orderRepository.save(order);

        if (!isOnlinePayment) {
            createSyncEvent(order);
        }

        return new OnlineOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getPaymentStatus(),
                order.getDeliveryType(),
                order.getSubtotal(),
                order.getDiscountAmount(),
                order.getDeliveryFee(),
                order.getTotalAmount(),
                order.getPaymentMethod()
        );
    }

    @Transactional(readOnly = true)
    public OnlineOrderResponse getOrder(UUID id) {
        OnlineOrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));

        return new OnlineOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getPaymentStatus(),
                order.getDeliveryType(),
                order.getSubtotal(),
                order.getDiscountAmount(),
                order.getDeliveryFee(),
                order.getTotalAmount(),
                order.getPaymentMethod()
        );
    }

    private OnlineCustomerEntity resolveCustomer(CreateOnlineOrderRequest request) {
        String phone = request.customer().phone();
        Optional<OnlineCustomerEntity> existingCustomer = customerRepository.findByPhone(phone);

        if (existingCustomer.isPresent()) {
            OnlineCustomerEntity customer = existingCustomer.get();
            customer.updateInfo(request.customer().name(), phone, request.customer().email());
            return customerRepository.save(customer);
        } else {
            OnlineCustomerEntity newCustomer = new OnlineCustomerEntity(
                    request.customer().name(),
                    phone,
                    request.customer().email()
            );
            return customerRepository.save(newCustomer);
        }
    }

    private OnlineCustomerAddressEntity resolveAddress(CreateOnlineOrderRequest request, OnlineCustomerEntity customer) {
        // Simple address creation for this sprint, ignoring exact match checks
        OnlineCustomerAddressEntity address = new OnlineCustomerAddressEntity(
                customer,
                request.address().street(),
                request.address().number(),
                request.address().neighborhood(),
                request.address().city(),
                request.address().state(),
                request.address().zipCode()
        );
        return addressRepository.save(address);
    }

    private void createSyncEvent(OnlineOrderEntity order) {
        String idempotencyKey = UUID.randomUUID().toString();
        
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("orderId", order.getId().toString());
        payload.put("orderNumber", order.getOrderNumber());
        payload.put("totalAmount", order.getTotalAmount());
        payload.put("deliveryType", order.getDeliveryType());
        
        java.util.List<Map<String, Object>> items = order.getItems().stream()
                .map(item -> {
                    Map<String, Object> itemMap = new java.util.LinkedHashMap<>();
                    itemMap.put("productId", item.getProduct().getId().toString());
                    itemMap.put("productName", item.getProductNameSnapshot());
                    itemMap.put("quantity", item.getQuantity());
                    itemMap.put("unitPrice", item.getUnitPrice());
                    itemMap.put("totalPrice", item.getTotalPrice());
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
