package br.com.novaalianca.mnss.onlineapp.domain.order;

import br.com.novaalianca.mnss.onlineapp.domain.customer.OnlineCustomerEntity;
import br.com.novaalianca.mnss.core.payment.PaymentStatus;
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
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "orders")
public class OnlineOrderEntity extends BaseEntity {

    @Column(name = "order_number", insertable = false, updatable = false)
    private Long orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private OnlineCustomerEntity customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private OrderOrigin origin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 50)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", nullable = false, length = 50)
    private DeliveryType deliveryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 50)
    private br.com.novaalianca.mnss.core.payment.PaymentMethod paymentMethod;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "delivery_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    private String notes;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "canceled_at")
    private Instant canceledAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OnlineOrderItemEntity> items = new LinkedHashSet<>();

    protected OnlineOrderEntity() {}

    public OnlineOrderEntity(OnlineCustomerEntity customer, OrderOrigin origin, DeliveryType deliveryType, br.com.novaalianca.mnss.core.payment.PaymentMethod paymentMethod, String notes) {
        if (origin == null) throw new IllegalArgumentException("Origin is required");
        if (deliveryType == null) throw new IllegalArgumentException("DeliveryType is required");
        if (paymentMethod == null) throw new IllegalArgumentException("PaymentMethod is required");
        
        this.customer = customer;
        this.origin = origin;
        this.deliveryType = deliveryType;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
        this.status = OrderStatus.CREATED;
        this.paymentStatus = PaymentStatus.PENDING;
    }

    public void addItem(OnlineOrderItemEntity item) {
        items.add(item);
        item.setOrder(this);
        calculateTotals();
    }

    public void calculateTotals() {
        this.subtotal = items.stream()
                .map(OnlineOrderItemEntity::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalAmount = this.subtotal.subtract(this.discountAmount).add(this.deliveryFee);
    }

    public void updateStatus(OrderStatus newStatus) {
        if (newStatus == null) throw new IllegalArgumentException("Status is required");
        this.status = newStatus;
        if (newStatus == OrderStatus.FINISHED) {
            this.finishedAt = Instant.now();
        } else if (newStatus == OrderStatus.CANCELED) {
            this.canceledAt = Instant.now();
        }
    }

    public void updatePaymentStatus(PaymentStatus newPaymentStatus) {
        if (newPaymentStatus == null) throw new IllegalArgumentException("PaymentStatus is required");
        this.paymentStatus = newPaymentStatus;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee != null ? deliveryFee : BigDecimal.ZERO;
        calculateTotals();
    }

    public Long getOrderNumber() { return orderNumber; }
    public OnlineCustomerEntity getCustomer() { return customer; }
    public OrderOrigin getOrigin() { return origin; }
    public OrderStatus getStatus() { return status; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public DeliveryType getDeliveryType() { return deliveryType; }
    public br.com.novaalianca.mnss.core.payment.PaymentMethod getPaymentMethod() { return paymentMethod; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getNotes() { return notes; }
    public Instant getFinishedAt() { return finishedAt; }
    public Instant getCanceledAt() { return canceledAt; }
    public Set<OnlineOrderItemEntity> getItems() { return Set.copyOf(items); }
}
