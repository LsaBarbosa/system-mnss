package br.com.novaalianca.mnss.localapp.domain.pdv;

import br.com.novaalianca.mnss.core.catalog.SalesChannel;
import br.com.novaalianca.mnss.localapp.domain.audit.AuditLogRequest;
import br.com.novaalianca.mnss.localapp.domain.audit.AuditService;
import br.com.novaalianca.mnss.localapp.domain.cash.CashRegisterRepository;
import br.com.novaalianca.mnss.localapp.domain.cash.CashRegisterService;
import br.com.novaalianca.mnss.localapp.domain.cash.CashRegisterStatus;
import br.com.novaalianca.mnss.localapp.domain.catalog.AvailabilityStatus;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductAvailabilityRepository;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductEntity;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductRepository;
import br.com.novaalianca.mnss.localapp.domain.hardware.HardwareAdapterService;
import br.com.novaalianca.mnss.localapp.domain.kds.KdsService;
import br.com.novaalianca.mnss.localapp.domain.order.DeliveryType;
import br.com.novaalianca.mnss.localapp.domain.order.OrderEntity;
import br.com.novaalianca.mnss.localapp.domain.order.OrderItemEntity;
import br.com.novaalianca.mnss.localapp.domain.order.OrderItemRepository;
import br.com.novaalianca.mnss.localapp.domain.order.OrderItemStatus;
import br.com.novaalianca.mnss.localapp.domain.order.OrderOrigin;
import br.com.novaalianca.mnss.localapp.domain.order.OrderRepository;
import br.com.novaalianca.mnss.localapp.domain.order.OrderStatus;
import br.com.novaalianca.mnss.core.payment.PaymentStatus;
import br.com.novaalianca.mnss.core.payment.PaymentMethod;
import br.com.novaalianca.mnss.localapp.domain.payment.PaymentEntity;
import br.com.novaalianca.mnss.localapp.domain.payment.PaymentRepository;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncEventRepository;
import br.com.novaalianca.mnss.localapp.domain.stock.StockService;
import br.com.novaalianca.mnss.sync.*;
import br.com.novaalianca.mnss.localapp.security.user.RoleName;
import br.com.novaalianca.mnss.sharedinfra.web.error.BusinessException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PdvSaleService {
    private static final BigDecimal DEFAULT_QUANTITY = BigDecimal.ONE.setScale(3, RoundingMode.HALF_UP);

    private final CashRegisterRepository cashRegisterRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ProductAvailabilityRepository productAvailabilityRepository;
    private final PaymentRepository paymentRepository;
    private final SyncEventRepository syncEventRepository;
    private final PdvSyncEventService pdvSyncEventService;
    private final HardwareAdapterService hardwareAdapterService;
    private final CashRegisterService cashRegisterService;
    private final StockService stockService;
    private final AuditService auditService;
    private final KdsService kdsService;

    PdvSaleService(
            CashRegisterRepository cashRegisterRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository,
            ProductAvailabilityRepository productAvailabilityRepository,
            PaymentRepository paymentRepository,
            SyncEventRepository syncEventRepository,
            PdvSyncEventService pdvSyncEventService,
            HardwareAdapterService hardwareAdapterService,
            CashRegisterService cashRegisterService,
            StockService stockService,
            AuditService auditService,
            KdsService kdsService) {
        this.cashRegisterRepository = cashRegisterRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.productAvailabilityRepository = productAvailabilityRepository;
        this.paymentRepository = paymentRepository;
        this.syncEventRepository = syncEventRepository;
        this.pdvSyncEventService = pdvSyncEventService;
        this.hardwareAdapterService = hardwareAdapterService;
        this.cashRegisterService = cashRegisterService;
        this.stockService = stockService;
        this.auditService = auditService;
        this.kdsService = kdsService;
    }

    @Transactional
    public PdvSaleResponse createSale(UUID actorUserId) {
        requireOpenCashRegister(actorUserId);
        OrderEntity order = new OrderEntity(
                OrderOrigin.PDV,
                OrderStatus.CREATED,
                PaymentStatus.PENDING,
                DeliveryType.LOCAL_CONSUMPTION);
        order.updateTotals(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        OrderEntity saved = orderRepository.save(order);
        return response(saved);
    }

    @Transactional
    public PdvSaleResponse addItem(UUID saleId, CreatePdvSaleItemRequest request) {
        OrderEntity sale = editableSale(saleId);
        ProductEntity product = sellableProduct(request.productId());
        BigDecimal quantity = normalizeQuantity(request.quantity());
        BigDecimal unitPrice = effectivePrice(product);
        OrderItemEntity item = new OrderItemEntity(
                sale,
                product,
                product.getName(),
                quantity,
                unitPrice,
                money(unitPrice.multiply(quantity)),
                OrderItemStatus.CREATED,
                product.getPreparationSector(),
                request.observation());
        OrderItemEntity saved = orderItemRepository.save(item);
        recalculate(sale, appendCurrentItem(sale.getId(), saved));
        return response(sale);
    }

    @Transactional
    public PdvSaleResponse updateItem(UUID saleId, UUID itemId, PatchPdvSaleItemRequest request) {
        OrderEntity sale = editableSale(saleId);
        BigDecimal quantity = normalizeQuantity(request.quantity());
        OrderItemEntity item = orderItemRepository
                .findByIdAndOrderId(itemId, saleId)
                .orElseThrow(() -> notFound("SALE_ITEM_NOT_FOUND", "Item nao encontrado."));
        item.changeQuantity(quantity);
        orderItemRepository.save(item);
        recalculate(sale, orderItemRepository.findByOrderIdOrderByCreatedAtAsc(sale.getId()));
        return response(sale);
    }

    @Transactional
    public PdvSaleResponse removeItem(UUID saleId, UUID itemId) {
        OrderEntity sale = editableSale(saleId);
        OrderItemEntity item = orderItemRepository
                .findByIdAndOrderId(itemId, saleId)
                .orElseThrow(() -> notFound("SALE_ITEM_NOT_FOUND", "Item nao encontrado."));
        orderItemRepository.delete(item);
        recalculate(sale, orderItemRepository.findByOrderIdOrderByCreatedAtAsc(sale.getId()));
        return response(sale);
    }

    @Transactional
    public PdvSaleResponse finishSale(UUID saleId, UUID actorUserId) {
        OrderEntity sale = orderRepository
                .findById(saleId)
                .orElseThrow(() -> notFound("SALE_NOT_FOUND", "Venda nao encontrada."));
        
        if (sale.getStatus() != OrderStatus.CREATED) {
            return response(sale);
        }

        List<OrderItemEntity> items = orderItemRepository.findByOrderIdOrderByCreatedAtAsc(sale.getId());
        if (items.isEmpty()) {
            throw new BusinessException("EMPTY_SALE", "Venda sem itens nao pode ser finalizada.", HttpStatus.BAD_REQUEST);
        }

        List<PaymentEntity> payments = paymentRepository.findByOrderIdOrderByCreatedAtAsc(saleId);
        BigDecimal paidTotal = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .map(PaymentEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remainingAmount = sale.getTotalAmount().subtract(paidTotal);

        if (remainingAmount.signum() > 0) {
            throw new BusinessException("SALE_NOT_FULLY_PAID", "Venda nao esta totalmente paga.", HttpStatus.BAD_REQUEST);
        }

        sale.changeStatus(nextFinishedStatus(items));
        orderRepository.save(sale);

        pdvSyncEventService.recordOrderFinishedEvent(sale);

        if (payments.stream().anyMatch(p -> p.getMethod() == PaymentMethod.CASH)) {
            hardwareAdapterService.openDrawer();
        }

        hardwareAdapterService.printReceipt(sale, items, payments);
        
        kdsService.createTicketsForOrder(sale, items);

        // Baixar estoque
        for (OrderItemEntity item : items) {
            try {
                stockService.recordSaleMovement(item.getProduct().getId(), item.getQuantity(), sale.getId(), actorUserId);
            } catch (Exception e) {
                // Log and continue to avoid blocking the sale finishing if stock fails (monolith safety)
                // In a production environment with strict stock, we might want to block or handle differently.
            }
        }

        return response(sale);
    }

    @Transactional(readOnly = true)
    public void reprintReceipt(UUID saleId, UUID actorUserId) {
        OrderEntity sale = orderRepository
                .findById(saleId)
                .orElseThrow(() -> notFound("SALE_NOT_FOUND", "Venda nao encontrada."));
        
        List<OrderItemEntity> items = orderItemRepository.findByOrderIdOrderByCreatedAtAsc(saleId);
        List<PaymentEntity> payments = paymentRepository.findByOrderIdOrderByCreatedAtAsc(saleId);
        
        auditService.record(new AuditLogRequest(actorUserId, "RECEIPT_REPRINTED", "Order", saleId, Map.of(), null));
        hardwareAdapterService.printReceipt(sale, items, payments);
    }

    @Transactional
    public PdvSaleResponse applyDiscount(UUID saleId, CreateDiscountRequest request, UUID actorUserId, List<String> roles) {
        OrderEntity sale = editableSale(saleId);
        BigDecimal discountAmount = normalizeQuantity(request.amount());
        BigDecimal subtotal = sale.getSubtotal();

        BigDecimal limit = subtotal.multiply(new BigDecimal("0.10"));
        if (discountAmount.compareTo(limit) > 0) {
            if (!roles.contains(RoleName.GERENTE.name()) && !roles.contains(RoleName.ADMIN.name())) {
                throw new BusinessException("DISCOUNT_LIMIT_EXCEEDED", "Desconto acima de 10% exige permissao de gerente.", HttpStatus.FORBIDDEN);
            }
        }

        BigDecimal newTotal = subtotal.subtract(discountAmount).add(sale.getDeliveryFee());
        if (newTotal.signum() < 0) {
            throw new BusinessException("INVALID_DISCOUNT", "Desconto nao pode ser maior que o valor da venda.", HttpStatus.BAD_REQUEST);
        }

        sale.updateTotals(sale.getSubtotal(), discountAmount, sale.getDeliveryFee(), money(newTotal));
        OrderEntity saved = orderRepository.save(sale);

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("discountAmount", discountAmount.toPlainString());
        details.put("subtotal", subtotal.toPlainString());
        details.put("percentage", discountAmount.multiply(new BigDecimal("100")).divide(subtotal, 2, RoundingMode.HALF_UP).toPlainString());
        auditService.record(new AuditLogRequest(actorUserId, "SALE_DISCOUNT_APPLIED", "Order", saleId, details, null));

        return response(saved);
    }

    @Transactional
    public PdvSaleResponse cancelSale(UUID saleId, CancelSaleRequest request, UUID actorUserId, List<String> roles) {
        if (!roles.contains(RoleName.GERENTE.name()) && !roles.contains(RoleName.ADMIN.name())) {
            throw new BusinessException("CANCEL_SALE_FORBIDDEN", "Cancelamento de venda exige permissao de gerente.", HttpStatus.FORBIDDEN);
        }

        OrderEntity sale = orderRepository
                .findById(saleId)
                .orElseThrow(() -> notFound("SALE_NOT_FOUND", "Venda nao encontrada."));

        if (sale.getStatus() == OrderStatus.CANCELED) {
            throw new BusinessException("SALE_ALREADY_CANCELED", "Venda ja esta cancelada.", HttpStatus.BAD_REQUEST);
        }

        sale.changeStatus(OrderStatus.CANCELED);
        orderRepository.save(sale);

        List<PaymentEntity> payments = paymentRepository.findByOrderIdOrderByCreatedAtAsc(saleId);
        for (PaymentEntity payment : payments) {
            if (payment.getStatus() == PaymentStatus.PAID) {
                payment.markCanceled();
                paymentRepository.save(payment);
                
                cashRegisterRepository.findFirstByOperatorIdAndStatusOrderByOpenedAtDesc(actorUserId, CashRegisterStatus.OPEN)
                        .ifPresent(cash -> cashRegisterService.recordRefundMovement(
                                cash.getId(), payment.getMethod(), payment.getAmount(), saleId, actorUserId));
            }
        }

        List<OrderItemEntity> items = orderItemRepository.findByOrderIdOrderByCreatedAtAsc(saleId);
        for (OrderItemEntity item : items) {
            stockService.recordReturnMovement(item.getProduct().getId(), item.getQuantity(), saleId, actorUserId);
        }

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("reason", request.reason());
        auditService.record(new AuditLogRequest(actorUserId, "SALE_CANCELED", "Order", saleId, details, null));

        return response(sale);
    }

    @Transactional(readOnly = true)
    public PdvSaleResponse getSale(UUID saleId) {
        return response(orderRepository
                .findById(saleId)
                .orElseThrow(() -> notFound("SALE_NOT_FOUND", "Venda nao encontrada.")));
    }

    @Transactional(readOnly = true)
    public List<PdvSaleResponse> listSales() {
        return orderRepository.findByOriginOrderByCreatedAtDesc(OrderOrigin.PDV).stream()
                .map(this::response)
                .toList();
    }

    private List<OrderItemEntity> appendCurrentItem(UUID saleId, OrderItemEntity saved) {
        List<OrderItemEntity> items = orderItemRepository.findByOrderIdOrderByCreatedAtAsc(saleId);
        if (saved.getId() == null || items.stream().anyMatch(item -> saved.getId().equals(item.getId()))) {
            return items;
        }
        return java.util.stream.Stream.concat(items.stream(), java.util.stream.Stream.of(saved)).toList();
    }

    private void recalculate(OrderEntity sale, List<OrderItemEntity> items) {
        BigDecimal subtotal = items.stream()
                .map(item -> money(item.getTotalPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = subtotal
                .subtract(sale.getDiscountAmount())
                .add(sale.getDeliveryFee());
        sale.updateTotals(money(subtotal), money(sale.getDiscountAmount()), money(sale.getDeliveryFee()), money(total));
        orderRepository.save(sale);
    }

    private PdvSaleResponse response(OrderEntity order) {
        List<PdvSaleItemResponse> items = orderItemRepository.findByOrderIdOrderByCreatedAtAsc(order.getId()).stream()
                .map(PdvSaleItemResponse::from)
                .toList();
        List<PdvSalePaymentResponse> payments = paymentRepository.findByOrderIdOrderByCreatedAtAsc(order.getId()).stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .map(PdvSalePaymentResponse::from)
                .toList();
        
        SyncEventStatus syncStatus = syncEventRepository
                .findFirstByAggregateIdOrderByCreatedAtDesc(order.getId())
                .map(SyncEventEntity::getStatus)
                .orElse(null);

        return PdvSaleResponse.from(order, items, payments, syncStatus);
    }

    private OrderEntity editableSale(UUID saleId) {
        OrderEntity sale = orderRepository
                .findById(saleId)
                .orElseThrow(() -> notFound("SALE_NOT_FOUND", "Venda nao encontrada."));
        if (sale.getOrigin() != OrderOrigin.PDV || sale.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException("SALE_NOT_EDITABLE", "Venda finalizada nao pode ser alterada.", HttpStatus.BAD_REQUEST);
        }
        return sale;
    }

    private ProductEntity sellableProduct(UUID productId) {
        ProductEntity product = productRepository
                .findById(productId)
                .orElseThrow(() -> notFound("PRODUCT_NOT_FOUND", "Produto nao encontrado."));
        if (!isSellableOnPdv(product)) {
            throw new BusinessException("PRODUCT_NOT_SELLABLE", "Produto nao disponivel para venda.", HttpStatus.NOT_FOUND);
        }
        return product;
    }

    private boolean isSellableOnPdv(ProductEntity product) {
        return product.isActive()
                && product.isAvailable()
                && product.isSellOnPdv()
                && product.getCategory() != null
                && product.getCategory().isActive()
                && product.getCategory().isShowOnPdv()
                && !hasUnavailableStatus(product);
    }

    private boolean hasUnavailableStatus(ProductEntity product) {
        return latestAvailability(product.getId(), SalesChannel.ALL)
                .filter(availability -> availability.getStatus() == AvailabilityStatus.UNAVAILABLE)
                .isPresent()
                || latestAvailability(product.getId(), SalesChannel.PDV)
                        .filter(availability -> availability.getStatus() == AvailabilityStatus.UNAVAILABLE)
                        .isPresent();
    }

    private Optional<br.com.novaalianca.mnss.localapp.domain.catalog.ProductAvailabilityEntity> latestAvailability(
            UUID productId,
            SalesChannel channel) {
        Optional<br.com.novaalianca.mnss.localapp.domain.catalog.ProductAvailabilityEntity> availability =
                productAvailabilityRepository
                        .findFirstByProductIdAndChannelOrderByUpdatedAtDesc(productId, channel);
        return availability == null ? Optional.empty() : availability;
    }

    private void requireOpenCashRegister(UUID actorUserId) {
        if (actorUserId == null
                || !cashRegisterRepository.existsByOperatorIdAndStatus(actorUserId, CashRegisterStatus.OPEN)) {
            throw new BusinessException("OPEN_CASH_REGISTER_REQUIRED", "Abra o caixa antes de iniciar venda.", HttpStatus.BAD_REQUEST);
        }
    }

    private OrderStatus nextFinishedStatus(List<OrderItemEntity> items) {
        boolean requiresPreparation = items.stream()
                .anyMatch(i -> i.getPreparationSector() != br.com.novaalianca.mnss.core.catalog.PreparationSector.SEM_PREPARO);
        return requiresPreparation ? OrderStatus.ACCEPTED : OrderStatus.FINISHED;
    }

    private BigDecimal normalizeQuantity(BigDecimal quantity) {
        BigDecimal normalized = quantity == null ? DEFAULT_QUANTITY : quantity;
        if (normalized.signum() <= 0) {
            throw new BusinessException("INVALID_ITEM_QUANTITY", "Quantidade deve ser positiva.", HttpStatus.BAD_REQUEST);
        }
        return normalized.setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal effectivePrice(ProductEntity product) {
        return money(product.getPromotionalPrice() == null ? product.getPrice() : product.getPromotionalPrice());
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BusinessException notFound(String code, String message) {
        return new BusinessException(code, message, HttpStatus.NOT_FOUND);
    }
}
