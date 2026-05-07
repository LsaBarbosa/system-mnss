package br.com.novaalianca.mnss.onlineapp.domain.sync;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OnlineLocalSaleSummaryRepository extends JpaRepository<OnlineLocalSaleSummaryEntity, UUID> {
    boolean existsByStoreIdAndLocalOrderId(String storeId, UUID localOrderId);
    Optional<OnlineLocalSaleSummaryEntity> findByStoreIdAndLocalOrderId(String storeId, UUID localOrderId);
}
