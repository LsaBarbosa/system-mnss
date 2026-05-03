package br.com.novaalianca.mnss.onlineapp.health;

import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
class HealthController {
    @GetMapping
    HealthResponse health() {
        return new HealthResponse(
                "UP",
                "online",
                false,
                "Canais externos, webhooks e sincronizacao via HTTPS",
                Instant.now());
    }
}
