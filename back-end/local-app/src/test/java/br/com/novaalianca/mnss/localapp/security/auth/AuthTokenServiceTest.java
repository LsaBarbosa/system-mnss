package br.com.novaalianca.mnss.localapp.security.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.novaalianca.mnss.localapp.security.config.AuthTokenProperties;
import br.com.novaalianca.mnss.sharedinfra.web.error.BusinessException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuthTokenServiceTest {
    private static final Instant NOW = Instant.parse("2026-05-04T12:00:00Z");
    private static final String SECRET = "test-secret";
    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Test
    void validatesIssuedToken() {
        AuthTokenService service = service(NOW, Duration.ofMinutes(30));

        String token = service.serialize(service.issue(USER_ID, "admin"));

        AuthTokenClaims claims = service.validate(token);
        assertThat(claims.userId()).isEqualTo(USER_ID);
        assertThat(claims.username()).isEqualTo("admin");
    }

    @Test
    void rejectsExpiredToken() {
        AuthTokenService issuer = service(NOW, Duration.ofSeconds(1));
        String token = issuer.serialize(issuer.issue(USER_ID, "admin"));

        AuthTokenService validator = service(NOW.plusSeconds(2), Duration.ofSeconds(1));

        assertThatThrownBy(() -> validator.validate(token))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Token expirado.");
    }

    private AuthTokenService service(Instant instant, Duration ttl) {
        return new AuthTokenService(
                new AuthTokenProperties(SECRET, ttl),
                Clock.fixed(instant, ZoneOffset.UTC));
    }
}
