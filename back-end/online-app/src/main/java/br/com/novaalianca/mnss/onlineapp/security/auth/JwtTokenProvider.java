package br.com.novaalianca.mnss.onlineapp.security.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class JwtTokenProvider {

    @Value("${mnss.online.jwt-secret}")
    private String tokenSecret;

    private final ObjectMapper objectMapper;

    public JwtTokenProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String generateToken(String username, Set<String> roles) {
        long expiresAt = System.currentTimeMillis() + (8L * 60 * 60 * 1000);
        String header = b64url(toJson(Map.of("alg", "HS256", "typ", "JWT")));
        String payload = b64url(toJson(Map.of(
                "sub", username,
                "exp", expiresAt,
                "roles", normalizeRoles(roles))));
        String sig     = hmacSha256(header + "." + payload);
        return header + "." + payload + "." + sig;
    }

    public String extractUsername(String token) {
        try {
            Map<String, Object> claims = decodeClaims(token);
            Object subject = claims.get("sub");
            if (!(subject instanceof String value) || value.isBlank()) {
                return null;
            }
            return value;
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> extractRoles(String token) {
        try {
            Map<String, Object> claims = decodeClaims(token);
            Object rolesClaim = claims.get("roles");
            if (!(rolesClaim instanceof List<?> rolesList)) {
                return List.of();
            }
            return rolesList.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public boolean validateToken(String token) {
        try {
            Map<String, Object> claims = decodeClaims(token);
            Object expiresAtClaim = claims.get("exp");
            long expiresAt = parseLongClaim(expiresAtClaim);
            return System.currentTimeMillis() < expiresAt;
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, Object> decodeClaims(String token) {
        String[] parts = splitAndVerify(token);
        if (parts == null) {
            throw new IllegalArgumentException("Invalid token");
        }
        String json = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        return readJson(json);
    }

    private String[] splitAndVerify(String token) {
        if (token == null) return null;
        String[] parts = token.split("\\.");
        if (parts.length != 3) return null;
        String expected = hmacSha256(parts[0] + "." + parts[1]);
        return expected.equals(parts[2]) ? parts : null;
    }

    private String b64url(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String hmacSha256(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(tokenSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign token", e);
        }
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize token payload", e);
        }
    }

    private Map<String, Object> readJson(String payload) {
        try {
            return objectMapper.readValue(payload, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token payload", e);
        }
    }

    private long parseLongClaim(Object claimValue) {
        if (claimValue instanceof Number number) {
            return number.longValue();
        }
        if (claimValue instanceof String text && !text.isBlank()) {
            return Long.parseLong(text.trim());
        }
        throw new IllegalArgumentException("Missing exp claim");
    }

    private Set<String> normalizeRoles(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of("USER");
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String role : roles) {
            if (role == null || role.isBlank()) {
                continue;
            }
            String trimmed = role.trim();
            normalized.add(trimmed.startsWith("ROLE_") ? trimmed.substring(5) : trimmed);
        }
        if (normalized.isEmpty()) {
            normalized.add("USER");
        }
        return normalized;
    }
}
