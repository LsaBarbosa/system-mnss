package br.com.novaalianca.mnss.localapp.security.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record CreateUserRequest(
        @NotBlank String name,
        String email,
        @NotBlank String username,
        @NotBlank String password,
        Boolean active,
        @NotEmpty Set<RoleName> roles) {
}
