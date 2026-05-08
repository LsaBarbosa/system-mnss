package br.com.novaalianca.mnss.onlineapp.config;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.novaalianca.mnss.sharedinfra.security.CorsAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.cors.CorsConfigurationSource;

class OnlineCorsAutoConfigurationContextTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CorsAutoConfiguration.class))
            .withPropertyValues(
                    "mnss.cors.allowed-origins=https://padarianovaalianca.com.br,https://admin.padarianovaalianca.com.br");

    @Test
    void onlineContextProvidesCorsConfigurationSourceBean() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(CorsConfigurationSource.class);
            assertThat(context.getBean(CorsConfigurationSource.class)).isNotNull();
        });
    }
}
