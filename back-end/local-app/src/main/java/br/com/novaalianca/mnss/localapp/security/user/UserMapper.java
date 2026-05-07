package br.com.novaalianca.mnss.localapp.security.user;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "roles", source = "user", qualifiedByName = "mapRoleNames")
    UserResponse toResponse(UserEntity user);

    RoleResponse toResponse(RoleEntity role);

    @Named("mapRoleNames")
    default Set<RoleName> mapRoleNames(UserEntity user) {
        return user.getRoles().stream()
                .map(RoleEntity::getName)
                .sorted(Comparator.comparing(Enum::name))
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }
}
