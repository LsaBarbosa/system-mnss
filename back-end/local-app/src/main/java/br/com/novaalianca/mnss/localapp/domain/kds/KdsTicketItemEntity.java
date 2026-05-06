package br.com.novaalianca.mnss.localapp.domain.kds;

import br.com.novaalianca.mnss.localapp.domain.order.OrderItemEntity;
import br.com.novaalianca.mnss.sharedinfra.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "kds_ticket_items")
public class KdsTicketItemEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kds_ticket_id", nullable = false)
    private KdsTicketEntity kdsTicket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItemEntity orderItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private KdsTicketStatus status;

    protected KdsTicketItemEntity() {}

    public KdsTicketItemEntity(KdsTicketEntity kdsTicket, OrderItemEntity orderItem, KdsTicketStatus status) {
        this.kdsTicket = Objects.requireNonNull(kdsTicket, "kdsTicket must not be null");
        this.orderItem = Objects.requireNonNull(orderItem, "orderItem must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    public void ready() {
        this.status = KdsTicketStatus.READY;
        touch();
    }

    public KdsTicketEntity getKdsTicket() {
        return kdsTicket;
    }

    public OrderItemEntity getOrderItem() {
        return orderItem;
    }

    public KdsTicketStatus getStatus() {
        return status;
    }
}
