package br.com.novaalianca.mnss.localapp.domain.sync;

import br.com.novaalianca.mnss.localapp.domain.shared.BaseEntity;
import br.com.novaalianca.mnss.localapp.domain.shared.DomainValidation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    private SyncStatus status;

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
            SyncStatus status) {
        this.idempotencyKey = DomainValidation.requireText(idempotencyKey, "idempotencyKey");
        this.direction = Objects.requireNonNull(direction, "direction must not be null");
        this.sourceEnvironment = Objects.requireNonNull(sourceEnvironment, "sourceEnvironment must not be null");
        this.targetEnvironment = Objects.requireNonNull(targetEnvironment, "targetEnvironment must not be null");
        this.aggregateType = DomainValidation.requireText(aggregateType, "aggregateType");
        this.eventType = DomainValidation.requireText(eventType, "eventType");
        this.payload = new LinkedHashMap<>(Objects.requireNonNull(payload, "payload must not be null"));
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    public void assignAggregateId(UUID aggregateId) {
        this.aggregateId = aggregateId;
        touch();
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public SyncDirection getDirection() {
        return direction;
    }

    public SyncEnvironment getSourceEnvironment() {
        return sourceEnvironment;
    }

    public SyncEnvironment getTargetEnvironment() {
        return targetEnvironment;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public Map<String, Object> getPayload() {
        return Collections.unmodifiableMap(payload);
    }

    public SyncStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public Instant getNextRetryAt() {
        return nextRetryAt;
    }

    public String getLastError() {
        return lastError;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
