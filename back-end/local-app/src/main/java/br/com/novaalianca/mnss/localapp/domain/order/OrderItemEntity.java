package br.com.novaalianca.mnss.localapp.domain.order;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductEntity;
import br.com.novaalianca.mnss.localapp.domain.shared.BaseEntity;
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
import java.math.RoundingMode;
import java.util.Objects;

@Entity
@Table(name = "order_items")
public class OrderItemEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @Column(nullable = false, length = 150)
    private String productNameSnapshot;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column(columnDefinition = "text")
    private String observation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderItemStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private PreparationSector preparationSector;

    protected OrderItemEntity() {}

    public OrderItemEntity(
            OrderEntity order,
            ProductEntity product,
            String productNameSnapshot,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice,
            OrderItemStatus status,
            PreparationSector preparationSector) {
        this(order, product, productNameSnapshot, quantity, unitPrice, totalPrice, status, preparationSector, null);
    }

    public OrderItemEntity(
            OrderEntity order,
            ProductEntity product,
            String productNameSnapshot,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice,
            OrderItemStatus status,
            PreparationSector preparationSector,
            String observation) {
        this.order = Objects.requireNonNull(order, "order must not be null");
        this.product = product;
        this.productNameSnapshot = DomainValidation.requireText(productNameSnapshot, "productNameSnapshot");
        this.quantity = DomainValidation.requirePositive(quantity, "quantity");
        this.unitPrice = DomainValidation.requireNonNegative(unitPrice, "unitPrice");
        this.totalPrice = DomainValidation.requireNonNegative(totalPrice, "totalPrice");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.preparationSector = Objects.requireNonNull(preparationSector, "preparationSector must not be null");
        this.observation = normalizeObservation(observation);
    }

    public void changeQuantity(BigDecimal quantity) {
        this.quantity = DomainValidation.requirePositive(quantity, "quantity");
        this.totalPrice = this.unitPrice.multiply(this.quantity).setScale(2, RoundingMode.HALF_UP);
        touch();
    }

    private String normalizeObservation(String observation) {
        if (observation == null || observation.isBlank()) {
            return null;
        }
        return observation.trim();
    }

    public OrderEntity getOrder() {
        return order;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public String getProductNameSnapshot() {
        return productNameSnapshot;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public String getObservation() {
        return observation;
    }

    public OrderItemStatus getStatus() {
        return status;
    }

    public PreparationSector getPreparationSector() {
        return preparationSector;
    }
}
