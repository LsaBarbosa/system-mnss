package br.com.novaalianca.mnss.onlineapp.domain.sync;

import br.com.novaalianca.mnss.sync.SyncEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SyncEventRepository extends JpaRepository<SyncEventEntity, UUID> {
    Optional<SyncEventEntity> findByIdempotencyKey(String idempotencyKey);
}
