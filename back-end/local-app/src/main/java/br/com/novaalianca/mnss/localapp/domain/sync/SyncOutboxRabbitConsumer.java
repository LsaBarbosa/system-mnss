package br.com.novaalianca.mnss.localapp.domain.sync;

import br.com.novaalianca.mnss.localapp.config.RabbitMQConfiguration;
import br.com.novaalianca.mnss.sync.SyncEventEntity;
import br.com.novaalianca.mnss.sync.SyncEventStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Processa eventos da fila do RabbitMQ imediatamente, sem esperar o ciclo de
 * polling de 5s do SyncOutboxWorker. O SyncOutboxWorker continua ativo como
 * fallback para eventos que não chegaram via fila (ex: RabbitMQ estava offline).
 */
@Component
public class SyncOutboxRabbitConsumer {
    private static final Logger log = LoggerFactory.getLogger(SyncOutboxRabbitConsumer.class);

    private final SyncEventRepository repository;
    private final SyncOutboxWorker outboxWorker;

    public SyncOutboxRabbitConsumer(SyncEventRepository repository, SyncOutboxWorker outboxWorker) {
        this.repository = repository;
        this.outboxWorker = outboxWorker;
    }

    @RabbitListener(queues = RabbitMQConfiguration.SYNC_OUTBOX_QUEUE)
    @Transactional
    public void onSyncOutboxEvent(Map<String, Object> message) {
        String idempotencyKey = (String) message.get("idempotencyKey");
        if (idempotencyKey == null) {
            log.warn("Received sync outbox message without idempotencyKey, discarding");
            return;
        }

        repository.findByIdempotencyKey(idempotencyKey).ifPresentOrElse(
                event -> {
                    if (event.getStatus() != SyncEventStatus.PENDING
                            && event.getStatus() != SyncEventStatus.FAILED
                            && event.getStatus() != SyncEventStatus.RETRYING) {
                        log.debug("Event {} already processed (status={}), skipping",
                                idempotencyKey, event.getStatus());
                        return;
                    }
                    log.info("Processing sync event immediately via RabbitMQ: {}", idempotencyKey);
                    outboxWorker.sendEventById(event.getId());
                },
                () -> log.warn("Sync event not found in DB for key: {}", idempotencyKey)
        );
    }
}
