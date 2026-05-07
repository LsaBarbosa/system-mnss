package br.com.novaalianca.mnss.onlineapp.domain.payment;

import br.com.novaalianca.mnss.onlineapp.domain.payment.dto.OnlinePaymentRequest;
import br.com.novaalianca.mnss.onlineapp.domain.payment.dto.OnlinePaymentResponse;
import br.com.novaalianca.mnss.sharedinfra.security.HmacUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/public/payments")
public class OnlinePaymentController {
    private static final Logger log = LoggerFactory.getLogger(OnlinePaymentController.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final OnlinePaymentService paymentService;
    private final ObjectMapper objectMapper;
    private final String webhookSecret;

    public OnlinePaymentController(
            OnlinePaymentService paymentService,
            ObjectMapper objectMapper,
            @Value("${mnss.payment.webhook-secret}") String webhookSecret) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
        this.webhookSecret = webhookSecret;
    }

    @PostMapping("/online")
    public ResponseEntity<OnlinePaymentResponse> createPayment(@RequestBody OnlinePaymentRequest request) {
        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader(value = "X-Signature", required = false) String signature,
            @RequestBody String rawPayload) {

        if (signature == null || signature.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String normalizedSignature = normalizeSignature(signature);
        if (!HmacUtils.verifyHmac(rawPayload, normalizedSignature, webhookSecret)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> payload;
        try {
            payload = objectMapper.readValue(rawPayload, MAP_TYPE);
        } catch (Exception e) {
            log.warn("Invalid payment webhook payload", e);
            return ResponseEntity.badRequest().build();
        }

        String transactionId = stringValue(payload.get("transactionId"));
        String status = stringValue(payload.get("status"));
        if (transactionId == null || status == null) {
            return ResponseEntity.badRequest().build();
        }

        paymentService.processWebhook(transactionId, status, rawPayload);

        return ResponseEntity.ok().build();
    }

    private String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }

    private String normalizeSignature(String signature) {
        String trimmed = signature.trim();
        if (trimmed.startsWith("sha256=")) {
            return trimmed.substring("sha256=".length());
        }
        return trimmed;
    }
}
