package br.com.novaalianca.mnss.localapp.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class LocalProfilePropertiesTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
            .withUserConfiguration(LocalProfileConfiguration.class)
            .withPropertyValues("spring.profiles.active=local");

    @Test
    void localProfileFailsFastWithoutDatabaseHost() {
        contextRunner
                .withPropertyValues(
                        "mnss.local.database-host=",
                        "mnss.local.database-port=5432",
                        "mnss.local.database-name=nova_alianca_local",
                        "mnss.local.database-user=nova_alianca",
                        "mnss.local.database-password=change_me",
                        "mnss.local.rabbitmq-host=rabbitmq-local",
                        "mnss.local.rabbitmq-user=nova_alianca",
                        "mnss.local.rabbitmq-password=change_me",
                        "mnss.local.redis-host=redis-local",
                        "mnss.local.redis-port=6379",
                        "mnss.local.online-sync-base-url=https://api.padarianovaalianca.com.br",
                        "mnss.local.store-id=nova-alianca-001",
                        "mnss.local.store-secret=change_me")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasMessageContaining("Could not bind properties to 'LocalProfileProperties'");
                });
    }
}
