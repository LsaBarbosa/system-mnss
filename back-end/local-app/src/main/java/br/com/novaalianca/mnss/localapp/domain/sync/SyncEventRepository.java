package br.com.novaalianca.mnss.localapp.domain.sync;

import br.com.novaalianca.mnss.sync.SyncDirection;
import br.com.novaalianca.mnss.sync.SyncEventEntity;
import br.com.novaalianca.mnss.sync.SyncEventStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncEventRepository extends JpaRepository<SyncEventEntity, UUID> {
    List<SyncEventEntity> findByStatusAndDirection(SyncEventStatus status, SyncDirection direction);
    List<SyncEventEntity> findByStatusInAndNextRetryAtBefore(List<SyncEventStatus> statuses, Instant now);
    Optional<SyncEventEntity> findFirstByAggregateIdOrderByCreatedAtDesc(UUID aggregateId);
}
