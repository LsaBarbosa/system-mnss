package br.com.novaalianca.mnss.onlineapp.infrastructure.payment;

import br.com.novaalianca.mnss.onlineapp.application.payment.PaymentGatewayPort;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.UUID;

@Component
public class MockPaymentGatewayAdapter implements PaymentGatewayPort {

    @Override
    public PaymentGatewayResponse createPayment(PaymentGatewayRequest request) {
        String transactionId = "mock_trx_" + UUID.randomUUID().toString().substring(0, 8);
        
        return new PaymentGatewayResponse(
                transactionId,
                "PENDING",
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==", // Mock base64
                "00020126580014br.gov.bcb.pix0136mock-pix-key-1234-5678-9012-3456",
                "https://payment.mock.com/pay/" + transactionId,
                Map.of("message", "Simulated payment created", "provider", "MockGateway")
        );
    }
}
