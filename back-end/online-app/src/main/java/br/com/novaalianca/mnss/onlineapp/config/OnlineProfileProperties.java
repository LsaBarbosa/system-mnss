package br.com.novaalianca.mnss.onlineapp.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "mnss.online")
public record OnlineProfileProperties(
        @NotBlank String jwtSecret,
        @NotBlank String syncMasterSecret,
        @NotBlank String siteUrl,
        @NotBlank String apiUrl,
        @NotBlank String adminUrl) {
}
