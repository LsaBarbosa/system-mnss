package br.com.novaalianca.mnss.localapp.security.auth;

import java.time.Instant;
import java.util.UUID;

record AuthTokenClaims(UUID userId, String username, Instant expiresAt) {
}
