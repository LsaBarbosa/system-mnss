package br.com.novaalianca.mnss.localapp.domain.audit;

import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final Optional<AuditLogRepository> auditLogRepository;

    public AuditService(Optional<AuditLogRepository> auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public AuditLogEntity record(AuditLogRequest request) {
        AuditLogEntity log = new AuditLogEntity(
                request.actorUserId(),
                request.action(),
                request.entityType(),
                request.entityId(),
                request.details(),
                request.ipAddress());
        return auditLogRepository
                .orElseThrow(() -> new IllegalStateException("Audit repository is not available."))
                .save(log);
    }
}
