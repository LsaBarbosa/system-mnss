package br.com.novaalianca.mnss.localapp.domain.kds;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.localapp.domain.order.OrderEntity;
import br.com.novaalianca.mnss.sharedinfra.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "kds_tickets")
public class KdsTicketEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(insertable = false, updatable = false)
    private Long ticketNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private PreparationSector sector;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private KdsTicketStatus status;

    private Instant startedAt;

    private Instant readyAt;

    private Instant finishedAt;

    @OneToMany(mappedBy = "kdsTicket", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<KdsTicketItemEntity> items = new LinkedHashSet<>();

    protected KdsTicketEntity() {}

    public KdsTicketEntity(OrderEntity order, PreparationSector sector, KdsTicketStatus status) {
        this.order = Objects.requireNonNull(order, "order must not be null");
        this.sector = Objects.requireNonNull(sector, "sector must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    public OrderEntity getOrder() {
        return order;
    }

    public Long getTicketNumber() {
        return ticketNumber;
    }

    public PreparationSector getSector() {
        return sector;
    }

    public KdsTicketStatus getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getReadyAt() {
        return readyAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void start() {
        this.status = KdsTicketStatus.IN_PREPARATION;
        this.startedAt = Instant.now();
        touch();
    }

    public void ready() {
        this.status = KdsTicketStatus.READY;
        this.readyAt = Instant.now();
        touch();
    }

    public void finish() {
        this.status = KdsTicketStatus.FINISHED;
        this.finishedAt = Instant.now();
        touch();
    }

    public Set<KdsTicketItemEntity> getItems() {
        return Set.copyOf(items);
    }
}
