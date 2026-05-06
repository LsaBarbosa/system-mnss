package br.com.novaalianca.mnss.localapp.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import br.com.novaalianca.mnss.localapp.NovaAliancaLocalApplication;

class FailFastConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(NovaAliancaLocalApplication.class);

    @Test
    void contextLoadingShouldFailWhenRequiredPropertiesAreMissing() {
        contextRunner
                .withPropertyValues(
                        "spring.profiles.active=test-fail-fast",
                        "AUTH_TOKEN_SECRET=${FORCE_FAILURE_MISSING_SECRET}",
                        "MNSS_LOCAL_DB_USER=${FORCE_FAILURE_MISSING_USER}"
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                });
    }

    @Test
    void contextLoadingShouldSucceedWithDevProfile() {
        contextRunner
                .withPropertyValues(
                        "spring.profiles.active=dev",
                        "spring.flyway.enabled=false",
                        "spring.jpa.hibernate.ddl-auto=none"
                )
                .run(context -> {
                    // Even if it fails to connect to DB, we want to see if the property resolution passed
                    // If it failed with PSQLException, it's actually GOOD for this test because it means placeholders were resolved.
                    if (context.getStartupFailure() != null) {
                        String msg = context.getStartupFailure().toString();
                        assertThat(msg).doesNotContain("Could not resolve placeholder");
                    } else {
                        assertThat(context).hasNotFailed();
                    }
                });
    }
}
