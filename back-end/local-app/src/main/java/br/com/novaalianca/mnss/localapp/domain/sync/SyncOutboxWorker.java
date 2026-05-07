package br.com.novaalianca.mnss.localapp.domain.sync;

import br.com.novaalianca.mnss.sharedinfra.security.HmacUtils;
import br.com.novaalianca.mnss.sync.SyncDirection;
import br.com.novaalianca.mnss.sync.SyncEventEntity;
import br.com.novaalianca.mnss.sync.SyncEventStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class SyncOutboxWorker {
    private static final Logger log = LoggerFactory.getLogger(SyncOutboxWorker.class);

    private final SyncEventRepository repository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${mnss.sync.online-url}")
    private String onlineUrl;

    @Value("${mnss.sync.store-id}")
    private String storeId;

    @Value("${mnss.sync.store-secret}")
    private String storeSecret;

    @Value("${mnss.sync.retry-intervals:1m,5m,15m,1h}")
    private String retryIntervals;

    @Value("${mnss.sync.require-https:false}")
    private boolean requireHttps;

    public SyncOutboxWorker(SyncEventRepository repository, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.repository = repository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // Permite que o consumer RabbitMQ processe um evento específico imediatamente
    public void sendEventById(java.util.UUID id) {
        repository.findById(id).ifPresent(this::sendEvent);
    }

    @Scheduled(fixedDelayString = "${mnss.sync.fixed-delay:30000}")
    public void processPendingEvents() {
        List<SyncEventEntity> pending = repository.findByStatusAndDirection(SyncEventStatus.PENDING, SyncDirection.LOCAL_TO_ONLINE);
        pending.forEach(this::sendEvent);

        List<SyncEventEntity> retrying = repository.findByStatusInAndNextRetryAtBefore(
                List.of(SyncEventStatus.FAILED, SyncEventStatus.RETRYING),
                Instant.now()
        );
        retrying.forEach(this::sendEvent);
    }

    private void sendEvent(SyncEventEntity event) {
        try {
            validateSyncUrl();
            log.info("Sending sync event: {} ({})", event.getEventType(), event.getIdempotencyKey());

            String payloadJson = objectMapper.writeValueAsString(event.getPayload());
            String signature = HmacUtils.calculateHmac(event.getIdempotencyKey() + ":" + payloadJson, storeSecret);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Store-ID", storeId);
            headers.set("X-Signature", signature);
            headers.set("X-Idempotency-Key", event.getIdempotencyKey());

            Map<String, Object> body = new java.util.LinkedHashMap<>();
            body.put("id", event.getId());
            body.put("eventType", event.getEventType());
            body.put("aggregateType", event.getAggregateType());
            body.put("aggregateId", event.getAggregateId());
            body.put("payload", event.getPayload());
            body.put("occurredAt", event.getCreatedAt());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Void> response = restTemplate.postForEntity(onlineUrl + "/api/sync/events", request, Void.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                event.markAsSynced();
                log.info("Event synced successfully: {}", event.getIdempotencyKey());
            } else {
                handleFailure(event, "Server returned " + response.getStatusCode());
            }
        } catch (Exception e) {
            handleFailure(event, e.getMessage());
        } finally {
            repository.save(event);
        }
    }

    private void validateSyncUrl() {
        if (requireHttps && onlineUrl != null && onlineUrl.startsWith("http://")) {
            throw new IllegalStateException(
                    "SECURITY VIOLATION: Sync URL must use HTTPS in production. " +
                    "Current URL: " + onlineUrl + ". " +
                    "Set mnss.sync.online-url to an HTTPS URL or disable require-https."
            );
        }
    }

    private void handleFailure(SyncEventEntity event, String error) {
        log.error("Failed to sync event {}: {}", event.getIdempotencyKey(), error);
        Instant nextRetry = calculateNextRetry(event.getRetryCount());
        if (nextRetry != null) {
            event.markAsFailed(error, nextRetry);
        } else {
            event.markAsDeadLetter();
            log.error("Event moved to dead letter: {}", event.getIdempotencyKey());
        }
    }

    private Instant calculateNextRetry(int retryCount) {
        String[] intervals = retryIntervals.split(",");
        if (retryCount >= intervals.length) {
            return null;
        }
        String intervalStr = intervals[retryCount].trim();
        Duration duration = parseDuration(intervalStr);
        return Instant.now().plus(duration);
    }

    private Duration parseDuration(String s) {
        if (s.endsWith("m")) return Duration.ofMinutes(Long.parseLong(s.replace("m", "")));
        if (s.endsWith("h")) return Duration.ofHours(Long.parseLong(s.replace("h", "")));
        if (s.endsWith("s")) return Duration.ofSeconds(Long.parseLong(s.replace("s", "")));
        return Duration.ofMinutes(5);
    }
}
