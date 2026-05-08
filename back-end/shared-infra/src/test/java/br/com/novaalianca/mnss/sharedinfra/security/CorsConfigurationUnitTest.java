package br.com.novaalianca.mnss.sharedinfra.security;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigurationUnitTest {

    @Test
    void shouldParseSingleOrigin() {
        CorsProperties props = new CorsProperties();
        props.setAllowedOrigins("http://localhost");

        CorsConfiguration config = new CorsConfiguration();
        for (String origin : props.getAllowedOrigins().split(",")) {
            config.addAllowedOrigin(origin.trim());
        }

        assertThat(config.getAllowedOrigins()).containsExactly("http://localhost");
    }

    @Test
    void shouldParseMultipleOrigins() {
        CorsProperties props = new CorsProperties();
        props.setAllowedOrigins("http://localhost,http://127.0.0.1,https://example.com");

        CorsConfiguration config = new CorsConfiguration();
        for (String origin : props.getAllowedOrigins().split(",")) {
            config.addAllowedOrigin(origin.trim());
        }

        assertThat(config.getAllowedOrigins())
            .containsExactlyInAnyOrder("http://localhost", "http://127.0.0.1", "https://example.com");
    }

    @Test
    void shouldConfigureAllowedMethods() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("PATCH");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");

        assertThat(config.getAllowedMethods())
            .containsExactlyInAnyOrder("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    }

    @Test
    void shouldConfigureAllowedHeaders() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedHeader("Authorization");
        config.addAllowedHeader("Content-Type");
        config.addAllowedHeader("X-Store-ID");
        config.addAllowedHeader("X-Signature");
        config.addAllowedHeader("X-Idempotency-Key");

        assertThat(config.getAllowedHeaders())
            .containsExactlyInAnyOrder(
                "Authorization", "Content-Type", "X-Store-ID", "X-Signature", "X-Idempotency-Key");
    }

    @Test
    void shouldAllowCredentials() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);

        assertThat(config.getAllowCredentials()).isTrue();
    }

    @Test
    void shouldNotUseWildcardByDefault() {
        CorsProperties props = new CorsProperties();
        String origins = props.getAllowedOrigins();

        assertThat(origins).doesNotContain("*");
    }
}
