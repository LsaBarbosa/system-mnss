package br.com.novaalianca.mnss.sharedinfra.health;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.actuate.health.CompositeHealth;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;

public class TechnicalHealthService {
    private final HealthEndpoint healthEndpoint;
    private final Clock clock;

    public TechnicalHealthService(HealthEndpoint healthEndpoint, Clock clock) {
        this.healthEndpoint = healthEndpoint;
        this.clock = clock;
    }

    public TechnicalHealthResponse inspect(
            String environment,
            boolean offlineCriticalOperation,
            String message) {
        HealthComponent health = healthEndpoint.health();
        return new TechnicalHealthResponse(
                health.getStatus().getCode(),
                environment,
                offlineCriticalOperation,
                message,
                Instant.now(clock),
                collectComponentStatuses(health));
    }

    private Map<String, String> collectComponentStatuses(HealthComponent health) {
        if (!(health instanceof CompositeHealth compositeHealth)) {
            return Map.of();
        }

        Map<String, String> components = new LinkedHashMap<>();
        compositeHealth.getComponents().forEach((name, component) ->
                components.put(name, component.getStatus().getCode()));
        return components;
    }
}
