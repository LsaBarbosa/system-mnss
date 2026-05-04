package br.com.novaalianca.mnss.localapp.security.user;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

public final class UserMapper {
    private UserMapper() {
    }

    public static UserResponse toResponse(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getUsername(),
                user.isActive(),
                roleNames(user));
    }

    public static RoleResponse toResponse(RoleEntity role) {
        return new RoleResponse(role.getId(), role.getName(), role.getDescription());
    }

    public static Set<RoleName> roleNames(UserEntity user) {
        return user.getRoles().stream()
                .map(RoleEntity::getName)
                .sorted(Comparator.comparing(Enum::name))
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }
}
