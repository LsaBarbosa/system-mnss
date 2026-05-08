package br.com.novaalianca.mnss.sharedinfra.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class CorsConfigurationUnitTest {

    @Test
    void shouldParseSingleOrigin() {
        CorsProperties props = new CorsProperties();
        props.setAllowedOrigins("http://localhost");

        CorsConfiguration config = configurationFor(props, "/api/ping");

        assertThat(config.getAllowedOrigins()).containsExactly("http://localhost");
    }

    @Test
    void shouldParseMultipleOrigins() {
        CorsProperties props = new CorsProperties();
        props.setAllowedOrigins("http://localhost,http://127.0.0.1,https://example.com");

        CorsConfiguration config = configurationFor(props, "/api/ping");

        assertThat(config.getAllowedOrigins())
            .containsExactlyInAnyOrder("http://localhost", "http://127.0.0.1", "https://example.com");
    }

    @Test
    void shouldConfigureAllowedMethods() {
        CorsConfiguration config = configurationFor(new CorsProperties(), "/api/ping");

        assertThat(config.getAllowedMethods())
            .containsExactlyInAnyOrder("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    }

    @Test
    void shouldConfigureAllowedHeaders() {
        CorsConfiguration config = configurationFor(new CorsProperties(), "/api/ping");

        assertThat(config.getAllowedHeaders())
            .containsExactlyInAnyOrder(
                "Authorization", "Content-Type", "X-Store-ID", "X-Signature", "X-Idempotency-Key");
    }

    @Test
    void shouldAllowCredentials() {
        CorsConfiguration config = configurationFor(new CorsProperties(), "/api/ping");

        assertThat(config.getAllowCredentials()).isTrue();
    }

    @Test
    void shouldNotUseWildcardByDefault() {
        CorsProperties props = new CorsProperties();
        String origins = props.getAllowedOrigins();

        assertThat(origins).doesNotContain("*");
    }

    @Test
    void shouldRegisterCorsOnlyForApiPaths() {
        CorsProperties props = new CorsProperties();
        props.setAllowedOrigins("https://example.com");

        assertThat(configurationFor(props, "/api/orders")).isNotNull();
        assertThat(configurationFor(props, "/assets/app.js")).isNull();
    }

    @Test
    void shouldAllowConfiguredOriginAndRejectUnknownOrigin() {
        CorsProperties props = new CorsProperties();
        props.setAllowedOrigins("https://example.com,https://admin.example.com");
        CorsConfiguration config = configurationFor(props, "/api/orders");

        assertThat(config.checkOrigin("https://example.com")).isEqualTo("https://example.com");
        assertThat(config.checkOrigin("https://attacker.example")).isNull();
    }

    @Test
    void shouldFailFastWhenWildcardIsUsedWithCredentials() {
        CorsProperties props = new CorsProperties();
        props.setAllowedOrigins("*");
        props.setAllowCredentials(true);

        assertThatThrownBy(() -> new CorsAutoConfiguration().corsConfigurationSource(props))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mnss.cors.allowed-origins");
    }

    private CorsConfiguration configurationFor(CorsProperties props, String path) {
        CorsConfigurationSource source = new CorsAutoConfiguration().corsConfigurationSource(props);
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", path);
        request.addHeader("Origin", "https://example.com");
        request.addHeader("Access-Control-Request-Method", "GET");
        return source.getCorsConfiguration(request);
    }
}
