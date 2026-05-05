package br.com.novaalianca.mnss.localapp.domain.sync;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.core.payment.PaymentStatus;
import br.com.novaalianca.mnss.localapp.domain.customer.CustomerEntity;
import br.com.novaalianca.mnss.localapp.domain.customer.CustomerRepository;
import br.com.novaalianca.mnss.localapp.domain.kds.KdsService;
import br.com.novaalianca.mnss.localapp.domain.order.*;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductEntity;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class SyncInboxService {
    private static final Logger log = LoggerFactory.getLogger(SyncInboxService.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final KdsService kdsService;
    private final ObjectMapper objectMapper;

    public SyncInboxService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            KdsService kdsService,
            ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.kdsService = kdsService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void processEvent(String aggregateType, String eventType, Map<String, Object> payload) {
        log.info("Processing inbox event: {} - {}", aggregateType, eventType);

        if ("ORDER".equals(aggregateType) && "ORDER_CREATED".equals(eventType)) {
            processOrderCreated(payload);
        } else {
            log.warn("Unknown event type for inbox: {} - {}", aggregateType, eventType);
        }
    }

    private void processOrderCreated(Map<String, Object> payload) {
        UUID orderId = UUID.fromString((String) payload.get("orderId"));
        
        // Idempotency check
        if (orderRepository.findById(orderId).isPresent()) {
            log.info("Order already exists local: {}", orderId);
            return;
        }

        OrderOrigin origin = OrderOrigin.SITE; // Default for online orders
        OrderStatus status = OrderStatus.valueOf((String) payload.get("status"));
        PaymentStatus paymentStatus = PaymentStatus.valueOf((String) payload.get("paymentStatus"));
        DeliveryType deliveryType = DeliveryType.valueOf((String) payload.get("deliveryType"));

        OrderEntity order = new OrderEntity(origin, status, paymentStatus, deliveryType);
        // Force ID to match online ID for easier tracking
        order.assignId(orderId);
        
        // Notes if present
        if (payload.containsKey("notes")) {
            // order.setNotes((String) payload.get("notes")); // Add notes field to OrderEntity if needed
        }

        order = orderRepository.save(order);

        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) payload.get("items");
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

        // Update order totals
        BigDecimal subtotal = new BigDecimal(payload.get("subtotal").toString());
        BigDecimal discount = new BigDecimal(payload.get("discountAmount").toString());
        BigDecimal deliveryFee = new BigDecimal(payload.get("deliveryFee").toString());
        BigDecimal total = new BigDecimal(payload.get("totalAmount").toString());
        order.updateTotals(subtotal, discount, deliveryFee, total);
        orderRepository.save(order);

        // KDS Integration
        kdsService.createTicketsForOrder(order, items);
        
        log.info("Online order processed locally: {} (Number: {})", order.getId(), payload.get("orderNumber"));
    }
}
