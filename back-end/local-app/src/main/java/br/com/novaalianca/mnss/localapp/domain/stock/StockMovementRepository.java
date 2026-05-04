package br.com.novaalianca.mnss.localapp.domain.stock;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovementEntity, UUID> {
    List<StockMovementEntity> findAllByOrderByCreatedAtDesc();

    List<StockMovementEntity> findByProductIdOrderByCreatedAtDesc(UUID productId);

    List<StockMovementEntity> findByProductIdIn(Collection<UUID> productIds);
}
