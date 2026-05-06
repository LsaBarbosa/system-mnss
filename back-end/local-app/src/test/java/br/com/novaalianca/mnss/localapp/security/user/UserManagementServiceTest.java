package br.com.novaalianca.mnss.localapp.security.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.novaalianca.mnss.localapp.security.auth.PasswordHasher;
import br.com.novaalianca.mnss.sharedinfra.web.error.BusinessException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    private final PasswordHasher passwordHasher = new PasswordHasher();
    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    void duplicateUsernameFails() {
        when(userRepository.existsByUsername("caixa")).thenReturn(true);

        assertThatThrownBy(() -> service().createUser(request(Set.of(RoleName.CAIXA))))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Username ja cadastrado.");
    }

    @Test
    void passwordIsHashedWhenCreatingUser() {
        RoleEntity role = new RoleEntity(RoleName.CAIXA, "caixa");
        when(roleRepository.findByNameIn(anyCollection())).thenReturn(List.of(role));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service().createUser(request(Set.of(RoleName.CAIXA)));

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isNotEqualTo("secret");
        assertThat(passwordHasher.matches("secret", userCaptor.getValue().getPasswordHash())).isTrue();
    }

    @Test
    void userWithoutOperationalRoleIsRefused() {
        assertThatThrownBy(() -> service().createUser(request(Set.of())))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Informe ao menos um perfil operacional.");
    }

    @Test
    void nonexistentRoleFails() {
        when(roleRepository.findByNameIn(anyCollection())).thenReturn(List.of());

        assertThatThrownBy(() -> service().createUser(request(Set.of(RoleName.CAIXA))))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Perfil inexistente.");
    }

    private UserManagementService service() {
        return new UserManagementService(userRepository, roleRepository, passwordHasher, userMapper);
    }

    private CreateUserRequest request(Set<RoleName> roles) {
        return new CreateUserRequest("Caixa", "caixa@local", "caixa", "secret", true, roles);
    }
}
