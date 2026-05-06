package br.com.novaalianca.mnss.onlineapp.security.auth;

public record AuthUserResponse(
        String id,
        String name,
        String email,
        String username,
        boolean active,
        boolean authenticated
) {}
