package br.com.novaalianca.mnss.onlineapp.domain.sync;

import br.com.novaalianca.mnss.onlineapp.domain.catalog.OnlineProductEntity;
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
public class OnlineStockBalanceEntity extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private OnlineProductEntity product;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal reservedQuantity;

    protected OnlineStockBalanceEntity() {}

    public OnlineStockBalanceEntity(OnlineProductEntity product) {
        this.product = Objects.requireNonNull(product, "product must not be null");
        this.quantity = BigDecimal.ZERO;
        this.reservedQuantity = BigDecimal.ZERO;
    }

    public void adjust(BigDecimal delta) {
        this.quantity = this.quantity.add(delta);
        touch();
    }

    public OnlineProductEntity getProduct() {
        return product;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getReservedQuantity() {
        return reservedQuantity;
    }

    public BigDecimal getAvailableQuantity() {
        return quantity.subtract(reservedQuantity);
    }
}
