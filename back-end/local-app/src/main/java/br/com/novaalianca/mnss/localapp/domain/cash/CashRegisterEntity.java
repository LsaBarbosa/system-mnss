package br.com.novaalianca.mnss.localapp.domain.cash;

import br.com.novaalianca.mnss.sharedinfra.domain.BaseEntity;
import br.com.novaalianca.mnss.localapp.domain.shared.DomainValidation;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "cash_registers")
public class CashRegisterEntity extends BaseEntity {
    @Column(nullable = false)
    private UUID operatorId;

    @Column(nullable = false)
    private Instant openedAt = Instant.now();

    private Instant closedAt;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal openingAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal closingAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal expectedAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal differenceAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CashRegisterStatus status;

    @Column(columnDefinition = "text")
    private String notes;

    @OneToMany(mappedBy = "cashRegister", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CashMovementEntity> movements = new LinkedHashSet<>();

    protected CashRegisterEntity() {}

    public CashRegisterEntity(UUID operatorId, BigDecimal openingAmount, CashRegisterStatus status) {
        this.operatorId = Objects.requireNonNull(operatorId, "operatorId must not be null");
        this.openingAmount = DomainValidation.requireNonNegative(openingAmount, "openingAmount");
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    public void updateNotes(String notes) {
        this.notes = notes;
        touch();
    }

    public void close(BigDecimal closingAmount, BigDecimal expectedAmount, BigDecimal differenceAmount, String notes) {
        if (status != CashRegisterStatus.OPEN) {
            throw new IllegalStateException("Cash register is not open.");
        }
        this.closingAmount = DomainValidation.requireNonNegative(closingAmount, "closingAmount");
        this.expectedAmount = DomainValidation.requireNonNegative(expectedAmount, "expectedAmount");
        this.differenceAmount = Objects.requireNonNull(differenceAmount, "differenceAmount must not be null");
        this.notes = notes;
        this.closedAt = Instant.now();
        this.status = CashRegisterStatus.CLOSED;
        touch();
    }

    public UUID getOperatorId() {
        return operatorId;
    }

    public Instant getOpenedAt() {
        return openedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public BigDecimal getOpeningAmount() {
        return openingAmount;
    }

    public BigDecimal getClosingAmount() {
        return closingAmount;
    }

    public BigDecimal getExpectedAmount() {
        return expectedAmount;
    }

    public BigDecimal getDifferenceAmount() {
        return differenceAmount;
    }

    public CashRegisterStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public Set<CashMovementEntity> getMovements() {
        return Set.copyOf(movements);
    }
}
