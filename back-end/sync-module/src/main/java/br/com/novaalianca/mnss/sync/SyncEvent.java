package br.com.novaalianca.mnss.sync;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class SyncEvent {
    private final UUID id;
    private final IdempotencyKey idempotencyKey;
    private final SyncDirection direction;
    private final String eventType;
    private final Instant occurredAt;

    public SyncEvent(
            UUID id,
            IdempotencyKey idempotencyKey,
            SyncDirection direction,
            String eventType,
            Instant occurredAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.idempotencyKey =
                Objects.requireNonNull(idempotencyKey, "idempotencyKey must not be null");
        this.direction = Objects.requireNonNull(direction, "direction must not be null");
        this.eventType = requireText(eventType, "eventType");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }

    public UUID id() {
        return id;
    }

    public IdempotencyKey idempotencyKey() {
        return idempotencyKey;
    }

    public SyncDirection direction() {
        return direction;
    }

    public String eventType() {
        return eventType;
    }

    public Instant occurredAt() {
        return occurredAt;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
