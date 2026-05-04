package br.com.novaalianca.mnss.localapp.domain.order;

import br.com.novaalianca.mnss.localapp.domain.customer.CustomerEntity;
import br.com.novaalianca.mnss.localapp.domain.payment.PaymentStatus;
import br.com.novaalianca.mnss.localapp.domain.shared.BaseEntity;
import br.com.novaalianca.mnss.localapp.domain.shared.DomainValidation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "orders")
public class OrderEntity extends BaseEntity {
    @Column(insertable = false, updatable = false)
    private Long orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private OrderOrigin origin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DeliveryType deliveryType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(columnDefinition = "text")
    private String notes;

    private Instant finishedAt;

    private Instant canceledAt;

    @OneToMany(mappedBy = "order")
    private Set<OrderItemEntity> items = new LinkedHashSet<>();

    protected OrderEntity() {}

    public OrderEntity(OrderOrigin origin, OrderStatus status, PaymentStatus paymentStatus, DeliveryType deliveryType) {
        this.origin = Objects.requireNonNull(origin, "origin must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.paymentStatus = Objects.requireNonNull(paymentStatus, "paymentStatus must not be null");
        this.deliveryType = Objects.requireNonNull(deliveryType, "deliveryType must not be null");
    }

    public void updateTotals(
            BigDecimal subtotal,
            BigDecimal discountAmount,
            BigDecimal deliveryFee,
            BigDecimal totalAmount) {
        this.subtotal = DomainValidation.requireNonNegative(subtotal, "subtotal");
        this.discountAmount = DomainValidation.requireNonNegative(discountAmount, "discountAmount");
        this.deliveryFee = DomainValidation.requireNonNegative(deliveryFee, "deliveryFee");
        this.totalAmount = DomainValidation.requireNonNegative(totalAmount, "totalAmount");
    }

    public Long getOrderNumber() {
        return orderNumber;
    }

    public CustomerEntity getCustomer() {
        return customer;
    }

    public OrderOrigin getOrigin() {
        return origin;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public Instant getCanceledAt() {
        return canceledAt;
    }

    public Set<OrderItemEntity> getItems() {
        return Set.copyOf(items);
    }
}
