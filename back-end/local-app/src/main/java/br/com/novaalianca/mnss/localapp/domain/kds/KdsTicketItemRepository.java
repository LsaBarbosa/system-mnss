package br.com.novaalianca.mnss.localapp.domain.kds;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KdsTicketItemRepository extends JpaRepository<KdsTicketItemEntity, UUID> {}
