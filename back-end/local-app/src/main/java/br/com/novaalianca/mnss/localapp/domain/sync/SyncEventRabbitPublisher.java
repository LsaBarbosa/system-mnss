package br.com.novaalianca.mnss.localapp.domain.sync;

import br.com.novaalianca.mnss.localapp.config.RabbitMQConfiguration;
import br.com.novaalianca.mnss.sync.SyncEventEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SyncEventRabbitPublisher {
    private static final Logger log = LoggerFactory.getLogger(SyncEventRabbitPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public SyncEventRabbitPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishSyncOutbox(SyncEventEntity event) {
        try {
            Map<String, Object> message = Map.of(
                    "eventId",        event.getId().toString(),
                    "idempotencyKey", event.getIdempotencyKey(),
                    "eventType",      event.getEventType(),
                    "aggregateType",  event.getAggregateType() != null ? event.getAggregateType() : "",
                    "aggregateId",    event.getAggregateId() != null ? event.getAggregateId().toString() : ""
            );
            rabbitTemplate.convertAndSend(
                    RabbitMQConfiguration.LOCAL_EXCHANGE,
                    "sync.outbox.pending",
                    message);
            log.debug("Published sync outbox event to RabbitMQ: {}", event.getIdempotencyKey());
        } catch (AmqpException e) {
            // RabbitMQ unavailable — DB outbox worker will retry via polling
            log.warn("Could not publish sync event to RabbitMQ (will retry via polling): {}", e.getMessage());
        }
    }

    public void publishKdsUpdate(String eventType, Map<String, Object> payload) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfiguration.LOCAL_EXCHANGE,
                    "kds." + eventType.toLowerCase(),
                    payload);
        } catch (AmqpException e) {
            log.warn("Could not publish KDS event to RabbitMQ: {}", e.getMessage());
        }
    }

    public void publishPrintJob(Map<String, Object> payload) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfiguration.LOCAL_EXCHANGE,
                    "print.job",
                    payload);
        } catch (AmqpException e) {
            log.warn("Could not publish print job to RabbitMQ: {}", e.getMessage());
        }
    }
}
