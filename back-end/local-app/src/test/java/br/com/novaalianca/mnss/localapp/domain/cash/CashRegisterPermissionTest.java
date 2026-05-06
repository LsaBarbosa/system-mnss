package br.com.novaalianca.mnss.localapp.domain.cash;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.novaalianca.mnss.localapp.security.auth.AuthService;
import br.com.novaalianca.mnss.localapp.security.config.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CashRegisterController.class)
@Import(SecurityConfiguration.class)
class CashRegisterPermissionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private CashRegisterService cashRegisterService;

    @Test
    @WithMockUser(roles = "CONSULTA")
    void userWithoutPermissionCannotOpenCashRegister() throws Exception {
        mockMvc.perform(post("/api/cash-register/open")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"openingAmount\":10.00}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CAIXA")
    void userWithPermissionCanOpenCashRegister() throws Exception {
        mockMvc.perform(post("/api/cash-register/open")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"openingAmount\":10.00}"))
                .andExpect(status().isCreated());
    }
}
