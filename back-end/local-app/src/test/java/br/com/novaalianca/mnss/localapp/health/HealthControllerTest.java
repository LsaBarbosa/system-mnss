package br.com.novaalianca.mnss.localapp.health;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.novaalianca.mnss.sharedinfra.health.TechnicalHealthResponse;
import br.com.novaalianca.mnss.sharedinfra.health.TechnicalHealthService;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class HealthControllerTest {
    private final TechnicalHealthService technicalHealthService = mock(TechnicalHealthService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new HealthController(technicalHealthService))
            .build();

    @Test
    void reportsLocalOperationHealthUp() throws Exception {
        when(technicalHealthService.inspect(
                "local",
                true,
                "PDV, caixa, KDS e banco local devem operar sem internet"))
                .thenReturn(new TechnicalHealthResponse(
                        "UP",
                        "local",
                        true,
                        "ready",
                        Instant.parse("2026-05-04T12:00:00Z"),
                        Map.of("db", "UP", "redis", "UP", "rabbit", "UP")));

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.environment").value("local"))
                .andExpect(jsonPath("$.offlineCriticalOperation").value(true))
                .andExpect(jsonPath("$.components.db").value("UP"));
    }

    @Test
    void reportsLocalOperationHealthDownWhenDependencyFails() throws Exception {
        when(technicalHealthService.inspect(
                "local",
                true,
                "PDV, caixa, KDS e banco local devem operar sem internet"))
                .thenReturn(new TechnicalHealthResponse(
                        "DOWN",
                        "local",
                        true,
                        "redis down",
                        Instant.parse("2026-05-04T12:00:00Z"),
                        Map.of("db", "UP", "redis", "DOWN", "rabbit", "UP")));

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.components.redis").value("DOWN"));
    }
}
