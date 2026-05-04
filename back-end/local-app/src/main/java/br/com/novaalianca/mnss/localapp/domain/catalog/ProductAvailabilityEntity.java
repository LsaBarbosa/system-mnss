package br.com.novaalianca.mnss.localapp.domain.catalog;

import br.com.novaalianca.mnss.core.catalog.SalesChannel;
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
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "product_availability")
public class ProductAvailabilityEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private AvailabilityStatus status;

    @Column(precision = 12, scale = 3)
    private BigDecimal availableQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SalesChannel channel;

    @Column(columnDefinition = "text")
    private String reason;

    private UUID updatedBy;

    protected ProductAvailabilityEntity() {}

    public ProductAvailabilityEntity(ProductEntity product, AvailabilityStatus status, SalesChannel channel) {
        this.product = Objects.requireNonNull(product, "product must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.channel = Objects.requireNonNull(channel, "channel must not be null");
    }

    public void update(
            AvailabilityStatus status,
            BigDecimal availableQuantity,
            String reason,
            UUID updatedBy) {
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.availableQuantity = DomainValidation.optionalNonNegative(availableQuantity, "availableQuantity");
        this.reason = reason;
        this.updatedBy = updatedBy;
        touch();
    }

    public void setAvailableQuantity(BigDecimal availableQuantity) {
        this.availableQuantity = DomainValidation.optionalNonNegative(availableQuantity, "availableQuantity");
    }

    public ProductEntity getProduct() {
        return product;
    }

    public AvailabilityStatus getStatus() {
        return status;
    }

    public BigDecimal getAvailableQuantity() {
        return availableQuantity;
    }

    public SalesChannel getChannel() {
        return channel;
    }

    public String getReason() {
        return reason;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }
}
