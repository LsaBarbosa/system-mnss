package br.com.novaalianca.mnss.localapp.security.user;

import br.com.novaalianca.mnss.localapp.security.auth.PasswordHasher;
import br.com.novaalianca.mnss.sharedinfra.web.error.BusinessException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(prefix = "mnss.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class UserManagementService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordHasher passwordHasher;
    private final UserMapper userMapper;

    public UserManagementService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordHasher passwordHasher,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordHasher = passwordHasher;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(UserEntity::getUsername))
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> listRoles() {
        return roleRepository.findAll().stream()
                .sorted(Comparator.comparing(role -> role.getName().name()))
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException(
                    "USERNAME_DUPLICATE",
                    "Username ja cadastrado.",
                    HttpStatus.CONFLICT);
        }

        Set<RoleEntity> roles = resolveRoles(request.roles());
        UserEntity user = new UserEntity(
                request.name(),
                request.email(),
                request.username(),
                passwordHasher.hash(request.password()),
                request.active() == null || request.active(),
                roles);

        return userMapper.toResponse(userRepository.save(user));
    }

    public Set<RoleEntity> resolveRoles(Set<RoleName> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            throw new BusinessException("ROLE_REQUIRED", "Informe ao menos um perfil operacional.");
        }

        Set<String> roleNameValues = roleNames.stream()
                .map(RoleName::name)
                .collect(Collectors.toSet());
        List<RoleEntity> roles = roleRepository.findByNameIn(roleNameValues);
        Set<String> existingRoleNames = roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
        if (!existingRoleNames.containsAll(roleNameValues)) {
            throw new BusinessException("ROLE_NOT_FOUND", "Perfil inexistente.", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return Set.copyOf(roles);
    }
}
