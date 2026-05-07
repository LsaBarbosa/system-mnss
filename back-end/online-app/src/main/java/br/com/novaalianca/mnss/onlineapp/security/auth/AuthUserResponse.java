package br.com.novaalianca.mnss.onlineapp.security.auth;

import java.util.Set;

public record AuthUserResponse(
        String id,
        String name,
        String email,
        String username,
        boolean active,
        Set<String> roles
) {}
