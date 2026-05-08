package br.com.novaalianca.mnss.onlineapp.security.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.novaalianca.mnss.onlineapp.api.VersionController;
import br.com.novaalianca.mnss.onlineapp.security.auth.JwtTokenProvider;
import br.com.novaalianca.mnss.onlineapp.security.auth.OnlineUserDetailsService;
import br.com.novaalianca.mnss.sharedinfra.security.CorsAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VersionController.class)
@Import({SecurityConfiguration.class, CorsAutoConfiguration.class})
@TestPropertySource(properties = "mnss.security.hsts-enabled=false")
class OnlineSecurityHstsDisabledTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private OnlineUserDetailsService onlineUserDetailsService;

    @Test
    void onlineSecurityDoesNotEmitHstsWhenDisabled() throws Exception {
        mockMvc.perform(get("/api/version").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Strict-Transport-Security"));
    }
}
