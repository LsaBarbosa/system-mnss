package br.com.novaalianca.mnss.localapp.config;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.novaalianca.mnss.sharedinfra.security.CorsAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.cors.CorsConfigurationSource;

class LocalCorsAutoConfigurationContextTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CorsAutoConfiguration.class))
            .withPropertyValues("mnss.cors.allowed-origins=http://localhost,http://127.0.0.1");

    @Test
    void localContextProvidesCorsConfigurationSourceBean() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(CorsConfigurationSource.class);
            assertThat(context.getBean(CorsConfigurationSource.class)).isNotNull();
        });
    }
}
