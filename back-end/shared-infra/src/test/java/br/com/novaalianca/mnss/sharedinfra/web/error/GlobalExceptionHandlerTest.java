package br.com.novaalianca.mnss.sharedinfra.web.error;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AutoConfigureMockMvc
@SpringBootTest(classes = GlobalExceptionHandlerTest.TestApplication.class)
class GlobalExceptionHandlerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void validationErrorsReturnBadRequestWithFieldDetails() throws Exception {
        mockMvc.perform(post("/test/errors/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Campos invalidos na requisicao."))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/test/errors/validation"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.validationErrors[0].field").value("name"));
    }

    @Test
    void businessErrorsReturnDeclaredStatusAndCode() throws Exception {
        mockMvc.perform(get("/test/errors/business"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CATALOG_DUPLICATE"))
                .andExpect(jsonPath("$.message").value("Categoria ja cadastrada."))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/test/errors/business"));
    }

    @Test
    void unexpectedErrorsDoNotLeakStacktraceOrInternalMessage() throws Exception {
        mockMvc.perform(get("/test/errors/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Erro interno inesperado."))
                .andExpect(jsonPath("$.message", not(containsString("segredo"))))
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist());
    }

    @RestController
    @RequestMapping("/test/errors")
    static class TestController {
        @PostMapping("/validation")
        void validation(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/business")
        void business() {
            throw new BusinessException("CATALOG_DUPLICATE", "Categoria ja cadastrada.", HttpStatus.CONFLICT);
        }

        @GetMapping("/unexpected")
        void unexpected() {
            throw new IllegalStateException("segredo interno");
        }
    }

    record TestRequest(@NotBlank String name) {
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({GlobalExceptionHandler.class, TestController.class})
    static class TestApplication {
    }
}
