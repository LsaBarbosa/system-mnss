package br.com.novaalianca.mnss.localapp.security.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mnss.security.token")
public record AuthTokenProperties(String secret, Duration ttl) {
}
