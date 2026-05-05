package br.com.novaalianca.mnss.onlineapp.domain.whatsapp;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Mock implementation of WhatsAppProviderPort for development and testing.
 * Logs messages instead of sending them to a real provider.
 */
@Component
@ConditionalOnProperty(name = "mnss.whatsapp.provider", havingValue = "mock", matchIfMissing = true)
public class MockWhatsAppProvider implements WhatsAppProviderPort {
    private static final Logger log = LoggerFactory.getLogger(MockWhatsAppProvider.class);

    @Override
    public String sendTextMessage(String phone, String text) {
        String mockExternalId = "mock-" + UUID.randomUUID();
        log.info("[MOCK WhatsApp] Sending message to {}: '{}' (externalId={})", phone, text, mockExternalId);
        return mockExternalId;
    }

    @Override
    public boolean validateWebhookSignature(String payload, String signature) {
        log.debug("[MOCK WhatsApp] Skipping signature validation for mock provider");
        return true;
    }
}
