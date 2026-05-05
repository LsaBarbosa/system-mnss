package br.com.novaalianca.mnss.onlineapp.domain.sync;

import org.springframework.boot.actuate.health.Health;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/sync/health")
public class SyncHealthController {

    private final SyncHealthIndicator syncHealthIndicator;

    public SyncHealthController(SyncHealthIndicator syncHealthIndicator) {
        this.syncHealthIndicator = syncHealthIndicator;
    }

    @GetMapping
    public Map<String, Object> getSyncHealth() {
        Health health = syncHealthIndicator.health();
        return Map.of(
                "status", health.getStatus().getCode(),
                "details", health.getDetails()
        );
    }
}
