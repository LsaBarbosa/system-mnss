package br.com.novaalianca.mnss.onlineapp.domain.catalog;

import br.com.novaalianca.mnss.core.catalog.SalesChannel;
import br.com.novaalianca.mnss.sharedinfra.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "product_availability")
public class OnlineProductAvailabilityEntity extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false, length = 60)
    private String status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SalesChannel channel;

    protected OnlineProductAvailabilityEntity() {}

    public UUID getProductId() {
        return productId;
    }

    public String getStatus() {
        return status;
    }

    public SalesChannel getChannel() {
        return channel;
    }
}
