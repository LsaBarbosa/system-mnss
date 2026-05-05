package br.com.novaalianca.mnss.localapp.domain.pdv;

import br.com.novaalianca.mnss.localapp.domain.order.OrderEntity;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncDirection;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncEnvironment;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncEventEntity;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncEventRepository;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
class PdvSyncEventService {
    private final Optional<SyncEventRepository> syncEventRepository;

    PdvSyncEventService(Optional<SyncEventRepository> syncEventRepository) {
        this.syncEventRepository = syncEventRepository;
    }

    @Transactional
    public void recordOrderFinishedEvent(OrderEntity order) {
        SyncEventEntity event = new SyncEventEntity(
                UUID.randomUUID().toString(),
                SyncDirection.LOCAL_TO_ONLINE,
                SyncEnvironment.LOCAL,
                SyncEnvironment.ONLINE,
                "Order",
                "SALE_FINISHED",
                Map.of("orderId", order.getId().toString()),
                SyncStatus.PENDING);
        event.assignAggregateId(order.getId());
        syncEventRepository().save(event);
    }

    private SyncEventRepository syncEventRepository() {
        return syncEventRepository
                .orElseThrow(() -> new IllegalStateException("Sync event repository is not available."));
    }
}
