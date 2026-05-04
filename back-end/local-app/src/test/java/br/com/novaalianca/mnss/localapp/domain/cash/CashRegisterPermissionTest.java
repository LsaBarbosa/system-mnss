package br.com.novaalianca.mnss.localapp.domain.cash;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.novaalianca.mnss.localapp.security.auth.AuthService;
import br.com.novaalianca.mnss.localapp.security.auth.AuthenticatedUser;
import br.com.novaalianca.mnss.localapp.security.auth.AuthenticatedUserInterceptor;
import br.com.novaalianca.mnss.localapp.security.user.RoleName;
import br.com.novaalianca.mnss.sharedinfra.web.error.GlobalExceptionHandler;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CashRegisterPermissionTest {
    @Mock
    private AuthService authService;

    @Mock
    private CashRegisterService cashRegisterService;

    @Test
    void userWithoutPermissionCannotOpenCashRegister() throws Exception {
        when(authService.authenticate("Bearer token")).thenReturn(user(RoleName.CONSULTA));

        mockMvc().perform(post("/api/cash-register/open")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"openingAmount\":10.00}"))
                .andExpect(status().isForbidden());
    }

    private MockMvc mockMvc() {
        return MockMvcBuilders
                .standaloneSetup(new CashRegisterController(cashRegisterService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .addInterceptors(new AuthenticatedUserInterceptor(authService))
                .build();
    }

    private AuthenticatedUser user(RoleName roleName) {
        return new AuthenticatedUser(
                UUID.randomUUID(),
                "User",
                "user@local",
                "user",
                true,
                Set.of(roleName));
    }
}
