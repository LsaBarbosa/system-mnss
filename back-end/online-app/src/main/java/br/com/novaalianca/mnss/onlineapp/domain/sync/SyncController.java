package br.com.novaalianca.mnss.onlineapp.domain.sync;

import br.com.novaalianca.mnss.onlineapp.config.SyncStoresProperties;
import br.com.novaalianca.mnss.sharedinfra.security.HmacUtils;
import br.com.novaalianca.mnss.sync.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sync")
public class SyncController {
    private static final Logger log = LoggerFactory.getLogger(SyncController.class);
    private static final String STORE_ID_FIELD = "storeId";

    private final SyncEventRepository repository;
    private final ObjectMapper objectMapper;
    private final SyncEventMapper syncEventMapper;
    private final SyncStoresProperties storesProperties;

    public SyncController(SyncEventRepository repository, ObjectMapper objectMapper,
                          SyncEventMapper syncEventMapper, SyncStoresProperties storesProperties) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.syncEventMapper = syncEventMapper;
        this.storesProperties = storesProperties;
    }

    @PostMapping("/events")
    public ResponseEntity<Void> receiveEvent(
            @RequestHeader("X-Store-ID") String storeId,
            @RequestHeader("X-Signature") String signature,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @RequestBody Map<String, Object> body) {

        log.info("Received sync event from store {}: {}", storeId, idempotencyKey);

        // 1. Validate Store
        String secret = storesProperties.secretFor(storeId);
        if (secret == null) {
            log.warn("Unknown store ID: {}", storeId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. Validate Signature
        try {
            Object payload = body.get("payload");
            if (!(payload instanceof Map<?, ?>)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
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
            Object payload = body.get("payload");
            @SuppressWarnings("unchecked")
            Map<String, Object> rawPayload = payload == null
                    ? Map.of()
                    : (Map<String, Object>) payload;
            Map<String, Object> eventPayload = new java.util.LinkedHashMap<>(rawPayload);
            eventPayload.putIfAbsent(STORE_ID_FIELD, storeId);

            SyncEventEntity event = new SyncEventEntity(
                    idempotencyKey,
                    SyncDirection.LOCAL_TO_ONLINE,
                    SyncEnvironment.LOCAL,
                    SyncEnvironment.ONLINE,
                    (String) body.get("aggregateType"),
                    (String) body.get("eventType"),
                    eventPayload,
                    SyncEventStatus.PENDING
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
    public ResponseEntity<java.util.List<SyncEventDto>> getPendingEvents(
            @RequestHeader("X-Store-ID") String storeId,
            @RequestHeader("X-Signature") String signature,
            @RequestParam(value = "storeId", required = false) String requestedStoreId) {

        // Validate Store
        String secret = storesProperties.secretFor(storeId);
        if (secret == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String effectiveStoreId = storeId;
        if (requestedStoreId != null && !requestedStoreId.isBlank()) {
            if (!storeId.equals(requestedStoreId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            effectiveStoreId = requestedStoreId;
        }

        String dataToSign = storeId + ":pull";
        if (!HmacUtils.verifyHmac(dataToSign, signature, secret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String targetStoreId = effectiveStoreId;
        java.util.List<SyncEventDto> filtered = repository.findByStatusAndDirection(
                        SyncEventStatus.PENDING,
                        SyncDirection.ONLINE_TO_LOCAL).stream()
                .filter(event -> belongsToStore(event, targetStoreId))
                .map(syncEventMapper::toDto)
                .toList();
        return ResponseEntity.ok(filtered);
    }

    @PostMapping("/events/{id}/ack")
    public ResponseEntity<Void> acknowledgeEvent(
            @PathVariable UUID id,
            @RequestHeader("X-Store-ID") String storeId,
            @RequestHeader("X-Signature") String signature) {

        // Validate Store
        String secret = storesProperties.secretFor(storeId);
        if (secret == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Validate Signature
        String dataToSign = id.toString() + ":ack";
        if (!HmacUtils.verifyHmac(dataToSign, signature, secret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return repository.findById(id).map(event -> {
            if (!belongsToStore(event, storeId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
            }
            if (event.getStatus() == SyncEventStatus.RECEIVED_BY_STORE) {
                return ResponseEntity.ok().<Void>build();
            }
            event.markAsReceivedByStore();
            repository.save(event);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/events")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<java.util.List<SyncEventDto>> listEvents() {
        return ResponseEntity.ok(
                syncEventMapper.toDtoList(repository.findAllByOrderByCreatedAtDesc()));
    }

    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Map<String, Long>> getSyncStatus() {
        Map<String, Long> counts = new java.util.LinkedHashMap<>();
        for (SyncEventStatus status : SyncEventStatus.values()) {
            long count = repository.countByStatus(status);
            if (count > 0) {
                counts.put(status.name(), count);
            }
        }
        return ResponseEntity.ok(counts);
    }

    @PostMapping("/events/{id}/reprocess")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Void> reprocessEvent(@PathVariable UUID id) {
        return repository.findById(id).map(event -> {
            event.resetStatus();
            repository.save(event);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/events/{id}/ignore")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Void> ignoreEvent(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "No reason provided");
        return repository.findById(id).map(event -> {
            event.markAsIgnored(reason);
            repository.save(event);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    private boolean belongsToStore(SyncEventEntity event, String storeId) {
        Object payloadStoreId = event.getPayload().get(STORE_ID_FIELD);
        return payloadStoreId != null && storeId.equals(payloadStoreId.toString());
    }
}
