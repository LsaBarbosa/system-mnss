package br.com.novaalianca.mnss.onlineapp.domain.catalog;

import br.com.novaalianca.mnss.core.catalog.SalesChannel;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OnlineProductAvailabilityRepository extends JpaRepository<OnlineProductAvailabilityEntity, UUID> {
    Optional<OnlineProductAvailabilityEntity> findFirstByProductIdAndChannelOrderByUpdatedAtDesc(UUID productId, SalesChannel channel);
}
