package br.com.novaalianca.mnss.localapp.domain.stock;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockBalanceRepository extends JpaRepository<StockBalanceEntity, UUID> {

    Optional<StockBalanceEntity> findByProductId(UUID productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM StockBalanceEntity b WHERE b.product.id = :productId")
    Optional<StockBalanceEntity> findByProductIdForUpdate(@Param("productId") UUID productId);

    List<StockBalanceEntity> findByProductIdIn(Collection<UUID> productIds);
}
