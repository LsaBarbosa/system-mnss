package br.com.novaalianca.mnss.localapp.ping;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import br.com.novaalianca.mnss.localapp.security.auth.AuthService;

import br.com.novaalianca.mnss.localapp.security.config.SecurityConfiguration;
import br.com.novaalianca.mnss.sharedinfra.security.CorsAutoConfiguration;
import org.springframework.context.annotation.Import;

@WebMvcTest(PingController.class)
@Import({SecurityConfiguration.class, CorsAutoConfiguration.class})
@TestPropertySource(properties = {
    "spring.application.name=mnss-local-api",
    "mnss.environment=local",
    "mnss.security.enabled=true"
})
class PingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void pingReturnsLocalApiStatus() throws Exception {
        mockMvc.perform(get("/api/ping"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("pong"))
                .andExpect(jsonPath("$.application").value("mnss-local-api"))
                .andExpect(jsonPath("$.environment").value("local"))
                .andExpect(jsonPath("$.checkedAt").exists());
    }

    @Test
    void localSecurityDoesNotEmitHsts() throws Exception {
        mockMvc.perform(get("/api/ping").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Strict-Transport-Security"));
    }
}
