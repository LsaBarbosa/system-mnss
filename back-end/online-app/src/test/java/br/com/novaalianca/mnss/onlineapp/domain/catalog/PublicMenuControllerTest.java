package br.com.novaalianca.mnss.onlineapp.domain.catalog;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.novaalianca.mnss.onlineapp.domain.store.StoreInfoResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PublicMenuController.class)
class PublicMenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PublicMenuService service;

    @Test
    void shouldReturnPublicMenu() throws Exception {
        PublicCategoryResponse category = new PublicCategoryResponse(
                UUID.randomUUID(), "Cat 1", "Desc", 1, "url");
        PublicProductResponse product = new PublicProductResponse(
                UUID.randomUUID(), "Prod 1", "Desc", null, null, null, null, null, null);
        PublicMenuResponse menuResponse = new PublicMenuResponse(category, List.of(product));

        when(service.listMenu(null)).thenReturn(List.of(menuResponse));

        mockMvc.perform(get("/api/public/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category.name").value("Cat 1"))
                .andExpect(jsonPath("$[0].products[0].name").value("Prod 1"));
    }

    @Test
    void shouldReturnStoreInfo() throws Exception {
        when(service.getStoreInfo()).thenReturn(new StoreInfoResponse("Padaria", "Rua", "08-20", "123", "Desc"));

        mockMvc.perform(get("/api/public/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Padaria"));
    }
}
