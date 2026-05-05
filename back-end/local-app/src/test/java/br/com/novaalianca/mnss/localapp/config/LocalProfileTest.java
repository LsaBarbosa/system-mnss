package br.com.novaalianca.mnss.localapp.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.context.annotation.Import;

@ActiveProfiles("local")
@Import(MockRepositoriesConfig.class)
@SpringBootTest(
    classes = {LocalProfileConfiguration.class},
    properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
    "DB_HOST=postgres-local",
    "DB_PORT=5432",
    "POSTGRES_DB=nova_alianca_local",
    "POSTGRES_USER=nova_alianca",
    "POSTGRES_PASSWORD=change_me",
    "RABBITMQ_HOST=rabbitmq-local",
    "RABBITMQ_DEFAULT_USER=nova_alianca",
    "RABBITMQ_DEFAULT_PASS=change_me",
    "REDIS_HOST=redis-local",
    "REDIS_PORT=6379",
    "ONLINE_SYNC_BASE_URL=https://api.padarianovaalianca.com.br",
    "STORE_ID=nova-alianca-001",
    "STORE_SECRET=change_me",
    "mnss.security.enabled=false"
})
class LocalProfileTest {
    @Autowired
    private Environment environment;

    @Autowired
    private LocalProfileProperties localProfileProperties;

    @Test
    void localProfileLoads() {
        assertThat(environment.getActiveProfiles()).contains("local");
        assertThat(environment.getProperty("mnss.environment")).isEqualTo("local");
        assertThat(localProfileProperties.databaseHost()).isEqualTo("postgres-local");
    }
}
