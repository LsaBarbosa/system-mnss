package br.com.novaalianca.mnss.localapp.security.auth;

import br.com.novaalianca.mnss.localapp.security.config.AuthTokenProperties;
import br.com.novaalianca.mnss.sharedinfra.web.error.BusinessException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.HttpStatus;

public class AuthTokenService {
    private static final String HMAC_SHA256 = "HmacSHA256";
    private final AuthTokenProperties properties;
    private final Clock clock;

    public AuthTokenService(AuthTokenProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public AuthTokenClaims issue(UUID userId, String username) {
        Duration ttl = properties.ttl();
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalStateException("Auth token ttl must be positive");
        }

        return new AuthTokenClaims(userId, username, Instant.now(clock).plus(ttl));
    }

    public String serialize(AuthTokenClaims claims) {
        String payload = claims.userId() + "\n" + claims.username() + "\n" + claims.expiresAt().getEpochSecond();
        String encodedPayload = encode(payload.getBytes(StandardCharsets.UTF_8));
        return encodedPayload + "." + encode(sign(encodedPayload));
    }

    public AuthTokenClaims validate(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 2 || !java.security.MessageDigest.isEqual(sign(parts[0]), decode(parts[1]))) {
            throw unauthorized("Token invalido.");
        }

        try {
            String payload = new String(decode(parts[0]), StandardCharsets.UTF_8);
            String[] fields = payload.split("\n");
            if (fields.length != 3) {
                throw unauthorized("Token invalido.");
            }

            AuthTokenClaims claims = new AuthTokenClaims(
                    UUID.fromString(fields[0]),
                    fields[1],
                    Instant.ofEpochSecond(Long.parseLong(fields[2])));
            if (!claims.expiresAt().isAfter(Instant.now(clock))) {
                throw unauthorized("Token expirado.");
            }
            return claims;
        } catch (IllegalArgumentException exception) {
            throw unauthorized("Token invalido.");
        }
    }

    private byte[] sign(String encodedPayload) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret().getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            return mac.doFinal(encodedPayload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign auth token", exception);
        }
    }

    private String secret() {
        if (properties.secret() == null || properties.secret().isBlank()) {
            throw new IllegalStateException("Auth token secret is required");
        }
        return properties.secret();
    }

    private String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private byte[] decode(String value) {
        try {
            return Base64.getUrlDecoder().decode(value);
        } catch (IllegalArgumentException exception) {
            throw unauthorized("Token invalido.");
        }
    }

    private BusinessException unauthorized(String message) {
        return new BusinessException("AUTH_TOKEN_INVALID", message, HttpStatus.UNAUTHORIZED);
    }
}
