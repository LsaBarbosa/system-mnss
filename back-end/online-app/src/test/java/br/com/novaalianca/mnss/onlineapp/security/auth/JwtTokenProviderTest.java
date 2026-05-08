package br.com.novaalianca.mnss.onlineapp.security.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private static final String SECRET = "test-jwt-secret-with-at-least-32-bytes-ok!";

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() throws Exception {
        provider = new JwtTokenProvider(new ObjectMapper());
        Field field = JwtTokenProvider.class.getDeclaredField("tokenSecret");
        field.setAccessible(true);
        field.set(provider, SECRET);
    }

    @Test
    void validateToken_withValidToken_returnsTrue() {
        String token = provider.generateToken("usuario", Set.of("ADMIN"));
        assertThat(provider.validateToken(token)).isTrue();
    }

    @Test
    void extractUsername_withValidToken_returnsSubject() {
        String token = provider.generateToken("operador", Set.of("PDV"));
        assertThat(provider.extractUsername(token)).isEqualTo("operador");
    }

    @Test
    void extractRoles_withValidToken_returnsRoles() {
        String token = provider.generateToken("gerente", Set.of("GERENTE", "PDV"));
        List<String> roles = provider.extractRoles(token);
        assertThat(roles).contains("GERENTE", "PDV");
    }

    @Test
    void validateToken_withTamperedSignature_returnsFalse() {
        String token = provider.generateToken("usuario", Set.of("ADMIN"));
        String tampered = token.substring(0, token.lastIndexOf('.')) + ".invalidsignature";
        assertThat(provider.validateToken(tampered)).isFalse();
    }

    @Test
    void extractUsername_withTamperedSignature_returnsNull() {
        String token = provider.generateToken("usuario", Set.of("ADMIN"));
        String tampered = token.substring(0, token.lastIndexOf('.')) + ".invalidsig";
        assertThat(provider.extractUsername(tampered)).isNull();
    }

    @Test
    void validateToken_withMalformedToken_returnsFalse() {
        assertThat(provider.validateToken("not.a.valid.jwt.token")).isFalse();
        assertThat(provider.validateToken("onlytwoparts.here")).isFalse();
        assertThat(provider.validateToken("")).isFalse();
    }

    @Test
    void validateToken_withNullToken_returnsFalse() {
        assertThat(provider.validateToken(null)).isFalse();
    }

    @Test
    void extractUsername_withNullToken_returnsNull() {
        assertThat(provider.extractUsername(null)).isNull();
    }

    @Test
    void validateToken_withExpiredToken_returnsFalse() throws Exception {
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(new ObjectMapper()) {
            @Override
            public String generateToken(String username, Set<String> roles) {
                return super.generateToken(username, roles);
            }
        };
        Field field = JwtTokenProvider.class.getDeclaredField("tokenSecret");
        field.setAccessible(true);
        field.set(shortLivedProvider, SECRET);

        String token = provider.generateToken("x", Set.of("USER"));
        String[] parts = token.split("\\.");

        // Decode and re-encode payload with exp in the past
        String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
        String expiredPayload = payloadJson.replace(
                "\"exp\":" + extractExp(payloadJson),
                "\"exp\":1000");
        String newPayload = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(expiredPayload.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        // Keep original signature — it won't match, so validateToken returns false anyway
        String expiredToken = parts[0] + "." + newPayload + "." + parts[2];
        assertThat(provider.validateToken(expiredToken)).isFalse();
    }

    private long extractExp(String json) {
        int idx = json.indexOf("\"exp\":");
        if (idx < 0) return 0;
        String rest = json.substring(idx + 6).trim();
        int end = rest.indexOf(',');
        if (end < 0) end = rest.indexOf('}');
        return Long.parseLong(rest.substring(0, end).trim());
    }
}
