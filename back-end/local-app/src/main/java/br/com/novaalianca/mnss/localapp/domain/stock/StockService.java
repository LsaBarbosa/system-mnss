package br.com.novaalianca.mnss.localapp.domain.stock;

import br.com.novaalianca.mnss.localapp.domain.audit.AuditLogRequest;
import br.com.novaalianca.mnss.localapp.domain.audit.AuditService;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductEntity;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductRepository;
import br.com.novaalianca.mnss.localapp.domain.order.OrderEntity;
import br.com.novaalianca.mnss.localapp.domain.order.OrderRepository;
import br.com.novaalianca.mnss.sharedinfra.web.error.BusinessException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {
    private final Optional<ProductRepository> productRepository;
    private final Optional<OrderRepository> orderRepository;
    private final Optional<StockMovementRepository> stockMovementRepository;
    private final StockSyncEventService syncEventService;
    private final AuditService auditService;

    StockService(
            Optional<ProductRepository> productRepository,
            Optional<OrderRepository> orderRepository,
            Optional<StockMovementRepository> stockMovementRepository,
            StockSyncEventService syncEventService,
            AuditService auditService) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.syncEventService = syncEventService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> listMovements(UUID productId) {
        List<StockMovementEntity> movements = productId == null
                ? stockMovementRepository().findAllByOrderByCreatedAtDesc()
                : stockMovementRepository().findByProductIdOrderByCreatedAtDesc(productId);
        return movements.stream().map(StockMovementResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<StockBalanceResponse> listBalances() {
        List<ProductEntity> products = productRepository().findAllByOrderByNameAsc();
        List<UUID> productIds = products.stream().map(ProductEntity::getId).toList();
        Map<UUID, BigDecimal> balances = aggregateBalances(stockMovementRepository().findByProductIdIn(productIds));
        return products.stream()
                .map(product -> new StockBalanceResponse(
                        product.getId(),
                        product.getName(),
                        balances.getOrDefault(product.getId(), BigDecimal.ZERO)))
                .toList();
    }

    @Transactional
    public StockMovementResponse createMovement(CreateStockMovementRequest request, UUID actorUserId) {
        UUID productId = requireProductId(request.productId());
        StockMovementType type = requireType(request.type());
        BigDecimal quantity = requirePositiveQuantity(request.quantity());
        requireAuthenticatedUser(actorUserId);
        validateReason(type, request.reason());

        ProductEntity product = productRepository().findById(productId)
                .orElseThrow(() -> notFound("PRODUCT_NOT_FOUND", "Produto nao encontrado."));
        OrderEntity order = findOrder(request.orderId());
        BigDecimal balanceBefore = calculateBalance(productId);
        BigDecimal balanceAfter = balanceBefore.add(signedQuantity(type, quantity));
        if (isOutbound(type) && balanceAfter.signum() < 0) {
            throw new BusinessException("INSUFFICIENT_STOCK", "Estoque insuficiente para a movimentacao.", HttpStatus.BAD_REQUEST);
        }

        StockMovementEntity movement = new StockMovementEntity(
                product,
                type,
                quantity,
                normalizeReason(request.reason()),
                order,
                actorUserId);
        StockMovementEntity saved = stockMovementRepository().save(movement);
        syncEventService.recordStockMovementEvent(saved, balanceAfter);

        if (product.isStockControlled() && product.isAvailable() && balanceAfter.signum() <= 0) {
            product.changeAvailability(false);
            productRepository().save(product);
            syncEventService.recordProductAvailabilityEvent(product);
        }

        auditService.record(new AuditLogRequest(
                actorUserId,
                "STOCK_MOVEMENT_CREATED",
                "StockMovement",
                productId,
                movementDetails(saved, balanceBefore, balanceAfter),
                null));
        return StockMovementResponse.from(saved);
    }

    @Transactional
    public StockMovementResponse recordSaleMovement(UUID productId, BigDecimal quantity, UUID orderId, UUID actorUserId) {
        return createMovement(new CreateStockMovementRequest(
                productId,
                StockMovementType.SALE,
                quantity,
                "Venda finalizada",
                orderId), actorUserId);
    }

    @Transactional
    public StockMovementResponse recordReturnMovement(UUID productId, BigDecimal quantity, UUID orderId, UUID actorUserId) {
        return createMovement(new CreateStockMovementRequest(
                productId,
                StockMovementType.RETURN,
                quantity,
                "Devolucao de venda",
                orderId), actorUserId);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateBalance(UUID productId) {
        requireProductId(productId);
        return stockMovementRepository().findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(movement -> signedQuantity(movement.getType(), movement.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<UUID, BigDecimal> aggregateBalances(Collection<StockMovementEntity> movements) {
        return movements.stream()
                .collect(Collectors.groupingBy(
                        movement -> movement.getProduct().getId(),
                        LinkedHashMap::new,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                movement -> signedQuantity(movement.getType(), movement.getQuantity()),
                                BigDecimal::add)));
    }

    private Map<String, Object> movementDetails(
            StockMovementEntity movement,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("productId", movement.getProduct().getId().toString());
        details.put("type", movement.getType().name());
        details.put("quantity", movement.getQuantity().toPlainString());
        details.put("reason", movement.getReason());
        details.put("orderId", movement.getOrder() == null ? null : movement.getOrder().getId().toString());
        details.put("balanceBefore", balanceBefore.toPlainString());
        details.put("balanceAfter", balanceAfter.toPlainString());
        return details;
    }

    private BigDecimal signedQuantity(StockMovementType type, BigDecimal quantity) {
        return isOutbound(type) ? quantity.negate() : quantity;
    }

    private boolean isOutbound(StockMovementType type) {
        return switch (type) {
            case OUT, SALE, LOSS -> true;
            case IN, ADJUSTMENT, RETURN -> false;
        };
    }

    private void validateReason(StockMovementType type, String reason) {
        boolean reasonRequired = switch (type) {
            case ADJUSTMENT, LOSS, OUT -> true;
            case IN, SALE, RETURN -> false;
        };
        if (reasonRequired && (reason == null || reason.isBlank())) {
            throw new BusinessException("STOCK_REASON_REQUIRED", "Motivo obrigatorio para esta movimentacao.", HttpStatus.BAD_REQUEST);
        }
    }

    private UUID requireProductId(UUID productId) {
        if (productId == null) {
            throw new BusinessException("PRODUCT_REQUIRED", "Produto obrigatorio.", HttpStatus.BAD_REQUEST);
        }
        return productId;
    }

    private StockMovementType requireType(StockMovementType type) {
        if (type == null) {
            throw new BusinessException("STOCK_TYPE_REQUIRED", "Tipo de movimentacao obrigatorio.", HttpStatus.BAD_REQUEST);
        }
        return type;
    }

    private BigDecimal requirePositiveQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.signum() <= 0) {
            throw new BusinessException("INVALID_STOCK_QUANTITY", "Quantidade deve ser positiva.", HttpStatus.BAD_REQUEST);
        }
        return quantity;
    }

    private void requireAuthenticatedUser(UUID actorUserId) {
        if (actorUserId == null) {
            throw new BusinessException("USER_REQUIRED", "Usuario responsavel obrigatorio.", HttpStatus.BAD_REQUEST);
        }
    }

    private OrderEntity findOrder(UUID orderId) {
        if (orderId == null) {
            return null;
        }
        return orderRepository()
                .findById(orderId)
                .orElseThrow(() -> notFound("ORDER_NOT_FOUND", "Pedido nao encontrado."));
    }

    private String normalizeReason(String reason) {
        return reason == null || reason.isBlank() ? null : reason.trim();
    }

    private BusinessException notFound(String code, String message) {
        return new BusinessException(code, message, HttpStatus.NOT_FOUND);
    }

    private ProductRepository productRepository() {
        return productRepository.orElseThrow(() -> new IllegalStateException("Product repository is not available."));
    }

    private OrderRepository orderRepository() {
        return orderRepository.orElseThrow(() -> new IllegalStateException("Order repository is not available."));
    }

    private StockMovementRepository stockMovementRepository() {
        return stockMovementRepository
                .orElseThrow(() -> new IllegalStateException("Stock movement repository is not available."));
    }
}
