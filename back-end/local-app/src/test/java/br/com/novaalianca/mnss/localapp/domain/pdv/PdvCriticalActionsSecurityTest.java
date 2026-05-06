package br.com.novaalianca.mnss.localapp.domain.pdv;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import br.com.novaalianca.mnss.localapp.config.MockRepositoriesConfig;
import br.com.novaalianca.mnss.localapp.security.config.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import br.com.novaalianca.mnss.localapp.domain.payment.PaymentService;

@WebMvcTest(PdvSaleController.class)
@Import({SecurityConfiguration.class, MockRepositoriesConfig.class})
@TestPropertySource(properties = {
    "mnss.security.enabled=true"
})
class PdvCriticalActionsSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PdvSaleService pdvSaleService;

    @MockBean
    private PaymentService paymentService;

    private final UUID id = UUID.randomUUID();

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void cancelSale_ForbiddenForAtendente() throws Exception {
        mockMvc.perform(post("/api/pdv/sales/" + id + "/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\": \"Error\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CAIXA")
    void cancelSale_ForbiddenForCaixa() throws Exception {
        mockMvc.perform(post("/api/pdv/sales/" + id + "/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\": \"Error\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    void cancelSale_AllowedForGerente() throws Exception {
        mockMvc.perform(post("/api/pdv/sales/" + id + "/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\": \"Error\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void reprint_ForbiddenForAtendente() throws Exception {
        mockMvc.perform(post("/api/pdv/sales/" + id + "/print"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CAIXA")
    void reprint_AllowedForCaixa() throws Exception {
        mockMvc.perform(post("/api/pdv/sales/" + id + "/print"))
                .andExpect(status().isNoContent());
    }
}
