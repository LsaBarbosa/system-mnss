package br.com.novaalianca.mnss.onlineapp.domain.sync;

import br.com.novaalianca.mnss.sharedinfra.security.HmacUtils;
import br.com.novaalianca.mnss.sync.SyncEventEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncControllerTest {

    @Mock
    private SyncEventRepository repository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private SyncController controller;

    private final String storeId = "store-1";
    private final String secret = "secret";

    @BeforeEach
    void setUp() {
        controller = new SyncController(repository, objectMapper);
        Map<String, String> stores = new HashMap<>();
        stores.put(storeId, secret);
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
}
