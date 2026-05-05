package br.com.novaalianca.mnss.onlineapp.domain.sync;

import br.com.novaalianca.mnss.sharedinfra.security.HmacUtils;
import br.com.novaalianca.mnss.sync.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sync")
public class SyncController {
    private static final Logger log = LoggerFactory.getLogger(SyncController.class);

    private final SyncEventRepository repository;
    private final ObjectMapper objectMapper;

    @Value("#{${mnss.sync.stores}}")
    private Map<String, String> storeSecrets;

    public SyncController(SyncEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/events")
    public ResponseEntity<Void> receiveEvent(
            @RequestHeader("X-Store-ID") String storeId,
            @RequestHeader("X-Signature") String signature,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @RequestBody Map<String, Object> body) {

        log.info("Received sync event from store {}: {}", storeId, idempotencyKey);

        // 1. Validate Store
        String secret = storeSecrets.get(storeId);
        if (secret == null) {
            log.warn("Unknown store ID: {}", storeId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. Validate Signature
        try {
            Object payload = body.get("payload");
            String payloadJson = objectMapper.writeValueAsString(payload);
            String dataToSign = idempotencyKey + ":" + payloadJson;
            
            if (!HmacUtils.verifyHmac(dataToSign, signature, secret)) {
                log.warn("Invalid signature for event: {}", idempotencyKey);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            log.error("Error validating signature", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // 3. Idempotency Check
        if (repository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            log.info("Duplicate event ignored: {}", idempotencyKey);
            return ResponseEntity.ok().build();
        }

        // 4. Save Event
        try {
            SyncEventEntity event = new SyncEventEntity(
                    idempotencyKey,
                    SyncDirection.LOCAL_TO_ONLINE,
                    SyncEnvironment.LOCAL,
                    SyncEnvironment.ONLINE,
                    (String) body.get("aggregateType"),
                    (String) body.get("eventType"),
                    (Map<String, Object>) body.get("payload"),
                    SyncEventStatus.SYNCED
            );
            
            String aggregateIdStr = (String) body.get("aggregateId");
            if (aggregateIdStr != null) {
                event.assignAggregateId(UUID.fromString(aggregateIdStr));
            }
            
            repository.save(event);
            log.info("Event saved successfully: {}", idempotencyKey);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error saving sync event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<java.util.List<SyncEventEntity>> getPendingEvents(
            @RequestHeader("X-Store-ID") String storeId,
            @RequestHeader("X-Signature") String signature) {

        // Validate Store
        String secret = storeSecrets.get(storeId);
        if (secret == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Validate Signature (Pull request signature validation)
        // In this case, we can sign the storeId + "pull" or similar
        String dataToSign = storeId + ":pull";
        if (!HmacUtils.verifyHmac(dataToSign, signature, secret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(repository.findByStatusAndDirection(SyncEventStatus.PENDING, SyncDirection.ONLINE_TO_LOCAL));
    }

    @PostMapping("/events/{id}/ack")
    public ResponseEntity<Void> acknowledgeEvent(
            @PathVariable UUID id,
            @RequestHeader("X-Store-ID") String storeId,
            @RequestHeader("X-Signature") String signature) {

        // Validate Store
        String secret = storeSecrets.get(storeId);
        if (secret == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Validate Signature
        String dataToSign = id.toString() + ":ack";
        if (!HmacUtils.verifyHmac(dataToSign, signature, secret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return repository.findById(id).map(event -> {
            event.markAsReceivedByStore();
            repository.save(event);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/events")
    public ResponseEntity<java.util.List<SyncEventEntity>> listEvents() {
        return ResponseEntity.ok(repository.findAllByOrderByCreatedAtDesc());
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Long>> getSyncStatus() {
        // Simplified status counts for dashboard
        java.util.List<SyncEventEntity> all = repository.findAll();
        Map<String, Long> counts = all.stream()
                .collect(java.util.stream.Collectors.groupingBy(e -> e.getStatus().name(), java.util.stream.Collectors.counting()));
        return ResponseEntity.ok(counts);
    }

    @PostMapping("/events/{id}/reprocess")
    public ResponseEntity<Void> reprocessEvent(@PathVariable UUID id) {
        return repository.findById(id).map(event -> {
            event.resetStatus();
            repository.save(event);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/events/{id}/ignore")
    public ResponseEntity<Void> ignoreEvent(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "No reason provided");
        return repository.findById(id).map(event -> {
            event.markAsIgnored(reason);
            repository.save(event);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
