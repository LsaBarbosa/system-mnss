package br.com.novaalianca.mnss.localapp.domain.stock;

import br.com.novaalianca.mnss.localapp.domain.catalog.ProductEntity;
import br.com.novaalianca.mnss.localapp.domain.order.OrderEntity;
import br.com.novaalianca.mnss.sharedinfra.domain.BaseEntity;
import br.com.novaalianca.mnss.localapp.domain.shared.DomainValidation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "stock_movements")
public class StockMovementEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StockMovementType type;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(precision = 12, scale = 3)
    private BigDecimal previousQuantity;

    @Column(precision = 12, scale = 3)
    private BigDecimal resultingQuantity;

    @Column(columnDefinition = "text")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    private UUID createdBy;

    @Column(length = 50)
    private String source;

    @Column(length = 50)
    private String referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(length = 255, unique = true)
    private String idempotencyKey;

    protected StockMovementEntity() {}

    public StockMovementEntity(ProductEntity product, StockMovementType type, BigDecimal quantity) {
        this.product = Objects.requireNonNull(product, "product must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.quantity = DomainValidation.requirePositive(quantity, "quantity");
    }

    public StockMovementEntity(
            ProductEntity product,
            StockMovementType type,
            BigDecimal quantity,
            String reason,
            OrderEntity order,
            UUID createdBy) {
        this(product, type, quantity);
        this.reason = reason;
        this.order = order;
        this.createdBy = createdBy;
    }

    public StockMovementEntity(
            ProductEntity product,
            StockMovementType type,
            BigDecimal quantity,
            String reason,
            OrderEntity order,
            UUID createdBy,
            BigDecimal previousQuantity,
            BigDecimal resultingQuantity) {
        this(product, type, quantity, reason, order, createdBy);
        this.previousQuantity = previousQuantity;
        this.resultingQuantity = resultingQuantity;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public StockMovementType getType() {
        return type;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPreviousQuantity() {
        return previousQuantity;
    }

    public BigDecimal getResultingQuantity() {
        return resultingQuantity;
    }

    public String getReason() {
        return reason;
    }

    public OrderEntity getOrder() {
        return order;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public String getSource() {
        return source;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}
