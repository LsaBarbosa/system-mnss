package br.com.novaalianca.mnss.localapp.ping;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PingController.class)
@TestPropertySource(properties = {
    "spring.application.name=mnss-local-api",
    "mnss.environment=local",
    "mnss.security.enabled=false"
})
class PingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void pingReturnsLocalApiStatus() throws Exception {
        mockMvc.perform(get("/api/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("pong"))
                .andExpect(jsonPath("$.application").value("mnss-local-api"))
                .andExpect(jsonPath("$.environment").value("local"))
                .andExpect(jsonPath("$.checkedAt").exists());
    }
}
