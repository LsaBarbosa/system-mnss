package br.com.novaalianca.mnss.localapp.security.user;

import java.util.UUID;

public record RoleResponse(UUID id, RoleName name, String description) {
}
