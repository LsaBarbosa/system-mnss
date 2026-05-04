package br.com.novaalianca.mnss.localapp.security.config;

import br.com.novaalianca.mnss.localapp.security.auth.PasswordHasher;
import br.com.novaalianca.mnss.localapp.security.user.RoleEntity;
import br.com.novaalianca.mnss.localapp.security.user.RoleName;
import br.com.novaalianca.mnss.localapp.security.user.RoleRepository;
import br.com.novaalianca.mnss.localapp.security.user.UserEntity;
import br.com.novaalianca.mnss.localapp.security.user.UserRepository;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(prefix = "mnss.security", name = "enabled", havingValue = "true", matchIfMissing = true)
class InitialSecurityDataSeeder implements ApplicationRunner {
    private static final Map<RoleName, String> ROLE_DESCRIPTIONS = Map.of(
            RoleName.ADMIN, "Acesso administrativo completo",
            RoleName.GERENTE, "Gestao operacional e acoes criticas",
            RoleName.CAIXA, "Operacao de caixa",
            RoleName.ATENDENTE, "Atendimento e pedidos",
            RoleName.COZINHA, "Producao e KDS",
            RoleName.ENTREGADOR, "Entrega de pedidos",
            RoleName.CONSULTA, "Acesso somente leitura");

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final InitialAdminProperties initialAdminProperties;

    InitialSecurityDataSeeder(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            InitialAdminProperties initialAdminProperties) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.initialAdminProperties = initialAdminProperties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedRoles();
        seedAdmin();
    }

    private void seedRoles() {
        ROLE_DESCRIPTIONS.forEach((roleName, description) ->
                roleRepository.findByName(roleName.name())
                        .orElseGet(() -> roleRepository.save(new RoleEntity(roleName, description))));
    }

    private void seedAdmin() {
        if (!initialAdminProperties.enabled()
                || userRepository.existsByUsername(initialAdminProperties.username())) {
            return;
        }

        RoleEntity adminRole = roleRepository.findByName(RoleName.ADMIN.name())
                .orElseThrow(() -> new IllegalStateException("ADMIN role was not created"));
        userRepository.save(new UserEntity(
                initialAdminProperties.name(),
                initialAdminProperties.email(),
                initialAdminProperties.username(),
                passwordHasher.hash(initialAdminProperties.password()),
                true,
                Set.of(adminRole)));
    }
}
