package br.com.novaalianca.mnss.localapp.domain.audit;

import java.util.Map;
import java.util.UUID;

public record AuditLogRequest(
        UUID actorUserId,
        String action,
        String entityType,
        UUID entityId,
        Map<String, Object> details,
        String ipAddress) {}
