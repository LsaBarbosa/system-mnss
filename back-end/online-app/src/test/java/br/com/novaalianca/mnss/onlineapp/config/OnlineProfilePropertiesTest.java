package br.com.novaalianca.mnss.onlineapp.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class OnlineProfilePropertiesTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
            .withUserConfiguration(OnlineProfileConfiguration.class)
            .withPropertyValues("spring.profiles.active=online");

    @Test
    void onlineProfileRequiresJwtSecret() {
        contextRunner
                .withPropertyValues(
                        validDeploymentProperties("mnss.online.jwt-secret="))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasMessageContaining("Could not bind properties to 'OnlineProfileProperties'");
                });
    }

    @Test
    void onlineProfileRequiresSyncMasterSecret() {
        contextRunner
                .withPropertyValues(
                        validDeploymentProperties("mnss.online.sync-master-secret="))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasMessageContaining("Could not bind properties to 'OnlineProfileProperties'");
                });
    }

    private String[] validDeploymentProperties(String override) {
        return new String[] {
            "mnss.online.database-host=postgres-online",
            "mnss.online.database-port=5432",
            "mnss.online.database-name=nova_alianca_online",
            "mnss.online.database-user=nova_alianca",
            "mnss.online.database-password=change_me",
            "mnss.online.rabbitmq-host=rabbitmq-online",
            "mnss.online.rabbitmq-user=nova_alianca",
            "mnss.online.rabbitmq-password=change_me",
            "mnss.online.redis-host=redis-online",
            "mnss.online.redis-port=6379",
            "mnss.online.jwt-secret=change_me",
            "mnss.online.sync-master-secret=change_me",
            "mnss.online.site-url=https://padarianovaalianca.com.br",
            "mnss.online.api-url=https://api.padarianovaalianca.com.br",
            "mnss.online.admin-url=https://admin.padarianovaalianca.com.br",
            "mnss.sync.stores.store-001=secret123",
            override
        };
    }
}
