package br.com.novaalianca.mnss.localapp.health;

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
                "local",
                true,
                "PDV, caixa, KDS e banco local devem operar sem internet",
                Instant.now());
    }
}
