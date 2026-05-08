package br.com.novaalianca.mnss.onlineapp.domain;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.novaalianca.mnss.onlineapp.domain.order.OnlineOrderRepository;
import br.com.novaalianca.mnss.onlineapp.domain.sync.OnlineLocalSaleSummaryRepository;
import br.com.novaalianca.mnss.onlineapp.domain.sync.SyncEventRepository;
import java.util.List;
import org.flywaydb.core.Flyway;
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

@DataJpaTest(properties = {
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.test.database.replace=none"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class OnlineRepositoryTest {
    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    @Autowired
    private SyncEventRepository syncEventRepository;

    @Autowired
    private OnlineOrderRepository onlineOrderRepository;

    @Autowired
    private OnlineLocalSaleSummaryRepository saleSummaryRepository;

    @Test
    void migrationsCreateExpectedOnlineSchema() {
        flyway.migrate();

        List<String> tables = jdbcTemplate.queryForList(
                "select table_name from information_schema.tables where table_schema = 'public'",
                String.class);

        assertThat(tables)
                .contains(
                        "orders",
                        "sync_events",
                        "online_local_sale_summaries",
                        "stock_balances",
                        "whatsapp_conversations");
    }

    @Test
    void ordersSchemaMatchesJpaMapping() {
        String paymentMethodNullable = jdbcTemplate.queryForObject(
                "select is_nullable from information_schema.columns where table_name = 'orders' and column_name = 'payment_method'",
                String.class);
        String deliveryAddressNullable = jdbcTemplate.queryForObject(
                "select is_nullable from information_schema.columns where table_name = 'orders' and column_name = 'delivery_address_id'",
                String.class);

        assertThat(paymentMethodNullable).isEqualTo("NO");
        assertThat(deliveryAddressNullable).isEqualTo("YES");
    }

    @Test
    void expectedOrderIndexesExist() {
        List<String> indexes = jdbcTemplate.queryForList(
                "select indexname from pg_indexes where schemaname = 'public' and tablename = 'orders'",
                String.class);

        assertThat(indexes)
                .contains("idx_orders_delivery_address_id", "idx_orders_payment_method");
    }

    @Test
    void onlineSpecificColumnsAndConstraintsMatchJpaMappings() {
        List<String> paymentColumns = columnsOf("payments");
        List<String> stockBalanceColumns = columnsOf("stock_balances");
        List<String> whatsappConversationColumns = columnsOf("whatsapp_conversations");
        List<String> whatsappMessageColumns = columnsOf("whatsapp_messages");
        List<String> whatsappMessageIndexes = jdbcTemplate.queryForList(
                "select indexname from pg_indexes where schemaname = 'public' and tablename = 'whatsapp_messages'",
                String.class);
        Integer assignedToFkCount = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.table_constraints "
                        + "where table_schema = 'public' "
                        + "and table_name = 'whatsapp_conversations' "
                        + "and constraint_name = 'fk_whatsapp_conversations_assigned_to' "
                        + "and constraint_type = 'FOREIGN KEY'",
                Integer.class);
        Integer externalMessageUniqueCount = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.table_constraints "
                        + "where table_schema = 'public' "
                        + "and table_name = 'whatsapp_messages' "
                        + "and constraint_type = 'UNIQUE'",
                Integer.class);

        assertThat(paymentColumns).contains("webhook_payload", "version");
        assertThat(stockBalanceColumns)
                .contains("product_id", "quantity", "reserved_quantity", "version", "created_at", "updated_at");
        assertThat(whatsappConversationColumns).contains("assigned_to", "version");
        assertThat(whatsappMessageColumns).contains("external_message_id", "version");
        assertThat(assignedToFkCount).isEqualTo(1);
        assertThat(externalMessageUniqueCount).isGreaterThanOrEqualTo(1);
        assertThat(whatsappMessageIndexes)
                .doesNotContain("idx_whatsapp_messages_external_id")
                .anyMatch(indexName -> indexName.contains("external_message_id"));
    }

    @Test
    void mainRepositoriesAreCreated() {
        assertThat(syncEventRepository).isNotNull();
        assertThat(onlineOrderRepository).isNotNull();
        assertThat(saleSummaryRepository).isNotNull();
    }

    private List<String> columnsOf(String tableName) {
        return jdbcTemplate.queryForList(
                "select column_name from information_schema.columns where table_schema = 'public' and table_name = ?",
                String.class,
                tableName);
    }
}
