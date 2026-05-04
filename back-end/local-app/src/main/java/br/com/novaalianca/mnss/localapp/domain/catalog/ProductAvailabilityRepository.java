package br.com.novaalianca.mnss.localapp.domain.catalog;

import br.com.novaalianca.mnss.core.catalog.SalesChannel;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductAvailabilityRepository extends JpaRepository<ProductAvailabilityEntity, UUID> {
    Optional<ProductAvailabilityEntity> findFirstByProductIdAndChannelOrderByUpdatedAtDesc(UUID productId, SalesChannel channel);

    List<ProductAvailabilityEntity> findByProductIdIn(List<UUID> productIds);
}
