package br.com.novaalianca.mnss.onlineapp.domain.order;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.onlineapp.domain.catalog.OnlineProductEntity;
import br.com.novaalianca.mnss.sharedinfra.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OnlineOrderItemEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OnlineOrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private OnlineProductEntity product;

    @Column(name = "product_name_snapshot", nullable = false, length = 150)
    private String productNameSnapshot;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    private String observation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderItemStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "preparation_sector", nullable = false, length = 60)
    private PreparationSector preparationSector;

    protected OnlineOrderItemEntity() {}

    public OnlineOrderItemEntity(OnlineProductEntity product, BigDecimal quantity, String observation) {
        if (product == null) throw new IllegalArgumentException("Product is required");
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Quantity must be greater than zero");

        this.product = product;
        this.productNameSnapshot = product.getName();
        this.quantity = quantity;
        this.unitPrice = product.getPromotionalPrice() != null ? product.getPromotionalPrice() : product.getPrice();
        this.totalPrice = this.unitPrice.multiply(quantity);
        this.observation = observation;
        this.status = OrderItemStatus.CREATED;
        this.preparationSector = product.getPreparationSector();
    }

    void setOrder(OnlineOrderEntity order) {
        this.order = order;
    }

    public OnlineOrderEntity getOrder() { return order; }
    public OnlineProductEntity getProduct() { return product; }
    public String getProductNameSnapshot() { return productNameSnapshot; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public String getObservation() { return observation; }
    public OrderItemStatus getStatus() { return status; }
    public PreparationSector getPreparationSector() { return preparationSector; }
}
