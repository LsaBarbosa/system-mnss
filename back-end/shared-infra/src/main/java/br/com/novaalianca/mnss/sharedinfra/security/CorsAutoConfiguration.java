package br.com.novaalianca.mnss.sharedinfra.security;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@AutoConfiguration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsAutoConfiguration {
    private static final List<String> ALLOWED_METHODS = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    private static final List<String> ALLOWED_HEADERS = List.of(
            "Authorization",
            "Content-Type",
            "X-Store-ID",
            "X-Signature",
            "X-Idempotency-Key");

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration config = new CorsConfiguration();
        List<String> allowedOrigins = parseOrigins(corsProperties.getAllowedOrigins());
        if (Boolean.TRUE.equals(corsProperties.getAllowCredentials()) && allowedOrigins.contains("*")) {
            throw new IllegalStateException(
                    "mnss.cors.allowed-origins cannot contain '*' when mnss.cors.allow-credentials=true");
        }

        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(ALLOWED_METHODS);
        config.setAllowedHeaders(ALLOWED_HEADERS);
        config.setAllowCredentials(corsProperties.getAllowCredentials());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    private List<String> parseOrigins(String origins) {
        if (origins == null || origins.isBlank()) {
            return List.of();
        }
        return Arrays.stream(origins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();
    }
}
