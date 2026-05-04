package br.com.novaalianca.mnss.localapp.domain.sync;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncEventRepository extends JpaRepository<SyncEventEntity, UUID> {}
