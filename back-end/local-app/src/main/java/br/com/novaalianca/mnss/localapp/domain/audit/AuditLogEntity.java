package br.com.novaalianca.mnss.localapp.domain.audit;

import br.com.novaalianca.mnss.sharedinfra.domain.BaseEntity;
import br.com.novaalianca.mnss.localapp.domain.shared.DomainValidation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "audit_logs")
public class AuditLogEntity extends BaseEntity {
    private UUID actorUserId;

    @Column(nullable = false, length = 120)
    private String action;

    @Column(nullable = false, length = 80)
    private String entityType;

    private UUID entityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> details = new LinkedHashMap<>();

    @Column(length = 80)
    private String ipAddress;

    protected AuditLogEntity() {}

    public AuditLogEntity(
            UUID actorUserId,
            String action,
            String entityType,
            UUID entityId,
            Map<String, Object> details,
            String ipAddress) {
        this.actorUserId = actorUserId;
        this.action = DomainValidation.requireText(action, "action");
        this.entityType = DomainValidation.requireText(entityType, "entityType");
        this.entityId = entityId;
        this.details = details == null ? new LinkedHashMap<>() : new LinkedHashMap<>(details);
        this.ipAddress = ipAddress;
    }

    public UUID getActorUserId() {
        return actorUserId;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public Map<String, Object> getDetails() {
        return Collections.unmodifiableMap(details);
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
