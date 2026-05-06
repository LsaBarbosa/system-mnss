package br.com.novaalianca.mnss.onlineapp.domain.sync;

import br.com.novaalianca.mnss.sharedinfra.security.HmacUtils;
import br.com.novaalianca.mnss.sync.SyncDirection;
import br.com.novaalianca.mnss.sync.SyncEnvironment;
import br.com.novaalianca.mnss.sync.SyncEventEntity;
import br.com.novaalianca.mnss.sync.SyncEventStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncControllerTest {

    @Mock
    private SyncEventRepository repository;

    @Mock
    private SyncEventMapper syncEventMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private SyncController controller;

    private final String storeId = "store-001";
    private final String secret = "secret-001";
    private final String otherStoreId = "store-002";
    private final String otherSecret = "secret-002";

    @BeforeEach
    void setUp() {
        controller = new SyncController(repository, objectMapper, syncEventMapper);
        Map<String, String> stores = new HashMap<>();
        stores.put(storeId, secret);
        stores.put(otherStoreId, otherSecret);
        ReflectionTestUtils.setField(controller, "storeSecrets", stores);
    }

    @Test
    void receiveEvent_Success_ShouldReturnOk() throws Exception {
        String idempotencyKey = "key-1";
        Map<String, Object> payload = Map.of("data", "val");
        String payloadJson = objectMapper.writeValueAsString(payload);
        String signature = HmacUtils.calculateHmac(idempotencyKey + ":" + payloadJson, secret);

        Map<String, Object> body = new HashMap<>();
        body.put("aggregateType", "Order");
        body.put("eventType", "SALE_FINISHED");
        body.put("payload", payload);

        when(repository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = controller.receiveEvent(storeId, signature, idempotencyKey, body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(repository).save(any(SyncEventEntity.class));
    }

    @Test
    void receiveEvent_InvalidSignature_ShouldReturnUnauthorized() throws Exception {
        String idempotencyKey = "key-1";
        Map<String, Object> payload = Map.of("data", "val");
        String signature = "invalid-sig";

        Map<String, Object> body = new HashMap<>();
        body.put("payload", payload);

        ResponseEntity<Void> response = controller.receiveEvent(storeId, signature, idempotencyKey, body);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void receiveEvent_Duplicate_ShouldReturnOkWithoutSaving() throws Exception {
        String idempotencyKey = "key-1";
        Map<String, Object> payload = Map.of("data", "val");
        String payloadJson = objectMapper.writeValueAsString(payload);
        String signature = HmacUtils.calculateHmac(idempotencyKey + ":" + payloadJson, secret);

        Map<String, Object> body = new HashMap<>();
        body.put("payload", payload);

        when(repository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(mock(SyncEventEntity.class)));

        ResponseEntity<Void> response = controller.receiveEvent(storeId, signature, idempotencyKey, body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void receiveEvent_ShouldPersistStoreIdIntoPayload() throws Exception {
        String idempotencyKey = "key-2";
        Map<String, Object> payload = new HashMap<>();
        payload.put("data", "value");
        String payloadJson = objectMapper.writeValueAsString(payload);
        String signature = HmacUtils.calculateHmac(idempotencyKey + ":" + payloadJson, secret);

        Map<String, Object> body = new HashMap<>();
        body.put("aggregateType", "Order");
        body.put("eventType", "ORDER_CREATED");
        body.put("payload", payload);

        when(repository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = controller.receiveEvent(storeId, signature, idempotencyKey, body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(repository).save(argThat(event -> storeId.equals(event.getPayload().get("storeId"))));
    }

    @Test
    void getPendingEvents_ShouldReturnOnlyEventsOwnedByStore() {
        String signature = HmacUtils.calculateHmac(storeId + ":pull", secret);
        SyncEventEntity ownedEvent = createPendingOnlineToLocalEvent(storeId);
        SyncEventEntity otherEvent = createPendingOnlineToLocalEvent(otherStoreId);

        when(repository.findByStatusAndDirection(SyncEventStatus.PENDING, SyncDirection.ONLINE_TO_LOCAL))
                .thenReturn(List.of(ownedEvent, otherEvent));
        when(syncEventMapper.toDto(any())).thenAnswer(invocation -> {
            SyncEventEntity event = invocation.getArgument(0);
            return new SyncEventDto(
                    event.getId(),
                    event.getIdempotencyKey(),
                    event.getDirection(),
                    event.getSourceEnvironment(),
                    event.getTargetEnvironment(),
                    event.getAggregateType(),
                    event.getAggregateId(),
                    event.getEventType(),
                    event.getPayload(),
                    event.getStatus(),
                    event.getRetryCount(),
                    event.getNextRetryAt(),
                    event.getLastError(),
                    event.getProcessedAt(),
                    event.getCreatedAt(),
                    event.getUpdatedAt()
            );
        });

        ResponseEntity<List<SyncEventDto>> response = controller.getPendingEvents(storeId, signature, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(storeId, response.getBody().get(0).payload().get("storeId"));
    }

    @Test
    void getPendingEvents_WithQueryStoreIdDifferentFromHeader_ShouldReturnForbidden() {
        String signature = HmacUtils.calculateHmac(storeId + ":pull", secret);

        ResponseEntity<List<SyncEventDto>> response = controller.getPendingEvents(
                storeId,
                signature,
                otherStoreId);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(repository, never()).findByStatusAndDirection(any(), any());
    }

    @Test
    void acknowledgeEvent_WhenStoreDoesNotOwnEvent_ShouldReturnForbidden() {
        UUID eventId = UUID.randomUUID();
        String signature = HmacUtils.calculateHmac(eventId + ":ack", secret);
        SyncEventEntity event = createPendingOnlineToLocalEvent(otherStoreId);

        when(repository.findById(eventId)).thenReturn(Optional.of(event));

        ResponseEntity<Void> response = controller.acknowledgeEvent(eventId, storeId, signature);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(repository, never()).save(any());
    }

    private SyncEventEntity createPendingOnlineToLocalEvent(String ownerStoreId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("storeId", ownerStoreId);
        payload.put("orderId", UUID.randomUUID().toString());

        return new SyncEventEntity(
                UUID.randomUUID().toString(),
                SyncDirection.ONLINE_TO_LOCAL,
                br.com.novaalianca.mnss.sync.SyncEnvironment.ONLINE,
                br.com.novaalianca.mnss.sync.SyncEnvironment.LOCAL,
                "ORDER",
                "ORDER_CREATED",
                payload,
                SyncEventStatus.PENDING
        );
    }
}
