package br.com.novaalianca.mnss.onlineapp.security.auth;

public record AuthResponse(
        String token,
        String expiresAt,
        AuthUserResponse user
) {}
