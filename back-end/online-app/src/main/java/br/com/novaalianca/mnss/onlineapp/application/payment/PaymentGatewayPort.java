package br.com.novaalianca.mnss.onlineapp.application.payment;

import br.com.novaalianca.mnss.core.payment.PaymentMethod;
import java.math.BigDecimal;
import java.util.Map;

public interface PaymentGatewayPort {
    
    PaymentGatewayResponse createPayment(PaymentGatewayRequest request);

    record PaymentGatewayRequest(
            String externalOrderId,
            BigDecimal amount,
            PaymentMethod method,
            String customerName,
            String customerEmail,
            String customerPhone
    ) {}

    record PaymentGatewayResponse(
            String transactionId,
            String paymentStatus,
            String qrCodeBase64,
            String qrCodeCopyPaste,
            String paymentUrl,
            Map<String, Object> rawResponse
    ) {}
}
