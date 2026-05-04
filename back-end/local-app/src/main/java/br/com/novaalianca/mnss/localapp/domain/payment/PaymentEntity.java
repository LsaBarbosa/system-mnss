package br.com.novaalianca.mnss.localapp.domain.payment;

import br.com.novaalianca.mnss.localapp.domain.order.OrderEntity;
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
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "payments")
public class PaymentEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private PaymentStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 150)
    private String transactionId;

    @Column(length = 80)
    private String gateway;

    private Instant paidAt;

    private Instant canceledAt;

    protected PaymentEntity() {}

    public PaymentEntity(OrderEntity order, PaymentMethod method, PaymentStatus status, BigDecimal amount) {
        this.order = Objects.requireNonNull(order, "order must not be null");
        this.method = Objects.requireNonNull(method, "method must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.amount = DomainValidation.requireNonNegative(amount, "amount");
    }

    public OrderEntity getOrder() {
        return order;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getGateway() {
        return gateway;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public Instant getCanceledAt() {
        return canceledAt;
    }
}
