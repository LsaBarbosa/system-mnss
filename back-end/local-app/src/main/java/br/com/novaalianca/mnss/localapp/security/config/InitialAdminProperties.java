package br.com.novaalianca.mnss.localapp.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mnss.security.initial-admin")
public record InitialAdminProperties(
        boolean enabled,
        String name,
        String email,
        String username,
        String password) {
}
