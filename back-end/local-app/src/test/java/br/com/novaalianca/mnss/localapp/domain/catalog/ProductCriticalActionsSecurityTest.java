package br.com.novaalianca.mnss.localapp.domain.catalog;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

@WebMvcTest(ProductController.class)
@Import({SecurityConfiguration.class, MockRepositoriesConfig.class})
@TestPropertySource(properties = {
    "mnss.security.enabled=true"
})
class ProductCriticalActionsSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CatalogService catalogService;

    private final UUID id = UUID.randomUUID();

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void updateAvailability_ForbiddenForAtendente() throws Exception {
        mockMvc.perform(patch("/api/products/" + id + "/availability")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"AVAILABLE\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    void updateAvailability_AllowedForGerente() throws Exception {
        mockMvc.perform(patch("/api/products/" + id + "/availability")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"AVAILABLE\"}"))
                .andExpect(status().isOk());
    }
}
