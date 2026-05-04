package br.com.novaalianca.mnss.localapp.domain.cash;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CashRegisterResponse(
        UUID id,
        UUID operatorId,
        Instant openedAt,
        Instant closedAt,
        BigDecimal openingAmount,
        BigDecimal closingAmount,
        BigDecimal expectedAmount,
        BigDecimal differenceAmount,
        CashRegisterStatus status,
        String notes,
        Instant createdAt,
        Instant updatedAt) {
    static CashRegisterResponse from(CashRegisterEntity cashRegister) {
        return new CashRegisterResponse(
                cashRegister.getId(),
                cashRegister.getOperatorId(),
                cashRegister.getOpenedAt(),
                cashRegister.getClosedAt(),
                cashRegister.getOpeningAmount(),
                cashRegister.getClosingAmount(),
                cashRegister.getExpectedAmount(),
                cashRegister.getDifferenceAmount(),
                cashRegister.getStatus(),
                cashRegister.getNotes(),
                cashRegister.getCreatedAt(),
                cashRegister.getUpdatedAt());
    }
}
