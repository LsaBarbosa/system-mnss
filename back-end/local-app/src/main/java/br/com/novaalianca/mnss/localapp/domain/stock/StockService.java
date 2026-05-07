package br.com.novaalianca.mnss.localapp.domain.stock;

import br.com.novaalianca.mnss.localapp.domain.audit.AuditLogRequest;
import br.com.novaalianca.mnss.localapp.domain.audit.AuditService;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductEntity;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductRepository;
import br.com.novaalianca.mnss.localapp.domain.order.OrderEntity;
import br.com.novaalianca.mnss.localapp.domain.order.OrderRepository;
import br.com.novaalianca.mnss.sharedinfra.web.error.BusinessException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final StockSyncEventService syncEventService;
    private final AuditService auditService;

    StockService(
            ProductRepository productRepository,
            OrderRepository orderRepository,
            StockMovementRepository stockMovementRepository,
            StockBalanceRepository stockBalanceRepository,
            StockSyncEventService syncEventService,
            AuditService auditService) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.stockBalanceRepository = stockBalanceRepository;
        this.syncEventService = syncEventService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> listMovements(UUID productId) {
        PageRequest page = PageRequest.of(0, 500);
        List<StockMovementEntity> movements = productId == null
                ? stockMovementRepository.findAllByOrderByCreatedAtDesc(page)
                : stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId, page);
        return movements.stream().map(StockMovementResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<StockBalanceResponse> listBalances() {
        List<ProductEntity> products = productRepository.findAllByOrderByNameAsc(Pageable.unpaged());
        List<UUID> productIds = products.stream().map(ProductEntity::getId).toList();
        Map<UUID, StockBalanceEntity> balanceMap = stockBalanceRepository.findByProductIdIn(productIds).stream()
                .collect(Collectors.toMap(b -> b.getProduct().getId(), Function.identity()));
        return products.stream()
                .map(product -> {
                    StockBalanceEntity balance = balanceMap.get(product.getId());
                    BigDecimal qty = balance != null ? balance.getQuantity() : BigDecimal.ZERO;
                    BigDecimal reserved = balance != null ? balance.getReservedQuantity() : BigDecimal.ZERO;
                    return new StockBalanceResponse(product.getId(), product.getName(), qty, reserved);
                })
                .toList();
    }

    @Transactional
    public StockMovementResponse createMovement(CreateStockMovementRequest request, UUID actorUserId) {
        UUID productId = requireProductId(request.productId());
        StockMovementType type = requireType(request.type());
        BigDecimal quantity = requirePositiveQuantity(request.quantity());
        requireAuthenticatedUser(actorUserId);
        validateReason(type, request.reason());
        requireNotSaleType(type);

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> notFound("PRODUCT_NOT_FOUND", "Produto nao encontrado."));
        OrderEntity order = findOrder(request.orderId());

        BigDecimal delta = signedQuantity(type, quantity);

        StockBalanceEntity balance = stockBalanceRepository.findByProductIdForUpdate(productId)
                .orElseGet(() -> new StockBalanceEntity(product));
        BigDecimal previousQuantity = balance.getQuantity();
        BigDecimal resultingQuantity = previousQuantity.add(delta);

        if (product.isStockControlled() && isOutbound(type) && resultingQuantity.signum() < 0) {
            throw new BusinessException("INSUFFICIENT_STOCK", "Estoque insuficiente para a movimentacao.", HttpStatus.BAD_REQUEST);
        }

        balance.adjust(delta);
        stockBalanceRepository.save(balance);

        StockMovementEntity movement = new StockMovementEntity(
                product, type, quantity,
                normalizeReason(request.reason()), order, actorUserId,
                previousQuantity, resultingQuantity);
        StockMovementEntity saved = stockMovementRepository.save(movement);
        syncEventService.recordStockMovementEvent(saved, resultingQuantity);

        if (product.isStockControlled() && product.isAvailable() && resultingQuantity.signum() <= 0) {
            product.changeAvailability(false);
            productRepository.save(product);
            syncEventService.recordProductAvailabilityEvent(product);
        }

        auditService.record(new AuditLogRequest(
                actorUserId,
                "STOCK_MOVEMENT_CREATED",
                "StockMovement",
                productId,
                movementDetails(saved, previousQuantity, resultingQuantity),
                null));
        return StockMovementResponse.from(saved);
    }

    @Transactional
    public StockMovementResponse recordSaleMovement(UUID productId, BigDecimal quantity, UUID orderId, UUID actorUserId) {
        UUID pid = requireProductId(productId);
        BigDecimal qty = requirePositiveQuantity(quantity);

        ProductEntity product = productRepository.findById(pid)
                .orElseThrow(() -> notFound("PRODUCT_NOT_FOUND", "Produto nao encontrado."));
        OrderEntity order = findOrder(orderId);
        BigDecimal delta = qty.negate();

        StockBalanceEntity balance = stockBalanceRepository.findByProductIdForUpdate(pid)
                .orElseGet(() -> new StockBalanceEntity(product));
        BigDecimal previousQuantity = balance.getQuantity();
        BigDecimal resultingQuantity = previousQuantity.add(delta);

        if (product.isStockControlled() && resultingQuantity.signum() < 0) {
            throw new BusinessException("INSUFFICIENT_STOCK", "Estoque insuficiente para a movimentacao.", HttpStatus.BAD_REQUEST);
        }

        balance.adjust(delta);
        stockBalanceRepository.save(balance);

        StockMovementEntity movement = new StockMovementEntity(
                product, StockMovementType.SALE, qty,
                "Venda finalizada", order, actorUserId,
                previousQuantity, resultingQuantity);
        StockMovementEntity saved = stockMovementRepository.save(movement);
        syncEventService.recordStockMovementEvent(saved, resultingQuantity);

        if (product.isStockControlled() && product.isAvailable() && resultingQuantity.signum() <= 0) {
            product.changeAvailability(false);
            productRepository.save(product);
            syncEventService.recordProductAvailabilityEvent(product);
        }

        auditService.record(new AuditLogRequest(
                actorUserId, "STOCK_MOVEMENT_CREATED", "StockMovement",
                pid, movementDetails(saved, previousQuantity, resultingQuantity), null));
        return StockMovementResponse.from(saved);
    }

    @Transactional
    public StockMovementResponse recordReturnMovement(UUID productId, BigDecimal quantity, UUID orderId, UUID actorUserId) {
        UUID pid = requireProductId(productId);
        BigDecimal qty = requirePositiveQuantity(quantity);

        ProductEntity product = productRepository.findById(pid)
                .orElseThrow(() -> notFound("PRODUCT_NOT_FOUND", "Produto nao encontrado."));
        OrderEntity order = findOrder(orderId);

        StockBalanceEntity balance = stockBalanceRepository.findByProductIdForUpdate(pid)
                .orElseGet(() -> new StockBalanceEntity(product));
        BigDecimal previousQuantity = balance.getQuantity();
        BigDecimal resultingQuantity = previousQuantity.add(qty);

        balance.adjust(qty);
        stockBalanceRepository.save(balance);

        StockMovementEntity movement = new StockMovementEntity(
                product, StockMovementType.RETURN, qty,
                "Devolucao de venda", order, actorUserId,
                previousQuantity, resultingQuantity);
        StockMovementEntity saved = stockMovementRepository.save(movement);
        syncEventService.recordStockMovementEvent(saved, resultingQuantity);

        auditService.record(new AuditLogRequest(
                actorUserId, "STOCK_MOVEMENT_CREATED", "StockMovement",
                pid, movementDetails(saved, previousQuantity, resultingQuantity), null));
        return StockMovementResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateBalance(UUID productId) {
        requireProductId(productId);
        return stockBalanceRepository.findByProductId(productId)
                .map(StockBalanceEntity::getQuantity)
                .orElse(BigDecimal.ZERO);
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
            case OUT, SALE, LOSS, ADJUSTMENT_NEGATIVE -> true;
            case IN, ADJUSTMENT, ADJUSTMENT_POSITIVE, INVENTORY_COUNT, RETURN -> false;
        };
    }

    private void validateReason(StockMovementType type, String reason) {
        boolean reasonRequired = switch (type) {
            case ADJUSTMENT, ADJUSTMENT_NEGATIVE, ADJUSTMENT_POSITIVE, INVENTORY_COUNT, LOSS, OUT -> true;
            case IN, SALE, RETURN -> false;
        };
        if (reasonRequired && (reason == null || reason.isBlank())) {
            throw new BusinessException("STOCK_REASON_REQUIRED", "Motivo obrigatorio para esta movimentacao.", HttpStatus.BAD_REQUEST);
        }
    }

    private void requireNotSaleType(StockMovementType type) {
        if (type == StockMovementType.SALE) {
            throw new BusinessException("SALE_TYPE_NOT_ALLOWED", "Tipo SALE nao pode ser criado manualmente.", HttpStatus.BAD_REQUEST);
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
        return orderRepository
                .findById(orderId)
                .orElseThrow(() -> notFound("ORDER_NOT_FOUND", "Pedido nao encontrado."));
    }

    private String normalizeReason(String reason) {
        return reason == null || reason.isBlank() ? null : reason.trim();
    }

    private BusinessException notFound(String code, String message) {
        return new BusinessException(code, message, HttpStatus.NOT_FOUND);
    }
}
