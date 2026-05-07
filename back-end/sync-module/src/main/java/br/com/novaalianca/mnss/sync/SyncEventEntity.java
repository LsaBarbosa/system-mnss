package br.com.novaalianca.mnss.sync;

import br.com.novaalianca.mnss.sharedinfra.domain.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "sync_events")
public class SyncEventEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 120)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SyncDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SyncEnvironment sourceEnvironment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SyncEnvironment targetEnvironment;

    @Column(nullable = false, length = 80)
    private String aggregateType;

    private UUID aggregateId;

    @Column(nullable = false, length = 120)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload = new LinkedHashMap<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SyncEventStatus status;

    @Column(nullable = false)
    private int retryCount;

    private Instant nextRetryAt;

    @Column(columnDefinition = "text")
    private String lastError;

    private Instant processedAt;

    protected SyncEventEntity() {}

    public SyncEventEntity(
            String idempotencyKey,
            SyncDirection direction,
            SyncEnvironment sourceEnvironment,
            SyncEnvironment targetEnvironment,
            String aggregateType,
            String eventType,
            Map<String, Object> payload,
            SyncEventStatus status) {
        this.idempotencyKey = idempotencyKey;
        this.direction = Objects.requireNonNull(direction, "direction must not be null");
        this.sourceEnvironment = Objects.requireNonNull(sourceEnvironment, "sourceEnvironment must not be null");
        this.targetEnvironment = Objects.requireNonNull(targetEnvironment, "targetEnvironment must not be null");
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.payload = new LinkedHashMap<>(Objects.requireNonNull(payload, "payload must not be null"));
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.retryCount = 0;
    }

    public void assignAggregateId(UUID aggregateId) {
        this.aggregateId = aggregateId;
        touch();
    }

    public void markAsSynced() {
        this.status = SyncEventStatus.SYNCED;
        this.processedAt = Instant.now();
        touch();
    }

    public void markAsFailed(String error, Instant nextRetry) {
        this.retryCount++;
        this.lastError = error;
        this.nextRetryAt = nextRetry;
        this.status = nextRetry != null ? SyncEventStatus.RETRYING : SyncEventStatus.FAILED;
        touch();
    }

    public void markAsDeadLetter() {
        this.status = SyncEventStatus.DEAD_LETTER;
        touch();
    }

    public void markAsReceivedByStore() {
        this.status = SyncEventStatus.RECEIVED_BY_STORE;
        this.processedAt = Instant.now();
        touch();
    }

    public void markAsIgnored(String reason) {
        this.status = SyncEventStatus.IGNORED;
        this.lastError = reason;
        touch();
    }

    public void resetStatus() {
        this.status = SyncEventStatus.PENDING;
        this.lastError = null;
        this.nextRetryAt = null;
        touch();
    }

    public String getIdempotencyKey() { return idempotencyKey; }
    public SyncDirection getDirection() { return direction; }
    public SyncEnvironment getSourceEnvironment() { return sourceEnvironment; }
    public SyncEnvironment getTargetEnvironment() { return targetEnvironment; }
    public String getAggregateType() { return aggregateType; }
    public UUID getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public Map<String, Object> getPayload() { return Collections.unmodifiableMap(payload); }
    public SyncEventStatus getStatus() { return status; }
    public int getRetryCount() { return retryCount; }
    public Instant getNextRetryAt() { return nextRetryAt; }
    public String getLastError() { return lastError; }
    public Instant getProcessedAt() { return processedAt; }
}
