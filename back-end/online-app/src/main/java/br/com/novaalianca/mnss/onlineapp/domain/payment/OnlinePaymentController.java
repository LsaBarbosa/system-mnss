package br.com.novaalianca.mnss.onlineapp.domain.payment;

import br.com.novaalianca.mnss.onlineapp.domain.payment.dto.OnlinePaymentRequest;
import br.com.novaalianca.mnss.onlineapp.domain.payment.dto.OnlinePaymentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/public/payments")
public class OnlinePaymentController {

    private final OnlinePaymentService paymentService;
    private final String webhookSecret = "replace_me_with_environment_variable";

    public OnlinePaymentController(OnlinePaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/online")
    public ResponseEntity<OnlinePaymentResponse> createPayment(@RequestBody OnlinePaymentRequest request) {
        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader(value = "X-Signature", required = false) String signature,
            @RequestBody Map<String, Object> payload) {
        
        // Simple signature validation (In production this would use HMAC-SHA256)
        if (signature == null || !signature.equals(webhookSecret)) {
            return ResponseEntity.status(401).build();
        }

        String transactionId = (String) payload.get("transactionId");
        String status = (String) payload.get("status");

        paymentService.processWebhook(transactionId, status, payload.toString());

        return ResponseEntity.ok().build();
    }
}
