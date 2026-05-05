package br.com.novaalianca.mnss.localapp.domain.pdv;

import br.com.novaalianca.mnss.core.catalog.SalesChannel;
import br.com.novaalianca.mnss.localapp.domain.cash.CashRegisterRepository;
import br.com.novaalianca.mnss.localapp.domain.cash.CashRegisterStatus;
import br.com.novaalianca.mnss.localapp.domain.catalog.AvailabilityStatus;
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
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PdvSaleService {
    private static final BigDecimal DEFAULT_QUANTITY = BigDecimal.ONE.setScale(3, RoundingMode.HALF_UP);

    private final Optional<CashRegisterRepository> cashRegisterRepository;
    private final Optional<OrderRepository> orderRepository;
    private final Optional<OrderItemRepository> orderItemRepository;
    private final Optional<ProductRepository> productRepository;
    private final Optional<ProductAvailabilityRepository> productAvailabilityRepository;

    PdvSaleService(
            Optional<CashRegisterRepository> cashRegisterRepository,
            Optional<OrderRepository> orderRepository,
            Optional<OrderItemRepository> orderItemRepository,
            Optional<ProductRepository> productRepository,
            Optional<ProductAvailabilityRepository> productAvailabilityRepository) {
        this.cashRegisterRepository = cashRegisterRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.productAvailabilityRepository = productAvailabilityRepository;
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
        OrderEntity saved = orderRepository().save(order);
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
        OrderItemEntity saved = orderItemRepository().save(item);
        recalculate(sale, appendCurrentItem(sale.getId(), saved));
        return response(sale);
    }

    @Transactional
    public PdvSaleResponse updateItem(UUID saleId, UUID itemId, PatchPdvSaleItemRequest request) {
        OrderEntity sale = editableSale(saleId);
        BigDecimal quantity = normalizeQuantity(request.quantity());
        OrderItemEntity item = orderItemRepository()
                .findByIdAndOrderId(itemId, saleId)
                .orElseThrow(() -> notFound("SALE_ITEM_NOT_FOUND", "Item nao encontrado."));
        item.changeQuantity(quantity);
        orderItemRepository().save(item);
        recalculate(sale, orderItemRepository().findByOrderIdOrderByCreatedAtAsc(sale.getId()));
        return response(sale);
    }

    @Transactional
    public PdvSaleResponse removeItem(UUID saleId, UUID itemId) {
        OrderEntity sale = editableSale(saleId);
        OrderItemEntity item = orderItemRepository()
                .findByIdAndOrderId(itemId, saleId)
                .orElseThrow(() -> notFound("SALE_ITEM_NOT_FOUND", "Item nao encontrado."));
        orderItemRepository().delete(item);
        recalculate(sale, orderItemRepository().findByOrderIdOrderByCreatedAtAsc(sale.getId()));
        return response(sale);
    }

    @Transactional(readOnly = true)
    public PdvSaleResponse getSale(UUID saleId) {
        return response(orderRepository()
                .findById(saleId)
                .orElseThrow(() -> notFound("SALE_NOT_FOUND", "Venda nao encontrada.")));
    }

    @Transactional(readOnly = true)
    public List<PdvSaleResponse> listSales() {
        return orderRepository().findByOriginOrderByCreatedAtDesc(OrderOrigin.PDV).stream()
                .map(this::response)
                .toList();
    }

    private List<OrderItemEntity> appendCurrentItem(UUID saleId, OrderItemEntity saved) {
        List<OrderItemEntity> items = orderItemRepository().findByOrderIdOrderByCreatedAtAsc(saleId);
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
        orderRepository().save(sale);
    }

    private PdvSaleResponse response(OrderEntity order) {
        List<PdvSaleItemResponse> items = orderItemRepository().findByOrderIdOrderByCreatedAtAsc(order.getId()).stream()
                .map(PdvSaleItemResponse::from)
                .toList();
        return PdvSaleResponse.from(order, items);
    }

    private OrderEntity editableSale(UUID saleId) {
        OrderEntity sale = orderRepository()
                .findById(saleId)
                .orElseThrow(() -> notFound("SALE_NOT_FOUND", "Venda nao encontrada."));
        if (sale.getOrigin() != OrderOrigin.PDV || sale.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException("SALE_NOT_EDITABLE", "Venda finalizada nao pode ser alterada.", HttpStatus.BAD_REQUEST);
        }
        return sale;
    }

    private ProductEntity sellableProduct(UUID productId) {
        ProductEntity product = productRepository()
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
                productAvailabilityRepository()
                        .findFirstByProductIdAndChannelOrderByUpdatedAtDesc(productId, channel);
        return availability == null ? Optional.empty() : availability;
    }

    private void requireOpenCashRegister(UUID actorUserId) {
        if (actorUserId == null
                || !cashRegisterRepository().existsByOperatorIdAndStatus(actorUserId, CashRegisterStatus.OPEN)) {
            throw new BusinessException("OPEN_CASH_REGISTER_REQUIRED", "Abra o caixa antes de iniciar venda.", HttpStatus.BAD_REQUEST);
        }
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

    private CashRegisterRepository cashRegisterRepository() {
        return cashRegisterRepository
                .orElseThrow(() -> new IllegalStateException("Cash register repository is not available."));
    }

    private OrderRepository orderRepository() {
        return orderRepository.orElseThrow(() -> new IllegalStateException("Order repository is not available."));
    }

    private OrderItemRepository orderItemRepository() {
        return orderItemRepository
                .orElseThrow(() -> new IllegalStateException("Order item repository is not available."));
    }

    private ProductRepository productRepository() {
        return productRepository.orElseThrow(() -> new IllegalStateException("Product repository is not available."));
    }

    private ProductAvailabilityRepository productAvailabilityRepository() {
        return productAvailabilityRepository
                .orElseThrow(() -> new IllegalStateException("Product availability repository is not available."));
    }
}
