package br.com.novaalianca.mnss.onlineapp.domain.whatsapp;

import br.com.novaalianca.mnss.sharedinfra.domain.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "whatsapp_messages")
public class WhatsAppMessageEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private WhatsAppConversationEntity conversation;

    @Column(name = "external_message_id", length = 120, unique = true)
    private String externalMessageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageDirection direction;

    @Column(name = "sender_phone", length = 30)
    private String senderPhone;

    @Column(name = "sender_name", length = 150)
    private String senderName;

    @Column(columnDefinition = "text")
    private String content;

    @Column(name = "message_type", nullable = false, length = 30)
    private String messageType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MessageStatus status;

    protected WhatsAppMessageEntity() {}

    public WhatsAppMessageEntity(
            WhatsAppConversationEntity conversation,
            String externalMessageId,
            MessageDirection direction,
            String senderPhone,
            String senderName,
            String content,
            String messageType) {
        this.conversation = conversation;
        this.externalMessageId = externalMessageId;
        this.direction = direction;
        this.senderPhone = senderPhone;
        this.senderName = senderName;
        this.content = content;
        this.messageType = messageType != null ? messageType : "TEXT";
        this.status = direction == MessageDirection.INBOUND ? MessageStatus.RECEIVED : MessageStatus.SENT;
    }

    public void markAsFailed() {
        this.status = MessageStatus.FAILED;
        touch();
    }

    public void markAsSent() {
        this.status = MessageStatus.SENT;
        touch();
    }

    public WhatsAppConversationEntity getConversation() { return conversation; }
    public String getExternalMessageId() { return externalMessageId; }
    public MessageDirection getDirection() { return direction; }
    public String getSenderPhone() { return senderPhone; }
    public String getSenderName() { return senderName; }
    public String getContent() { return content; }
    public String getMessageType() { return messageType; }
    public MessageStatus getStatus() { return status; }
}
