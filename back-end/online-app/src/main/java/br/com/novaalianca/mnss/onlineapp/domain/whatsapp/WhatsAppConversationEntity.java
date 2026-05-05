package br.com.novaalianca.mnss.onlineapp.domain.whatsapp;

import br.com.novaalianca.mnss.sharedinfra.domain.BaseEntity;
import jakarta.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "whatsapp_conversations")
public class WhatsAppConversationEntity extends BaseEntity {

    @Column(name = "customer_phone", nullable = false, length = 30)
    private String customerPhone;

    @Column(name = "customer_name", length = 150)
    private String customerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ConversationStatus status;

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @Column(name = "order_id")
    private UUID orderId;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private Set<WhatsAppMessageEntity> messages = new LinkedHashSet<>();

    protected WhatsAppConversationEntity() {}

    public WhatsAppConversationEntity(String customerPhone, String customerName) {
        if (customerPhone == null || customerPhone.isBlank()) {
            throw new IllegalArgumentException("Customer phone is required");
        }
        this.customerPhone = customerPhone;
        this.customerName = customerName;
        this.status = ConversationStatus.OPEN;
    }

    public void assignTo(UUID userId) {
        this.assignedTo = userId;
        this.status = ConversationStatus.ASSIGNED;
        touch();
    }

    public void linkOrder(UUID orderId) {
        this.orderId = orderId;
        touch();
    }

    public void close() {
        this.status = ConversationStatus.CLOSED;
        touch();
    }

    public void updateCustomerName(String name) {
        if (name != null && !name.isBlank()) {
            this.customerName = name;
            touch();
        }
    }

    public String getCustomerPhone() { return customerPhone; }
    public String getCustomerName() { return customerName; }
    public ConversationStatus getStatus() { return status; }
    public UUID getAssignedTo() { return assignedTo; }
    public UUID getOrderId() { return orderId; }
    public Set<WhatsAppMessageEntity> getMessages() { return Set.copyOf(messages); }
}
