package br.com.novaalianca.mnss.localapp.domain.cash;

import br.com.novaalianca.mnss.localapp.domain.order.OrderEntity;
import br.com.novaalianca.mnss.core.payment.PaymentMethod;
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
@Table(name = "cash_movements")
public class CashMovementEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cash_register_id", nullable = false)
    private CashRegisterEntity cashRegister;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CashMovementType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 60)
    private PaymentMethod paymentMethod;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(columnDefinition = "text")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    private UUID createdBy;

    protected CashMovementEntity() {}

    public CashMovementEntity(CashRegisterEntity cashRegister, CashMovementType type, BigDecimal amount) {
        this.cashRegister = Objects.requireNonNull(cashRegister, "cashRegister must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.amount = DomainValidation.requireNonNegative(amount, "amount");
    }

    public CashMovementEntity(
            CashRegisterEntity cashRegister,
            CashMovementType type,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            String description,
            OrderEntity order,
            UUID createdBy) {
        this(cashRegister, type, amount);
        this.paymentMethod = paymentMethod;
        this.description = description;
        this.order = order;
        this.createdBy = createdBy;
    }

    public CashRegisterEntity getCashRegister() {
        return cashRegister;
    }

    public CashMovementType getType() {
        return type;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public OrderEntity getOrder() {
        return order;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }
}
