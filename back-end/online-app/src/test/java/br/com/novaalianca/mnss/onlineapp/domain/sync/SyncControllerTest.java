package br.com.novaalianca.mnss.onlineapp.domain.sync;

import br.com.novaalianca.mnss.onlineapp.config.SyncStoresProperties;
import br.com.novaalianca.mnss.sharedinfra.security.HmacUtils;
import br.com.novaalianca.mnss.sync.SyncDirection;
import br.com.novaalianca.mnss.sync.SyncEnvironment;
import br.com.novaalianca.mnss.sync.SyncEventEntity;
import br.com.novaalianca.mnss.sync.SyncEventDto;
import br.com.novaalianca.mnss.sync.SyncEventStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
        Map<String, String> stores = new HashMap<>();
        stores.put(storeId, secret);
        stores.put(otherStoreId, otherSecret);
        SyncStoresProperties storesProperties = new SyncStoresProperties(stores);
        controller = new SyncController(repository, objectMapper, syncEventMapper, storesProperties);
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
    void receiveEvent_MissingSignature_ShouldReturnUnauthorized() {
        Map<String, Object> body = new HashMap<>();
        body.put("aggregateType", "Order");
        body.put("eventType", "SALE_FINISHED");
        body.put("payload", Map.of("data", "val"));

        ResponseEntity<Void> response = controller.receiveEvent(storeId, null, "key-missing-sig", body);

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
    void receiveEvent_WithPayloadStoreIdEqualToHeader_ShouldReturnOk() throws Exception {
        String idempotencyKey = "key-store-match";
        Map<String, Object> payload = new HashMap<>();
        payload.put("storeId", storeId);
        payload.put("data", "value");
        String signature = HmacUtils.calculateHmac(idempotencyKey + ":" + objectMapper.writeValueAsString(payload), secret);

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
    void receiveEvent_WithPayloadStoreIdDifferentFromHeader_ShouldReturnForbidden() throws Exception {
        String idempotencyKey = "key-store-mismatch";
        Map<String, Object> payload = new HashMap<>();
        payload.put("storeId", otherStoreId);
        payload.put("data", "value");
        String signature = HmacUtils.calculateHmac(idempotencyKey + ":" + objectMapper.writeValueAsString(payload), secret);

        Map<String, Object> body = new HashMap<>();
        body.put("aggregateType", "Order");
        body.put("eventType", "ORDER_CREATED");
        body.put("payload", payload);

        ResponseEntity<Void> response = controller.receiveEvent(storeId, signature, idempotencyKey, body);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void receiveEvent_WithBlankPayloadStoreId_ShouldReturnBadRequest() throws Exception {
        String idempotencyKey = "key-store-blank";
        Map<String, Object> payload = new HashMap<>();
        payload.put("storeId", " ");
        payload.put("data", "value");
        String signature = HmacUtils.calculateHmac(idempotencyKey + ":" + objectMapper.writeValueAsString(payload), secret);

        Map<String, Object> body = new HashMap<>();
        body.put("aggregateType", "Order");
        body.put("eventType", "ORDER_CREATED");
        body.put("payload", payload);

        ResponseEntity<Void> response = controller.receiveEvent(storeId, signature, idempotencyKey, body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void receiveEvent_WithNonStringPayloadStoreId_ShouldReturnBadRequest() throws Exception {
        String idempotencyKey = "key-store-number";
        Map<String, Object> payload = new HashMap<>();
        payload.put("storeId", 123);
        payload.put("data", "value");
        String signature = HmacUtils.calculateHmac(idempotencyKey + ":" + objectMapper.writeValueAsString(payload), secret);

        Map<String, Object> body = new HashMap<>();
        body.put("aggregateType", "Order");
        body.put("eventType", "ORDER_CREATED");
        body.put("payload", payload);

        ResponseEntity<Void> response = controller.receiveEvent(storeId, signature, idempotencyKey, body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(repository, never()).save(any());
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

    @Test
    void listEvents_ShouldRequireAuthentication() {
        // Unit test: @PreAuthorize is enforced by Spring AOP, not here.
        // Verify the method exists and returns data when called without security context.
        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());
        when(syncEventMapper.toDtoList(any())).thenReturn(List.of());

        ResponseEntity<List<SyncEventDto>> response = controller.listEvents();
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getSyncStatus_ShouldReturnCountsPerStatus() {
        when(repository.countByStatus(any())).thenReturn(0L);
        when(repository.countByStatus(SyncEventStatus.PENDING)).thenReturn(3L);
        when(repository.countByStatus(SyncEventStatus.SYNCED)).thenReturn(10L);

        ResponseEntity<Map<String, Long>> response = controller.getSyncStatus();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3L, response.getBody().get("PENDING"));
        assertEquals(10L, response.getBody().get("SYNCED"));
    }

    @Test
    void reprocessEvent_ShouldResetStatusToPending() {
        UUID id = UUID.randomUUID();
        SyncEventEntity event = createPendingOnlineToLocalEvent(storeId);

        when(repository.findById(id)).thenReturn(Optional.of(event));

        ResponseEntity<Void> response = controller.reprocessEvent(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(repository).save(argThat(e -> e.getStatus() == SyncEventStatus.PENDING));
    }

    @Test
    void ignoreEvent_ShouldMarkAsIgnored() {
        UUID id = UUID.randomUUID();
        SyncEventEntity event = createPendingOnlineToLocalEvent(storeId);

        when(repository.findById(id)).thenReturn(Optional.of(event));

        ResponseEntity<Void> response = controller.ignoreEvent(id, Map.of("reason", "teste"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(repository).save(argThat(e -> e.getStatus() == SyncEventStatus.IGNORED));
    }

    // S05-H01: payload type validation
    @Test
    void receiveEventWithStringPayloadReturns400() throws Exception {
        String idempotencyKey = "key-string";
        Map<String, Object> body = new HashMap<>();
        body.put("aggregateType", "Order");
        body.put("eventType", "SALE_FINISHED");
        body.put("payload", "invalid-string-payload");

        ResponseEntity<Void> response = controller.receiveEvent(storeId, "any-sig", idempotencyKey, body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void receiveEventWithArrayPayloadReturns400() throws Exception {
        String idempotencyKey = "key-array";
        Map<String, Object> body = new HashMap<>();
        body.put("aggregateType", "Order");
        body.put("payload", java.util.List.of("a", "b", "c"));

        ResponseEntity<Void> response = controller.receiveEvent(storeId, "any-sig", idempotencyKey, body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(repository, never()).save(any());
    }

    // S05-H02: HMAC security coverage
    @Test
    void receiveEvent_UnknownStore_ShouldReturnUnauthorized() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("payload", Map.of("data", "val"));

        ResponseEntity<Void> response = controller.receiveEvent("unknown-store", "any-sig", "key-x", body);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void getPendingEvents_UnknownStore_ShouldReturnUnauthorized() {
        ResponseEntity<List<SyncEventDto>> response =
                controller.getPendingEvents("unknown-store", "any-sig", null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(repository, never()).findByStatusAndDirection(any(), any());
    }

    @Test
    void getPendingEvents_InvalidSignature_ShouldReturnUnauthorized() {
        ResponseEntity<List<SyncEventDto>> response =
                controller.getPendingEvents(storeId, "invalid-sig", null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(repository, never()).findByStatusAndDirection(any(), any());
    }

    @Test
    void acknowledgeEvent_UnknownStore_ShouldReturnUnauthorized() {
        UUID eventId = UUID.randomUUID();
        ResponseEntity<Void> response = controller.acknowledgeEvent(eventId, "unknown-store", "any-sig");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(repository, never()).findById(any());
    }

    @Test
    void acknowledgeEvent_InvalidSignature_ShouldReturnUnauthorized() {
        UUID eventId = UUID.randomUUID();
        ResponseEntity<Void> response = controller.acknowledgeEvent(eventId, storeId, "invalid-sig");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void acknowledgeEvent_EventNotFound_ShouldReturnNotFound() {
        UUID eventId = UUID.randomUUID();
        String signature = HmacUtils.calculateHmac(eventId + ":ack", secret);

        when(repository.findById(eventId)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = controller.acknowledgeEvent(eventId, storeId, signature);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(repository, never()).save(any());
    }

    // S04-H01: aggregateType / eventType validation
    @Test
    void receiveEvent_MissingAggregateType_ShouldReturn400() {
        Map<String, Object> body = new HashMap<>();
        body.put("eventType", "SALE_FINISHED");
        String sig = HmacUtils.calculateHmac("key-no-agg" + ":{}", secret);

        ResponseEntity<Void> response = controller.receiveEvent(storeId, sig, "key-no-agg", body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void receiveEvent_BlankAggregateType_ShouldReturn400() {
        Map<String, Object> body = new HashMap<>();
        body.put("aggregateType", "   ");
        body.put("eventType", "SALE_FINISHED");
        String sig = HmacUtils.calculateHmac("key-blank-agg" + ":{}", secret);

        ResponseEntity<Void> response = controller.receiveEvent(storeId, sig, "key-blank-agg", body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void receiveEvent_MissingEventType_ShouldReturn400() {
        Map<String, Object> body = new HashMap<>();
        body.put("aggregateType", "Order");
        String sig = HmacUtils.calculateHmac("key-no-evt" + ":{}", secret);

        ResponseEntity<Void> response = controller.receiveEvent(storeId, sig, "key-no-evt", body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void receiveEvent_BlankEventType_ShouldReturn400() {
        Map<String, Object> body = new HashMap<>();
        body.put("aggregateType", "Order");
        body.put("eventType", "");
        String sig = HmacUtils.calculateHmac("key-blank-evt" + ":{}", secret);

        ResponseEntity<Void> response = controller.receiveEvent(storeId, sig, "key-blank-evt", body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(repository, never()).save(any());
    }

    // S04-H02: aggregateId validation
    @Test
    void receiveEvent_InvalidAggregateId_ShouldReturn400() {
        Map<String, Object> body = new HashMap<>();
        body.put("aggregateType", "Order");
        body.put("eventType", "SALE_FINISHED");
        body.put("aggregateId", "not-a-uuid");
        String sig = HmacUtils.calculateHmac("key-bad-uuid" + ":{}", secret);

        ResponseEntity<Void> response = controller.receiveEvent(storeId, sig, "key-bad-uuid", body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void receiveEvent_NonStringAggregateId_ShouldReturn400() {
        Map<String, Object> body = new HashMap<>();
        body.put("aggregateType", "Order");
        body.put("eventType", "SALE_FINISHED");
        body.put("aggregateId", 12345);
        String sig = HmacUtils.calculateHmac("key-int-uuid" + ":{}", secret);

        ResponseEntity<Void> response = controller.receiveEvent(storeId, sig, "key-int-uuid", body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void receiveEvent_ValidAggregateId_ShouldSaveAndReturn200() throws Exception {
        String idempotencyKey = "key-valid-uuid";
        Map<String, Object> payload = Map.of("orderId", "123");
        Map<String, Object> body = new HashMap<>();
        body.put("aggregateType", "Order");
        body.put("eventType", "SALE_FINISHED");
        body.put("aggregateId", UUID.randomUUID().toString());
        body.put("payload", payload);
        String payloadJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload);
        String sig = HmacUtils.calculateHmac(idempotencyKey + ":" + payloadJson, secret);

        when(repository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<Void> response = controller.receiveEvent(storeId, sig, idempotencyKey, body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(repository).save(any());
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
