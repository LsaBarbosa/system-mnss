package br.com.novaalianca.mnss.localapp.domain.kds;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.localapp.domain.order.OrderEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KdsTicketRepository extends JpaRepository<KdsTicketEntity, UUID> {
    List<KdsTicketEntity> findBySectorOrderByCreatedAtAsc(PreparationSector sector);
    List<KdsTicketEntity> findAllByOrderByCreatedAtAsc();
    List<KdsTicketEntity> findByOrder(OrderEntity order);
}
