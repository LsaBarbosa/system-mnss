package br.com.novaalianca.mnss.onlineapp.domain.sync;

import br.com.novaalianca.mnss.sync.SyncDirection;
import br.com.novaalianca.mnss.sync.SyncEnvironment;
import br.com.novaalianca.mnss.sync.SyncEventStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record SyncEventDto(
        UUID id,
        String idempotencyKey,
        SyncDirection direction,
        SyncEnvironment sourceEnvironment,
        SyncEnvironment targetEnvironment,
        String aggregateType,
        UUID aggregateId,
        String eventType,
        Map<String, Object> payload,
        SyncEventStatus status,
        int retryCount,
        Instant nextRetryAt,
        String lastError,
        Instant processedAt,
        Instant createdAt,
        Instant updatedAt
) {}
