package br.com.novaalianca.mnss.localapp.domain.sync;

import br.com.novaalianca.mnss.sync.SyncDirection;
import br.com.novaalianca.mnss.sync.SyncEnvironment;
import br.com.novaalianca.mnss.sync.SyncEventEntity;
import br.com.novaalianca.mnss.sync.SyncEventStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SyncEventServiceTest {

    @Mock
    private SyncEventRepository repository;

    @InjectMocks
    private SyncEventService service;

    @Test
    void createPending_ShouldSaveEventWithPendingStatus() {
        UUID aggregateId = UUID.randomUUID();
        Map<String, Object> payload = Map.of("key", "value");

        service.createPending("TestEntity", aggregateId, "TEST_EVENT", payload);

        ArgumentCaptor<SyncEventEntity> captor = ArgumentCaptor.forClass(SyncEventEntity.class);
        verify(repository).save(captor.capture());

        SyncEventEntity saved = captor.getValue();
        assertNotNull(saved.getIdempotencyKey());
        assertEquals(SyncDirection.LOCAL_TO_ONLINE, saved.getDirection());
        assertEquals(SyncEnvironment.LOCAL, saved.getSourceEnvironment());
        assertEquals(SyncEnvironment.ONLINE, saved.getTargetEnvironment());
        assertEquals("TestEntity", saved.getAggregateType());
        assertEquals(aggregateId, saved.getAggregateId());
        assertEquals("TEST_EVENT", saved.getEventType());
        assertEquals(payload, saved.getPayload());
        assertEquals(SyncEventStatus.PENDING, saved.getStatus());
    }
}
