package br.com.novaalianca.mnss.localapp.domain.cash;

import br.com.novaalianca.mnss.localapp.domain.payment.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CashMovementRequest(
        @NotNull CashMovementType type,
        PaymentMethod paymentMethod,
        @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal amount,
        String description,
        UUID orderId) {}
