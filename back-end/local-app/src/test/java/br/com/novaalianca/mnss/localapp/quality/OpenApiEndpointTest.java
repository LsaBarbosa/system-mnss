package br.com.novaalianca.mnss.localapp.quality;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
    "mnss.security.enabled=false"
})
class OpenApiEndpointTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void exposesOpenApiContractEndpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.paths").exists())
                .andExpect(jsonPath("$.paths['/api/categories']").exists())
                .andExpect(jsonPath("$.paths['/api/categories/{id}']").exists())
                .andExpect(jsonPath("$.paths['/api/products']").exists())
                .andExpect(jsonPath("$.paths['/api/products/{id}']").exists())
                .andExpect(jsonPath("$.paths['/api/products/barcode/{barcode}']").exists())
                .andExpect(jsonPath("$.paths['/api/products/{id}/availability']").exists())
                .andExpect(jsonPath("$.paths['/api/pdv/products']").exists())
                .andExpect(jsonPath("$.paths['/api/cash-register/open']").exists())
                .andExpect(jsonPath("$.paths['/api/cash-register/current']").exists())
                .andExpect(jsonPath("$.paths['/api/cash-register/{id}/movement']").exists())
                .andExpect(jsonPath("$.paths['/api/cash-register/{id}/close']").exists())
                .andExpect(jsonPath("$.paths['/api/cash-register/{id}/summary']").exists())
                .andExpect(jsonPath("$.paths['/api/stock-movements']").exists())
                .andExpect(jsonPath("$.paths['/api/stock-movements/balances']").exists())
                .andExpect(jsonPath("$.paths['/api/public/menu']").exists());
    }
}
