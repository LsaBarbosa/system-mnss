package br.com.novaalianca.mnss.localapp.ping;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ping")
class PingController {
    private final String applicationName;
    private final String environment;

    PingController(
            @Value("${spring.application.name}") String applicationName,
            @Value("${mnss.environment}") String environment) {
        this.applicationName = applicationName;
        this.environment = environment;
    }

    @GetMapping
    PingResponse ping() {
        return new PingResponse("pong", applicationName, environment, Instant.now());
    }
}
