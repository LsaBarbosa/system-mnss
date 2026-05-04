package br.com.novaalianca.mnss.localapp.security.config;

import br.com.novaalianca.mnss.localapp.security.auth.AuthTokenService;
import br.com.novaalianca.mnss.localapp.security.auth.PasswordHasher;
import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "mnss.security", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({AuthTokenProperties.class, InitialAdminProperties.class})
class SecurityCoreConfiguration {
    @Bean
    PasswordHasher passwordHasher() {
        return new PasswordHasher();
    }

    @Bean
    AuthTokenService authTokenService(AuthTokenProperties properties, Clock clock) {
        return new AuthTokenService(properties, clock);
    }
}
