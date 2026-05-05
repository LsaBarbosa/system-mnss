package br.com.novaalianca.mnss.onlineapp.domain.whatsapp;

import br.com.novaalianca.mnss.onlineapp.domain.whatsapp.dto.ConversationResponse;
import br.com.novaalianca.mnss.onlineapp.domain.whatsapp.dto.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WhatsAppService {
    private static final Logger log = LoggerFactory.getLogger(WhatsAppService.class);

    private final WhatsAppConversationRepository conversationRepository;
    private final WhatsAppMessageRepository messageRepository;
    private final WhatsAppProviderPort providerPort;

    public WhatsAppService(
            WhatsAppConversationRepository conversationRepository,
            WhatsAppMessageRepository messageRepository,
            WhatsAppProviderPort providerPort) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.providerPort = providerPort;
    }

    /**
     * Processes an inbound webhook message from the WhatsApp provider.
     * Creates or reuses a conversation and persists the message.
     * Idempotent: duplicate externalMessageId is ignored.
     */
    @Transactional
    public void receiveMessage(String externalMessageId, String senderPhone, String senderName,
                               String content, String messageType) {
        // Idempotency check
        if (externalMessageId != null && messageRepository.findByExternalMessageId(externalMessageId).isPresent()) {
            log.info("Duplicate WhatsApp message ignored: {}", externalMessageId);
            return;
        }

        WhatsAppConversationEntity conversation = findOrCreateConversation(senderPhone, senderName);

        WhatsAppMessageEntity message = new WhatsAppMessageEntity(
                conversation,
                externalMessageId,
                MessageDirection.INBOUND,
                senderPhone,
                senderName,
                content,
                messageType);
        messageRepository.save(message);

        log.info("WhatsApp message received from {} in conversation {}", senderPhone, conversation.getId());
    }

    /**
     * Sends a text message to the customer in a conversation via the provider adapter.
     * Persists the outbound message. Provider failures do NOT throw — the message is marked FAILED.
     */
    @Transactional
    public MessageResponse sendMessage(UUID conversationId, String content) {
        WhatsAppConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversa não encontrada: " + conversationId));

        WhatsAppMessageEntity message = new WhatsAppMessageEntity(
                conversation, null, MessageDirection.OUTBOUND,
                null, null, content, "TEXT");

        try {
            String externalId = providerPort.sendTextMessage(conversation.getCustomerPhone(), content);
            log.info("WhatsApp message sent to {} (externalId={})", conversation.getCustomerPhone(), externalId);
        } catch (Exception e) {
            log.error("Failed to send WhatsApp message to {}: {}", conversation.getCustomerPhone(), e.getMessage());
            message.markAsFailed();
        }

        WhatsAppMessageEntity saved = messageRepository.save(message);
        return MessageResponse.from(saved);
    }

    /**
     * Sends a formatted order summary to the customer.
     */
    @Transactional
    public MessageResponse sendOrderSummary(UUID conversationId, UUID orderId, String summaryText) {
        return sendMessage(conversationId,
                "✅ *Pedido #" + orderId.toString().substring(0, 8) + " confirmado!*\n\n" + summaryText +
                "\n\nObrigado por seu pedido! Acompanhe o status pelo seu telefone.");
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> listConversations() {
        return conversationRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(conv -> {
                    List<WhatsAppMessageEntity> messages = messageRepository
                            .findByConversationIdOrderByCreatedAtAsc(conv.getId());
                    String lastMsg = messages.isEmpty() ? null
                            : messages.get(messages.size() - 1).getContent();
                    return ConversationResponse.from(conv, lastMsg);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getConversationMessages(UUID conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .map(MessageResponse::from)
                .toList();
    }

    @Transactional
    public ConversationResponse assignConversation(UUID conversationId, UUID userId) {
        WhatsAppConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversa não encontrada: " + conversationId));
        conversation.assignTo(userId);
        conversationRepository.save(conversation);
        return ConversationResponse.from(conversation, null);
    }

    @Transactional
    public void linkOrderToConversation(UUID conversationId, UUID orderId) {
        WhatsAppConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversa não encontrada: " + conversationId));
        conversation.linkOrder(orderId);
        conversationRepository.save(conversation);
    }

    private WhatsAppConversationEntity findOrCreateConversation(String phone, String name) {
        return conversationRepository
                .findFirstByCustomerPhoneAndStatusNotOrderByCreatedAtDesc(phone, ConversationStatus.CLOSED)
                .map(conv -> {
                    conv.updateCustomerName(name);
                    return conversationRepository.save(conv);
                })
                .orElseGet(() -> conversationRepository.save(new WhatsAppConversationEntity(phone, name)));
    }
}
