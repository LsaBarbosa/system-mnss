package br.com.novaalianca.mnss.localapp.domain.sync;

import br.com.novaalianca.mnss.sharedinfra.security.HmacUtils;
import br.com.novaalianca.mnss.sync.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Component
public class SyncPullWorker {
    private static final Logger log = LoggerFactory.getLogger(SyncPullWorker.class);

    private final SyncInboxService inboxService;
    private final RestTemplate restTemplate;

    @Value("${mnss.sync.online-url}")
    private String onlineUrl;

    @Value("${mnss.sync.store-id}")
    private String storeId;

    @Value("${mnss.sync.store-secret}")
    private String storeSecret;

    public SyncPullWorker(SyncInboxService inboxService, RestTemplate restTemplate) {
        this.inboxService = inboxService;
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedDelayString = "${mnss.sync.pull-delay:10000}")
    public void pullPendingEvents() {
        try {
            log.debug("Pulling pending events from online...");
            
            String signature = HmacUtils.calculateHmac(storeId + ":pull", storeSecret);
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Store-ID", storeId);
            headers.set("X-Signature", signature);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<List<SyncEventDto>> response = restTemplate.exchange(
                    onlineUrl + "/api/sync/pending",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<SyncEventDto>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<SyncEventDto> events = response.getBody();
                if (!events.isEmpty()) {
                    log.info("Pulled {} events from online", events.size());
                    events.forEach(this::processAndAck);
                }
            }
        } catch (Exception e) {
            log.error("Error pulling sync events: {}", e.getMessage());
        }
    }

    private void processAndAck(SyncEventDto event) {
        try {
            inboxService.processEvent(event);
            sendAck(event.id());
        } catch (Exception e) {
            log.error("Error processing pulled event {}: {}", event.id(), e.getMessage());
        }
    }

    private void sendAck(UUID eventId) {
        try {
            String signature = HmacUtils.calculateHmac(eventId.toString() + ":ack", storeSecret);
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Store-ID", storeId);
            headers.set("X-Signature", signature);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            restTemplate.postForEntity(onlineUrl + "/api/sync/events/" + eventId + "/ack", entity, Void.class);
            log.info("Ack sent for event {}", eventId);
        } catch (Exception e) {
            log.error("Error sending ACK for event {}: {}", eventId, e.getMessage());
        }
    }
}
