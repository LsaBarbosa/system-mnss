package br.com.novaalianca.mnss.onlineapp;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.novaalianca.mnss.onlineapp.config.OnlineProfileProperties;
import br.com.novaalianca.mnss.onlineapp.config.OnlineProfileConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

/**
 * Valida que as propriedades do perfil online (OnlineProfileProperties) são
 * corretamente resolvidas a partir dos nomes MNSS_* atuais.
 *
 * Não valida startup completo com DB/RabbitMQ — isso é coberto por
 * OnlineSchemaValidationTest com Testcontainers.
 */
@ActiveProfiles("online")
@SpringBootTest(
        classes = OnlineProfileConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",
            "MNSS_ONLINE_DB_URL=jdbc:postgresql://postgres-online:5432/nova_alianca_online",
            "MNSS_ONLINE_DB_USER=nova_alianca",
            "MNSS_ONLINE_DB_PASSWORD=change_me",
            "MNSS_ONLINE_RABBITMQ_HOST=rabbitmq-online",
            "MNSS_ONLINE_RABBITMQ_PORT=5672",
            "MNSS_ONLINE_RABBITMQ_USER=nova_alianca",
            "MNSS_ONLINE_RABBITMQ_PASSWORD=change_me",
            "MNSS_ONLINE_REDIS_HOST=redis-online",
            "MNSS_ONLINE_REDIS_PORT=6379",
            "MNSS_ONLINE_JWT_SECRET=change_me_jwt_secret_at_least_32_bytes_long",
            "SYNC_MASTER_SECRET=change_me_sync_master_secret_32_bytes_ok",
            "SITE_URL=https://padarianovaalianca.com.br",
            "API_URL=https://api.padarianovaalianca.com.br",
            "ADMIN_URL=https://admin.padarianovaalianca.com.br",
            "MNSS_STORE_001_SECRET=change_me",
            "MNSS_PAYMENT_WEBHOOK_SECRET=change_me",
            "WHATSAPP_VERIFY_TOKEN=change_me",
            "SPRING_SECURITY_USER_NAME=online_admin",
            "SPRING_SECURITY_USER_PASSWORD=change_me"
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
        assertThat(onlineProfileProperties.jwtSecret()).isNotBlank();
        assertThat(onlineProfileProperties.syncMasterSecret()).isNotBlank();
    }
}
