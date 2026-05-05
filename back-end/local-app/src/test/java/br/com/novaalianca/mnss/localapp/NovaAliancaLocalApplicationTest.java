package br.com.novaalianca.mnss.localapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import br.com.novaalianca.mnss.localapp.config.MockRepositoriesConfig;

@ActiveProfiles("smoke-test")
@Import(MockRepositoriesConfig.class)
@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
    "mnss.security.enabled=false"
})
class NovaAliancaLocalApplicationTest {
    @Test
    void contextLoads() {}
}
