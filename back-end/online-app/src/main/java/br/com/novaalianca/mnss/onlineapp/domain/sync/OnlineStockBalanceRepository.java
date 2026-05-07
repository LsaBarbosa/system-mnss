package br.com.novaalianca.mnss.onlineapp.domain.sync;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnlineStockBalanceRepository extends JpaRepository<OnlineStockBalanceEntity, UUID> {

    Optional<OnlineStockBalanceEntity> findByProductId(UUID productId);
}
