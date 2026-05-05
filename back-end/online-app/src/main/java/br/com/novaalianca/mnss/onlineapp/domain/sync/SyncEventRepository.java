package br.com.novaalianca.mnss.onlineapp.domain.sync;

import br.com.novaalianca.mnss.sync.SyncDirection;
import br.com.novaalianca.mnss.sync.SyncEventEntity;
import br.com.novaalianca.mnss.sync.SyncEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SyncEventRepository extends JpaRepository<SyncEventEntity, UUID> {
    Optional<SyncEventEntity> findByIdempotencyKey(String idempotencyKey);
    List<SyncEventEntity> findByStatusAndDirection(SyncEventStatus status, SyncDirection direction);
    List<SyncEventEntity> findByDirectionAndStatusIn(SyncDirection direction, List<SyncEventStatus> statuses);
    List<SyncEventEntity> findAllByOrderByCreatedAtDesc();
    long countByStatus(SyncEventStatus status);
}
