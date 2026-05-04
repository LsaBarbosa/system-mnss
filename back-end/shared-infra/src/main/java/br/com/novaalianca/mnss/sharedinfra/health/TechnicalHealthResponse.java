package br.com.novaalianca.mnss.sharedinfra.health;

import java.time.Instant;
import java.util.Map;

public record TechnicalHealthResponse(
        String status,
        String environment,
        boolean offlineCriticalOperation,
        String message,
        Instant checkedAt,
        Map<String, String> components) {
}
