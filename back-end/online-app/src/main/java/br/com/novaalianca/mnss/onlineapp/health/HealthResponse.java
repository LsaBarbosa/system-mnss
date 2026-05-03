package br.com.novaalianca.mnss.onlineapp.health;

import java.time.Instant;

record HealthResponse(
        String status,
        String environment,
        boolean offlineCriticalOperation,
        String message,
        Instant checkedAt) {}
