package br.com.novaalianca.mnss.onlineapp.domain.payment;

import br.com.novaalianca.mnss.core.payment.PaymentMethod;
import br.com.novaalianca.mnss.onlineapp.domain.order.OnlineOrderEntity;
import br.com.novaalianca.mnss.core.payment.PaymentStatus;
import br.com.novaalianca.mnss.sharedinfra.domain.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class OnlinePaymentEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OnlineOrderEntity order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PaymentStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_id", length = 150)
    private String transactionId;

    @Column(length = 80)
    private String gateway;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "webhook_payload", columnDefinition = "text")
    private String webhookPayload;

    protected OnlinePaymentEntity() {}

    public OnlinePaymentEntity(OnlineOrderEntity order, PaymentMethod method, BigDecimal amount, String gateway) {
        if (order == null) throw new IllegalArgumentException("Order is required");
        if (method == null) throw new IllegalArgumentException("Method is required");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        
        this.order = order;
        this.method = method;
        this.amount = amount;
        this.gateway = gateway;
        this.status = PaymentStatus.PENDING;
    }

    public void markAsPaid(String transactionId, String payload) {
        this.status = PaymentStatus.PAID;
        this.transactionId = transactionId;
        this.webhookPayload = payload;
        this.paidAt = LocalDateTime.now();
    }

    public void markAsRefused(String transactionId, String payload) {
        this.status = PaymentStatus.REFUSED;
        this.transactionId = transactionId;
        this.webhookPayload = payload;
    }

    public void markAsCanceled(String reason) {
        this.status = PaymentStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }

    public void markAsExpired() {
        this.status = PaymentStatus.EXPIRED;
    }

    public OnlineOrderEntity getOrder() { return order; }
    public PaymentMethod getMethod() { return method; }
    public PaymentStatus getStatus() { return status; }
    public BigDecimal getAmount() { return amount; }
    public String getTransactionId() { return transactionId; }
    public String getGateway() { return gateway; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public LocalDateTime getCanceledAt() { return canceledAt; }
    public String getWebhookPayload() { return webhookPayload; }
}
