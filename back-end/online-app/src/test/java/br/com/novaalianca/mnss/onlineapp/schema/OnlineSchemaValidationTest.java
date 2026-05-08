package br.com.novaalianca.mnss.onlineapp.schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Valida que as migrations Flyway online executam e que o Hibernate
 * ddl-auto=validate aceita o schema resultante contra PostgreSQL real.
 */
@DataJpaTest(properties = {
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.test.database.replace=none"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class OnlineSchemaValidationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("nova_alianca_online_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void flywayMigrationsAndHibernateValidatePass() {
        List<String> tables = jdbc.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'",
                String.class);
        assertThat(tables).contains(
                "roles", "users", "categories", "products", "product_availability",
                "customers", "customer_addresses", "orders", "order_items", "payments",
                "sync_events", "stock_balances", "whatsapp_conversations", "whatsapp_messages");
    }

    @Test
    void versionColumnExistsOnAllEntityTables() {
        List<String> entityTables = List.of(
                "roles", "users", "categories", "products", "product_availability",
                "customers", "customer_addresses", "orders", "order_items", "payments",
                "sync_events", "stock_balances",
                "whatsapp_conversations", "whatsapp_messages",
                "online_local_sale_summaries");

        for (String table : entityTables) {
            List<String> cols = jdbc.queryForList(
                    "SELECT column_name FROM information_schema.columns WHERE table_name = ? AND column_name = 'version'",
                    String.class, table);
            assertThat(cols)
                    .as("Tabela '%s' deve ter coluna version (Hibernate @Version)", table)
                    .isNotEmpty();
        }
    }

    @Test
    void stockBalancesHasReservedQuantityAddedByV19() {
        List<String> cols = jdbc.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'stock_balances'",
                String.class);
        assertThat(cols).contains("quantity", "reserved_quantity", "version");

        String notNullable = jdbc.queryForObject(
                "SELECT is_nullable FROM information_schema.columns "
                        + "WHERE table_name = 'stock_balances' AND column_name = 'reserved_quantity'",
                String.class);
        assertThat(notNullable).isEqualTo("NO");
    }

    @Test
    void onlineLocalSaleSummariesSchemaMatchesEntity() {
        List<String> cols = jdbc.queryForList(
                "SELECT column_name FROM information_schema.columns "
                        + "WHERE table_name = 'online_local_sale_summaries'",
                String.class);
        assertThat(cols).contains(
                "store_id", "local_order_id", "order_number",
                "total_amount", "payment_status", "finished_at",
                "raw_payload", "version");

        Integer uniqueCount = jdbc.queryForObject(
                "SELECT count(*) FROM information_schema.table_constraints "
                        + "WHERE table_name = 'online_local_sale_summaries' "
                        + "AND constraint_type = 'UNIQUE'",
                Integer.class);
        assertThat(uniqueCount).isGreaterThanOrEqualTo(1);
    }
}
