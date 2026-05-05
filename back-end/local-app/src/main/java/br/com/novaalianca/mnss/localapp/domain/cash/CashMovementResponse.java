package br.com.novaalianca.mnss.localapp.domain.cash;

import br.com.novaalianca.mnss.core.payment.PaymentMethod;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CashMovementResponse(
        UUID id,
        UUID cashRegisterId,
        CashMovementType type,
        PaymentMethod paymentMethod,
        BigDecimal amount,
        String description,
        UUID orderId,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt) {
    static CashMovementResponse from(CashMovementEntity movement) {
        return new CashMovementResponse(
                movement.getId(),
                movement.getCashRegister().getId(),
                movement.getType(),
                movement.getPaymentMethod(),
                movement.getAmount(),
                movement.getDescription(),
                movement.getOrder() == null ? null : movement.getOrder().getId(),
                movement.getCreatedBy(),
                movement.getCreatedAt(),
                movement.getUpdatedAt());
    }
}
