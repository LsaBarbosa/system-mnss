package br.com.novaalianca.mnss.onlineapp.domain.whatsapp;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WhatsAppConversationRepository extends JpaRepository<WhatsAppConversationEntity, UUID> {
    Optional<WhatsAppConversationEntity> findFirstByCustomerPhoneAndStatusNotOrderByCreatedAtDesc(
            String customerPhone, ConversationStatus excludedStatus);

    List<WhatsAppConversationEntity> findAllByOrderByUpdatedAtDesc();

    List<WhatsAppConversationEntity> findByStatusOrderByUpdatedAtDesc(ConversationStatus status);
}
