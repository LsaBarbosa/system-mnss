package br.com.novaalianca.mnss.localapp.domain.payment;

import jakarta.validation.constraints.DecimalMin;
import br.com.novaalianca.mnss.core.payment.PaymentMethod;
import br.com.novaalianca.mnss.core.payment.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotNull PaymentMethod method,
        @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal amount,
        String transactionId,
        String gateway) {}
