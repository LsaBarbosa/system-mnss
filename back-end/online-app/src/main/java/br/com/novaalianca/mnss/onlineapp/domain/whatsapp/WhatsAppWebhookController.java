package br.com.novaalianca.mnss.onlineapp.domain.whatsapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Public webhook controller for receiving WhatsApp messages from the provider.
 * No JWT authentication — validates via verify token and provider signature.
 * Responds quickly (200 OK) as required by DO §18.
 */
@RestController
@RequestMapping("/api/whatsapp/webhook")
public class WhatsAppWebhookController {
    private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookController.class);

    private final WhatsAppService whatsAppService;
    private final WhatsAppProviderPort providerPort;

    @Value("${mnss.whatsapp.verify-token:change_me}")
    private String verifyToken;

    public WhatsAppWebhookController(WhatsAppService whatsAppService, WhatsAppProviderPort providerPort) {
        this.whatsAppService = whatsAppService;
        this.providerPort = providerPort;
    }

    /**
     * Webhook verification endpoint (GET).
     * The provider sends a challenge token that must be echoed back.
     */
    @GetMapping
    public ResponseEntity<String> verify(
            @RequestParam(value = "hub.mode", required = false) String mode,
            @RequestParam(value = "hub.verify_token", required = false) String token,
            @RequestParam(value = "hub.challenge", required = false) String challenge) {

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            log.info("WhatsApp webhook verified successfully");
            return ResponseEntity.ok(challenge);
        }
        log.warn("WhatsApp webhook verification failed: mode={}, token mismatch={}", mode, !verifyToken.equals(token));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Webhook message reception endpoint (POST).
     * Processes inbound messages from the WhatsApp provider.
     * Returns 200 immediately to avoid provider retries.
     */
    @PostMapping
    public ResponseEntity<Void> receiveMessage(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature) {

        log.info("WhatsApp webhook received: {}", payload.getOrDefault("object", "unknown"));

        // Validate signature when available
        if (signature != null && !providerPort.validateWebhookSignature(payload.toString(), signature)) {
            log.warn("Invalid WhatsApp webhook signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            processPayload(payload);
        } catch (Exception e) {
            // Log but still return 200 to prevent provider retries for parsing errors
            log.error("Error processing WhatsApp webhook payload: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok().build();
    }

    @SuppressWarnings("unchecked")
    private void processPayload(Map<String, Object> payload) {
        // Generic payload parsing — adapts to Meta Cloud API format
        // Structure: { entry: [{ changes: [{ value: { messages: [...] } }] }] }
        Object entryObj = payload.get("entry");
        if (!(entryObj instanceof java.util.List<?> entries) || entries.isEmpty()) {
            log.debug("No entries in webhook payload");
            return;
        }

        for (Object entry : entries) {
            if (!(entry instanceof Map<?, ?> entryMap)) continue;

            Object changesObj = entryMap.get("changes");
            if (!(changesObj instanceof java.util.List<?> changes)) continue;

            for (Object change : changes) {
                if (!(change instanceof Map<?, ?> changeMap)) continue;

                Object valueObj = changeMap.get("value");
                if (!(valueObj instanceof Map<?, ?> value)) continue;

                Object messagesObj = value.get("messages");
                if (!(messagesObj instanceof java.util.List<?> messages)) continue;

                for (Object msg : messages) {
                    if (!(msg instanceof Map<?, ?> messageMap)) continue;
                    processMessage((Map<String, Object>) messageMap, (Map<String, Object>) value);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processMessage(Map<String, Object> messageMap, Map<String, Object> value) {
        String externalId = (String) messageMap.get("id");
        String from = (String) messageMap.get("from");
        String type = (String) messageMap.getOrDefault("type", "text");

        // Extract text content
        String content = null;
        Object textObj = messageMap.get("text");
        if (textObj instanceof Map<?, ?> textMap) {
            content = (String) textMap.get("body");
        }

        // Extract sender name from contacts
        String senderName = from;
        Object contactsObj = value.get("contacts");
        if (contactsObj instanceof java.util.List<?> contacts && !contacts.isEmpty()) {
            Object contact = contacts.get(0);
            if (contact instanceof Map<?, ?> contactMap) {
                Object profileObj = contactMap.get("profile");
                if (profileObj instanceof Map<?, ?> profile) {
                    String name = (String) profile.get("name");
                    if (name != null) senderName = name;
                }
            }
        }

        whatsAppService.receiveMessage(externalId, from, senderName, content, type);
    }
}
