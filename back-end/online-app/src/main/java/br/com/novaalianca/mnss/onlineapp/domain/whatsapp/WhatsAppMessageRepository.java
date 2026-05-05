package br.com.novaalianca.mnss.onlineapp.domain.whatsapp;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WhatsAppMessageRepository extends JpaRepository<WhatsAppMessageEntity, UUID> {
    Optional<WhatsAppMessageEntity> findByExternalMessageId(String externalMessageId);

    List<WhatsAppMessageEntity> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);
}
