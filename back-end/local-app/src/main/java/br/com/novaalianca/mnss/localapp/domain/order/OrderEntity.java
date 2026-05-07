package br.com.novaalianca.mnss.localapp.domain.order;

import br.com.novaalianca.mnss.localapp.domain.customer.CustomerAddressEntity;
import br.com.novaalianca.mnss.localapp.domain.customer.CustomerEntity;
import br.com.novaalianca.mnss.core.payment.PaymentStatus;
import br.com.novaalianca.mnss.sharedinfra.domain.BaseEntity;
import br.com.novaalianca.mnss.localapp.domain.shared.DomainValidation;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_address_id")
    private CustomerAddressEntity deliveryAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
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
        touch();
    }

    public void changeStatus(OrderStatus status) {
        this.status = Objects.requireNonNull(status, "status must not be null");
        touch();
    }

    public void markPaid(OrderStatus nextStatus) {
        if (nextStatus != OrderStatus.PAID && nextStatus != OrderStatus.SENT_TO_STORE) {
            throw new IllegalArgumentException("paid order status must be PAID or SENT_TO_STORE");
        }
        this.paymentStatus = PaymentStatus.PAID;
        this.status = nextStatus;
        touch();
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

    public void setNotes(String notes) {
        this.notes = notes;
        touch();
    }

    public String getNotes() {
        return notes;
    }

    public CustomerAddressEntity getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(CustomerAddressEntity deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
        touch();
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

    public void markAsReady() {
        this.status = OrderStatus.READY;
        touch();
    }

    public void finish() {
        this.status = OrderStatus.FINISHED;
        this.finishedAt = Instant.now();
        touch();
    }
}
