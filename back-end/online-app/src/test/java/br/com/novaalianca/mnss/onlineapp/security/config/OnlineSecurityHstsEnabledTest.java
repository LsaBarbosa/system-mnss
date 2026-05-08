package br.com.novaalianca.mnss.onlineapp.security.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.novaalianca.mnss.onlineapp.api.VersionController;
import br.com.novaalianca.mnss.onlineapp.security.auth.JwtTokenProvider;
import br.com.novaalianca.mnss.onlineapp.security.auth.OnlineUserDetailsService;
import br.com.novaalianca.mnss.sharedinfra.security.CorsAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

@WebMvcTest(VersionController.class)
@Import({SecurityConfiguration.class, CorsAutoConfiguration.class})
@TestPropertySource(properties = "mnss.security.hsts-enabled=true")
class OnlineSecurityHstsEnabledTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private OnlineUserDetailsService onlineUserDetailsService;

    @Test
    void onlineSecurityEmitsHstsWhenEnabled() throws Exception {
        mockMvc.perform(get("/api/version").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().string("Strict-Transport-Security", "max-age=31536000 ; includeSubDomains"));
    }
}
