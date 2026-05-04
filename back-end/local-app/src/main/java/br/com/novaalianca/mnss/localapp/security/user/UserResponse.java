package br.com.novaalianca.mnss.localapp.security.user;

import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String username,
        boolean active,
        Set<RoleName> roles) {
}
