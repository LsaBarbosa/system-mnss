package br.com.novaalianca.mnss.localapp.domain.stock;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovementEntity, UUID> {
    List<StockMovementEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<StockMovementEntity> findByProductIdOrderByCreatedAtDesc(UUID productId, Pageable pageable);

    List<StockMovementEntity> findByProductIdIn(Collection<UUID> productIds);

    Optional<StockMovementEntity> findByIdempotencyKey(String idempotencyKey);
}
