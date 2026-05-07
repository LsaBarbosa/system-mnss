package br.com.novaalianca.mnss.localapp.domain.stock;

import br.com.novaalianca.mnss.localapp.domain.catalog.ProductEntity;
import br.com.novaalianca.mnss.sharedinfra.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "stock_balances")
public class StockBalanceEntity extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal reservedQuantity;

    protected StockBalanceEntity() {}

    public StockBalanceEntity(ProductEntity product) {
        this.product = Objects.requireNonNull(product, "product must not be null");
        this.quantity = BigDecimal.ZERO;
        this.reservedQuantity = BigDecimal.ZERO;
    }

    public void adjust(BigDecimal delta) {
        this.quantity = this.quantity.add(delta);
        touch();
    }

    public BigDecimal getAvailableQuantity() {
        return quantity.subtract(reservedQuantity);
    }

    public ProductEntity getProduct() {
        return product;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getReservedQuantity() {
        return reservedQuantity;
    }
}
