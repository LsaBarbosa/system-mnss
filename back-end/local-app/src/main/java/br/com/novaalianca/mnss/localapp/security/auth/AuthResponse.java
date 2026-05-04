package br.com.novaalianca.mnss.localapp.security.auth;

import br.com.novaalianca.mnss.localapp.security.user.UserResponse;
import java.time.Instant;

public record AuthResponse(String token, Instant expiresAt, UserResponse user) {
}
