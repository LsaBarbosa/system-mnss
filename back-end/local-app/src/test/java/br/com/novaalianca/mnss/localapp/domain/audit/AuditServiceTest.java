package br.com.novaalianca.mnss.localapp.domain.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {
    @Mock
    private AuditLogRepository auditLogRepository;

    @Test
    void recordsActionUserEntityAndTimestamp() {
        UUID actorUserId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        when(auditLogRepository.save(any(AuditLogEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuditLogEntity log = service().record(new AuditLogRequest(
                actorUserId,
                "PRODUCT_PRICE_CHANGED",
                "Product",
                entityId,
                Map.of("newPrice", "12.50"),
                "127.0.0.1"));

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(log).isSameAs(captor.getValue());
        assertThat(log.getActorUserId()).isEqualTo(actorUserId);
        assertThat(log.getAction()).isEqualTo("PRODUCT_PRICE_CHANGED");
        assertThat(log.getEntityType()).isEqualTo("Product");
        assertThat(log.getEntityId()).isEqualTo(entityId);
        assertThat(log.getCreatedAt()).isNotNull();
    }

    @Test
    void refusesEmptyAction() {
        assertThatThrownBy(() -> service().record(new AuditLogRequest(
                        UUID.randomUUID(),
                        " ",
                        "Product",
                        UUID.randomUUID(),
                        Map.of(),
                        "127.0.0.1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("action must not be blank");
        verifyNoInteractions(auditLogRepository);
    }

    private AuditService service() {
        return new AuditService(auditLogRepository);
    }
}
