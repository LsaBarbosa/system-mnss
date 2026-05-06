package br.com.novaalianca.mnss.localapp.domain.kds;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import br.com.novaalianca.mnss.localapp.config.MockRepositoriesConfig;
import br.com.novaalianca.mnss.localapp.security.config.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(KdsController.class)
@Import({SecurityConfiguration.class, MockRepositoriesConfig.class})
@TestPropertySource(properties = {
    "mnss.security.enabled=true"
})
class KdsSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KdsService kdsService;

    private final UUID id = UUID.randomUUID();

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void getTickets_AllowedForAtendente() throws Exception {
        when(kdsService.getTickets(any())).thenReturn(List.of());
        mockMvc.perform(get("/api/kds/tickets"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void startTicket_ForbiddenForAtendente() throws Exception {
        mockMvc.perform(patch("/api/kds/tickets/" + id + "/start"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "COZINHA")
    void startTicket_AllowedForCozinha() throws Exception {
        when(kdsService.startTicket(any())).thenReturn(new KdsTicketResponse(id, id, 1L, null, null, KdsTicketStatus.IN_PREPARATION, null, null, null, null, null, List.of()));
        mockMvc.perform(patch("/api/kds/tickets/" + id + "/start"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EXPEDICAO")
    void finishOrder_AllowedForExpedicao() throws Exception {
        mockMvc.perform(patch("/api/kds/orders/" + id + "/finish"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CAIXA")
    void finishOrder_ForbiddenForCaixa() throws Exception {
        mockMvc.perform(patch("/api/kds/orders/" + id + "/finish"))
                .andExpect(status().isForbidden());
    }
}
