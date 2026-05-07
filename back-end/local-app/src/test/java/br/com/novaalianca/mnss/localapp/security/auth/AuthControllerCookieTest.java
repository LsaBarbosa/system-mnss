package br.com.novaalianca.mnss.localapp.security.auth;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.novaalianca.mnss.localapp.security.user.RoleName;
import br.com.novaalianca.mnss.localapp.security.user.UserResponse;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Testa que o atributo Secure do cookie de autenticação é configurável
 * via mnss.security.cookie.secure (S09-H01).
 */
class AuthControllerCookieTest {

    @Test
    void cookieHasSecureFlagWhenConfigured() {
        AuthController controller = new AuthController(mockAuthService(), true);
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.login(new LoginRequest("user", "pass"), response);

        String setCookie = response.getHeader("Set-Cookie");
        assertThat(setCookie).contains("Secure");
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("SameSite=Strict");
    }

    @Test
    void cookieDoesNotHaveSecureFlagWhenDisabled() {
        AuthController controller = new AuthController(mockAuthService(), false);
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.login(new LoginRequest("user", "pass"), response);

        String setCookie = response.getHeader("Set-Cookie");
        assertThat(setCookie).doesNotContain("Secure");
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("SameSite=Strict");
    }

    private AuthService mockAuthService() {
        return new AuthService(null, null, null, null) {
            @Override
            public AuthResponse login(LoginRequest request) {
                UserResponse user = new UserResponse(
                        UUID.randomUUID(), "Test", "test@example.com", "test", true, Set.of(RoleName.ADMIN));
                return new AuthResponse("test-token", Instant.now().plusSeconds(3600), user);
            }
        };
    }
}
