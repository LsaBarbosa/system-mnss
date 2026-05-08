package br.com.novaalianca.mnss.localapp.schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import br.com.novaalianca.mnss.localapp.config.JpaConfiguration;

/**
 * Valida que Flyway executa todas as migrations locais e que o Hibernate
 * com ddl-auto=validate aceita o schema resultante.
 * Falha se qualquer coluna mapeada por JPA estiver ausente no banco.
 */
@DataJpaTest(properties = {
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.test.database.replace=none",
    "mnss.security.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class LocalSchemaValidationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("nova_alianca_local_test")
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
                "cash_registers", "cash_movements", "kds_tickets", "kds_ticket_items",
                "stock_movements", "stock_balances", "sync_events", "audit_logs");
    }

    @Test
    void versionColumnExistsOnAllEntityTables() {
        List<String> entityTables = List.of(
                "roles", "users", "categories", "products", "product_availability",
                "customers", "customer_addresses", "orders", "order_items", "payments",
                "cash_registers", "cash_movements", "kds_tickets", "kds_ticket_items",
                "stock_movements", "stock_balances", "sync_events", "audit_logs");

        for (String table : entityTables) {
            List<String> columns = jdbc.queryForList(
                    "SELECT column_name FROM information_schema.columns WHERE table_name = ? AND column_name = 'version'",
                    String.class, table);
            assertThat(columns)
                    .as("Tabela '%s' deve ter coluna version (Hibernate @Version)", table)
                    .isNotEmpty();
        }
    }

    @Test
    void stockBalancesHasReservedQuantityAndIndexes() {
        List<String> cols = jdbc.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'stock_balances'",
                String.class);
        assertThat(cols).contains("product_id", "quantity", "reserved_quantity", "version");

        List<String> indexes = jdbc.queryForList(
                "SELECT indexname FROM pg_indexes WHERE tablename = 'stock_balances'",
                String.class);
        assertThat(indexes).contains("idx_stock_balances_product_id");
    }

    @Test
    void stockMovementsHasExtendedColumnsFromV10() {
        List<String> cols = jdbc.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'stock_movements'",
                String.class);
        assertThat(cols).contains(
                "previous_quantity", "resulting_quantity",
                "source", "reference_type", "reference_id", "idempotency_key");
    }

    @Test
    void customerAddressCustomerIdIsNullable() {
        String isNullable = jdbc.queryForObject(
                "SELECT is_nullable FROM information_schema.columns "
                        + "WHERE table_name = 'customer_addresses' AND column_name = 'customer_id'",
                String.class);
        assertThat(isNullable).isEqualTo("YES");
    }

    @Test
    void ordersHasDeliveryAddressAndEnumCheckConstraints() {
        List<String> cols = jdbc.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'orders'",
                String.class);
        assertThat(cols).contains("delivery_address_id", "delivery_type", "payment_status", "origin", "status");

        Integer checkCount = jdbc.queryForObject(
                "SELECT count(*) FROM information_schema.table_constraints "
                        + "WHERE table_name = 'orders' AND constraint_type = 'CHECK'",
                Integer.class);
        assertThat(checkCount).isGreaterThanOrEqualTo(4);
    }

    @Test
    void syncEventsHasCompositeStatusDirectionIndex() {
        List<String> indexes = jdbc.queryForList(
                "SELECT indexname FROM pg_indexes WHERE tablename = 'sync_events'",
                String.class);
        assertThat(indexes).contains("idx_sync_events_status_direction");
    }
}
