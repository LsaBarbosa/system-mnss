package br.com.novaalianca.mnss.sharedinfra.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.CompositeHealth;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;

@ExtendWith(MockitoExtension.class)
class TechnicalHealthServiceTest {
    private static final Instant NOW = Instant.parse("2026-05-04T12:00:00Z");

    @Mock
    private HealthEndpoint healthEndpoint;

    @Test
    void reportsUpWhenAllDependenciesAreUp() {
        CompositeHealth actuatorHealth = mock(CompositeHealth.class);
        when(actuatorHealth.getStatus()).thenReturn(Status.UP);
        when(actuatorHealth.getComponents()).thenReturn(Map.of(
                "db", Health.up().build(),
                "redis", Health.up().build(),
                "rabbit", Health.up().build()));
        when(healthEndpoint.health()).thenReturn(actuatorHealth);

        TechnicalHealthResponse response = service().inspect("local", true, "ready");

        assertThat(response.status()).isEqualTo("UP");
        assertThat(response.components())
                .containsEntry("db", "UP")
                .containsEntry("redis", "UP")
                .containsEntry("rabbit", "UP");
        assertThat(response.timestamp()).isEqualTo(NOW);
    }

    @Test
    void reportsDownWhenDependencyFails() {
        CompositeHealth actuatorHealth = mock(CompositeHealth.class);
        when(actuatorHealth.getStatus()).thenReturn(Status.DOWN);
        when(actuatorHealth.getComponents()).thenReturn(Map.of(
                "db", Health.up().build(),
                "redis", Health.down().build(),
                "rabbit", Health.up().build()));
        when(healthEndpoint.health()).thenReturn(actuatorHealth);

        TechnicalHealthResponse response = service().inspect("online", false, "ready");

        assertThat(response.status()).isEqualTo("DOWN");
        assertThat(response.components()).containsEntry("redis", "DOWN");
    }

    private TechnicalHealthService service() {
        return new TechnicalHealthService(healthEndpoint, Clock.fixed(NOW, ZoneOffset.UTC), "0.0.1-TEST");
    }
}
