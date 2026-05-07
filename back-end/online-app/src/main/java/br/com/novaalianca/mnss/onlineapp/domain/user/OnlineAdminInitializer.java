package br.com.novaalianca.mnss.onlineapp.domain.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OnlineAdminInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(OnlineAdminInitializer.class);

    @Value("${spring.security.user.name:online_admin}")
    private String adminUsername;

    @Value("${spring.security.user.password:change_me}")
    private String adminPassword;

    private final OnlineUserRepository userRepository;
    private final OnlineRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public OnlineAdminInitializer(OnlineUserRepository userRepository,
                                   OnlineRoleRepository roleRepository,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }
        OnlineRoleEntity adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException(
                        "ADMIN role not found — V3 seed migration may not have run"));

        OnlineUserEntity admin = new OnlineUserEntity(
                adminUsername,
                adminUsername + "@novaalianca.local",
                adminUsername,
                passwordEncoder.encode(adminPassword)
        );
        admin.addRole(adminRole);
        userRepository.save(admin);
        log.info("Initial admin user '{}' created.", adminUsername);
    }
}
