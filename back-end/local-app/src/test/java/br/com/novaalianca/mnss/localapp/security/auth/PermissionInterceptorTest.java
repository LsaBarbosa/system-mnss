package br.com.novaalianca.mnss.localapp.security.auth;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.novaalianca.mnss.localapp.security.user.RoleName;
import br.com.novaalianca.mnss.sharedinfra.web.error.GlobalExceptionHandler;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PermissionInterceptorTest {
    @Mock
    private AuthService authService;

    @Test
    void userWithoutPermissionReceivesForbidden() throws Exception {
        when(authService.authenticate("Bearer token")).thenReturn(user(RoleName.CAIXA));

        mockMvc().perform(post("/api/admin/critical-actions/authorize")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminAccessesCriticalActions() throws Exception {
        when(authService.authenticate("Bearer token")).thenReturn(user(RoleName.ADMIN));

        mockMvc().perform(post("/api/admin/critical-actions/authorize")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andExpect(status().isNoContent());
    }

    private MockMvc mockMvc() {
        return MockMvcBuilders
                .standaloneSetup(new CriticalActionController())
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
