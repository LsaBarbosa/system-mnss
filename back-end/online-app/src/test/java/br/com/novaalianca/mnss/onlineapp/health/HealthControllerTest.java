package br.com.novaalianca.mnss.onlineapp.health;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.novaalianca.mnss.sharedinfra.health.TechnicalHealthResponse;
import br.com.novaalianca.mnss.sharedinfra.health.TechnicalHealthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class HealthControllerTest {
    private final TechnicalHealthService technicalHealthService = mock(TechnicalHealthService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new HealthController(technicalHealthService))
            .setMessageConverters(new MappingJackson2HttpMessageConverter(
                    new ObjectMapper()
                            .registerModule(new JavaTimeModule())
                            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)))
            .build();

    @Test
    void reportsOnlineOperationHealthUp() throws Exception {
        when(technicalHealthService.inspect(
                "online",
                false,
                "Canais externos, webhooks e sincronizacao via HTTPS"))
                .thenReturn(new TechnicalHealthResponse(
                        "UP",
                        "online",
                        false,
                        "ready",
                        "1.0.0",
                        Instant.parse("2026-05-04T12:00:00Z"),
                        Map.of("db", "UP", "redis", "UP", "rabbit", "UP")));

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.environment").value("online"))
                .andExpect(jsonPath("$.offlineCriticalOperation").value(false))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.timestamp").value("2026-05-04T12:00:00Z"))
                .andExpect(jsonPath("$.components.rabbit").value("UP"));
    }

    @Test
    void reportsOnlineOperationHealthDownWhenDependencyFails() throws Exception {
        when(technicalHealthService.inspect(
                "online",
                false,
                "Canais externos, webhooks e sincronizacao via HTTPS"))
                .thenReturn(new TechnicalHealthResponse(
                        "DOWN",
                        "online",
                        false,
                        "rabbit down",
                        "1.0.0",
                        Instant.parse("2026-05-04T12:00:00Z"),
                        Map.of("db", "UP", "redis", "UP", "rabbit", "DOWN")));

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.components.rabbit").value("DOWN"));
    }
}
