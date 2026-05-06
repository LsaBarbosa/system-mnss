package br.com.novaalianca.mnss.onlineapp;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.novaalianca.mnss.onlineapp.config.OnlineProfileProperties;
import br.com.novaalianca.mnss.onlineapp.config.OnlineProfileConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("online")
@SpringBootTest(
        classes = OnlineProfileConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
            "DB_HOST=postgres-online",
            "DB_PORT=5432",
            "POSTGRES_DB=nova_alianca_online",
            "POSTGRES_USER=nova_alianca",
            "POSTGRES_PASSWORD=change_me",
            "RABBITMQ_HOST=rabbitmq-online",
            "RABBITMQ_DEFAULT_USER=nova_alianca",
            "RABBITMQ_DEFAULT_PASS=change_me",
            "REDIS_HOST=redis-online",
            "REDIS_PORT=6379",
            "JWT_SECRET=change_me",
            "SYNC_MASTER_SECRET=change_me",
            "SITE_URL=https://padarianovaalianca.com.br",
            "API_URL=https://api.padarianovaalianca.com.br",
            "ADMIN_URL=https://admin.padarianovaalianca.com.br",
            "mnss.sync.stores={'test-store':'change_me'}"
        })
class NovaAliancaOnlineApplicationTest {
    @Autowired
    private Environment environment;

    @Autowired
    private OnlineProfileProperties onlineProfileProperties;

    @Test
    void contextLoadsWithOnlineProfile() {
        assertThat(environment.getActiveProfiles()).contains("online");
        assertThat(environment.getProperty("mnss.environment")).isEqualTo("online");
        assertThat(onlineProfileProperties.apiUrl()).isEqualTo("https://api.padarianovaalianca.com.br");
    }
}
