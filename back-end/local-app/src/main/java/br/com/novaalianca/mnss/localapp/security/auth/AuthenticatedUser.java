package br.com.novaalianca.mnss.localapp.security.auth;

import br.com.novaalianca.mnss.localapp.security.user.RoleName;
import java.util.Set;
import java.util.UUID;

public record AuthenticatedUser(
        UUID id,
        String name,
        String email,
        String username,
        boolean active,
        Set<RoleName> roles) {

    public boolean hasAnyRole(Set<RoleName> requiredRoles) {
        return roles.contains(RoleName.ADMIN) || roles.stream().anyMatch(requiredRoles::contains);
    }
}
