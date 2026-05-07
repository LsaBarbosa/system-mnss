package br.com.novaalianca.mnss.localapp.domain.sync;

import br.com.novaalianca.mnss.sync.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

public class SyncEventService {
    private final SyncEventRepository repository;
    private final SyncEventRabbitPublisher rabbitPublisher;

    public SyncEventService(SyncEventRepository repository, SyncEventRabbitPublisher rabbitPublisher) {
        this.repository = repository;
        this.rabbitPublisher = rabbitPublisher;
    }

    @Transactional
    public void createPending(String aggregateType, UUID aggregateId, String eventType, Map<String, Object> payload) {
        SyncEventEntity event = new SyncEventEntity(
                UUID.randomUUID().toString(),
                SyncDirection.LOCAL_TO_ONLINE,
                SyncEnvironment.LOCAL,
                SyncEnvironment.ONLINE,
                aggregateType,
                eventType,
                payload,
                SyncEventStatus.PENDING
        );
        event.assignAggregateId(aggregateId);
        SyncEventEntity saved = repository.save(event);
        // Trigger immediate processing via RabbitMQ; DB outbox acts as fallback
        rabbitPublisher.publishSyncOutbox(saved);
    }
}
