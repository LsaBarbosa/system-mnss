package br.com.novaalianca.mnss.onlineapp.domain.payment.dto;

import br.com.novaalianca.mnss.core.payment.PaymentMethod;
import br.com.novaalianca.mnss.core.payment.PaymentStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record OnlinePaymentResponse(
    UUID id,
    UUID orderId,
    PaymentMethod method,
    PaymentStatus status,
    BigDecimal amount,
    String transactionId,
    String qrCodeBase64,
    String qrCodeCopyPaste,
    String paymentUrl
) {}
