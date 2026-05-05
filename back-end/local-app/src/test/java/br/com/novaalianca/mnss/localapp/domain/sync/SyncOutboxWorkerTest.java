package br.com.novaalianca.mnss.localapp.domain.sync;

import br.com.novaalianca.mnss.sync.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncOutboxWorkerTest {

    @Mock
    private SyncEventRepository repository;

    @Mock
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private SyncOutboxWorker worker;

    @BeforeEach
    void setUp() {
        worker = new SyncOutboxWorker(repository, restTemplate, objectMapper);
        ReflectionTestUtils.setField(worker, "onlineUrl", "http://online");
        ReflectionTestUtils.setField(worker, "storeId", "store-1");
        ReflectionTestUtils.setField(worker, "storeSecret", "secret");
        ReflectionTestUtils.setField(worker, "retryIntervals", "1m,5m");
    }

    private SyncEventEntity createTestEvent() {
        SyncEventEntity event = new SyncEventEntity(
                "key", SyncDirection.LOCAL_TO_ONLINE, SyncEnvironment.LOCAL, SyncEnvironment.ONLINE,
                "Type", "EVENT", Map.of("data", "val"), SyncEventStatus.PENDING
        );
        // BaseEntity auto-generates id and createdAt in constructor, no need for ReflectionTestUtils
        return event;
    }

    @Test
    void processPendingEvents_Success_ShouldMarkAsSynced() {
        SyncEventEntity event = createTestEvent();

        when(repository.findByStatusAndDirection(SyncEventStatus.PENDING, SyncDirection.LOCAL_TO_ONLINE))
                .thenReturn(List.of(event));
        when(repository.findByStatusInAndNextRetryAtBefore(anyList(), any(Instant.class)))
                .thenReturn(List.of());
        when(restTemplate.postForEntity(anyString(), any(), any()))
                .thenReturn(ResponseEntity.ok().build());

        worker.processPendingEvents();

        assertEquals(SyncEventStatus.SYNCED, event.getStatus());
        verify(repository).save(event);
    }

    @Test
    void processPendingEvents_Failure_ShouldMarkAsFailed() {
        SyncEventEntity event = createTestEvent();

        when(repository.findByStatusAndDirection(SyncEventStatus.PENDING, SyncDirection.LOCAL_TO_ONLINE))
                .thenReturn(List.of(event));
        when(repository.findByStatusInAndNextRetryAtBefore(anyList(), any(Instant.class)))
                .thenReturn(List.of());
        when(restTemplate.postForEntity(anyString(), any(), any()))
                .thenReturn(ResponseEntity.status(500).build());

        worker.processPendingEvents();

        assertEquals(SyncEventStatus.FAILED, event.getStatus());
        assertEquals(1, event.getRetryCount());
        verify(repository).save(event);
    }
}
