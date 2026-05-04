package br.com.novaalianca.mnss.localapp.security.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import br.com.novaalianca.mnss.localapp.security.config.AuthTokenProperties;
import br.com.novaalianca.mnss.localapp.security.user.RoleEntity;
import br.com.novaalianca.mnss.localapp.security.user.RoleName;
import br.com.novaalianca.mnss.localapp.security.user.UserEntity;
import br.com.novaalianca.mnss.localapp.security.user.UserRepository;
import br.com.novaalianca.mnss.sharedinfra.web.error.BusinessException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    private static final Instant NOW = Instant.parse("2026-05-04T12:00:00Z");

    @Mock
    private UserRepository userRepository;

    private final PasswordHasher passwordHasher = new PasswordHasher();

    @Test
    void validLoginReturnsTokenAndUserProfiles() {
        UserEntity user = activeUser();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        AuthResponse response = service().login(new LoginRequest("admin", "secret"));

        assertThat(response.token()).isNotBlank();
        assertThat(response.user().roles()).containsExactly(RoleName.ADMIN);
    }

    @Test
    void invalidPasswordReturnsUnauthorized() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(activeUser()));

        assertThatThrownBy(() -> service().login(new LoginRequest("admin", "wrong")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Credenciais invalidas.");
    }

    @Test
    void inactiveUserIsBlocked() {
        UserEntity inactiveUser = new UserEntity(
                "Admin",
                "admin@novaalianca.local",
                "admin",
                passwordHasher.hash("secret"),
                false,
                Set.of(new RoleEntity(RoleName.ADMIN, "admin")));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(inactiveUser));

        assertThatThrownBy(() -> service().login(new LoginRequest("admin", "secret")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Usuario inativo.");
    }

    private AuthService service() {
        AuthTokenService tokenService = new AuthTokenService(
                new AuthTokenProperties("test-secret", Duration.ofHours(8)),
                Clock.fixed(NOW, ZoneOffset.UTC));
        return new AuthService(userRepository, passwordHasher, tokenService);
    }

    private UserEntity activeUser() {
        return new UserEntity(
                "Admin",
                "admin@novaalianca.local",
                "admin",
                passwordHasher.hash("secret"),
                true,
                Set.of(new RoleEntity(RoleName.ADMIN, "admin")));
    }
}
