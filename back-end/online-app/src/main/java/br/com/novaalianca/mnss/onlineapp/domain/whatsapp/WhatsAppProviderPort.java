package br.com.novaalianca.mnss.onlineapp.domain.whatsapp;

/**
 * Port interface for WhatsApp provider integration.
 * Implementations can be swapped for different providers (Meta Cloud API, Z-API, etc.).
 */
public interface WhatsAppProviderPort {

    /**
     * Sends a text message to the given phone number.
     *
     * @param phone recipient phone number
     * @param text  message content
     * @return external message ID from the provider, or null if unavailable
     */
    String sendTextMessage(String phone, String text);

    /**
     * Validates the webhook signature from the provider.
     *
     * @param payload   raw payload body
     * @param signature signature header value
     * @return true if signature is valid
     */
    boolean validateWebhookSignature(String payload, String signature);
}
