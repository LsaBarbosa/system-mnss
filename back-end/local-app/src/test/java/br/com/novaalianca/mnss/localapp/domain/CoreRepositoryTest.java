package br.com.novaalianca.mnss.localapp.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.novaalianca.mnss.core.catalog.PreparationSector;
import br.com.novaalianca.mnss.core.catalog.UnitType;
import br.com.novaalianca.mnss.core.payment.PaymentMethod;
import br.com.novaalianca.mnss.core.payment.PaymentStatus;
import br.com.novaalianca.mnss.localapp.domain.audit.AuditLogEntity;
import br.com.novaalianca.mnss.localapp.domain.audit.AuditLogRequest;
import br.com.novaalianca.mnss.localapp.domain.audit.AuditService;
import br.com.novaalianca.mnss.localapp.domain.catalog.CategoryEntity;
import br.com.novaalianca.mnss.localapp.domain.catalog.CategoryRepository;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductEntity;
import br.com.novaalianca.mnss.localapp.domain.catalog.ProductRepository;
import br.com.novaalianca.mnss.localapp.domain.customer.CustomerAddressEntity;
import br.com.novaalianca.mnss.localapp.domain.customer.CustomerAddressRepository;
import br.com.novaalianca.mnss.localapp.domain.order.DeliveryType;
import br.com.novaalianca.mnss.localapp.domain.order.OrderEntity;
import br.com.novaalianca.mnss.localapp.domain.order.OrderOrigin;
import br.com.novaalianca.mnss.localapp.domain.order.OrderRepository;
import br.com.novaalianca.mnss.localapp.domain.order.OrderStatus;
import br.com.novaalianca.mnss.localapp.domain.payment.PaymentEntity;
import br.com.novaalianca.mnss.localapp.domain.payment.PaymentRepository;
import br.com.novaalianca.mnss.localapp.domain.sync.SyncEventRepository;
import br.com.novaalianca.mnss.sync.SyncDirection;
import br.com.novaalianca.mnss.sync.SyncEnvironment;
import br.com.novaalianca.mnss.sync.SyncEventEntity;
import br.com.novaalianca.mnss.sync.SyncEventStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import br.com.novaalianca.mnss.localapp.config.JpaConfiguration;

@DataJpaTest(properties = {
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.test.database.replace=none",
    "mnss.security.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({AuditService.class, JpaConfiguration.class})
@Testcontainers(disabledWithoutDocker = true)
class CoreRepositoryTest {
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
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerAddressRepository customerAddressRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SyncEventRepository syncEventRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void migrationsCreateExpectedTablesOnEmptyDatabase() {
        List<String> tables = jdbcTemplate.queryForList(
                "select table_name from information_schema.tables where table_schema = 'public'",
                String.class);

        assertThat(tables)
                .contains(
                        "roles",
                        "users",
                        "user_roles",
                        "categories",
                        "products",
                        "product_availability",
                        "customers",
                        "customer_addresses",
                        "orders",
                        "order_items",
                        "payments",
                        "cash_registers",
                        "cash_movements",
                        "kds_tickets",
                        "kds_ticket_items",
                        "stock_movements",
                        "sync_events",
                        "audit_logs");
    }

    @Test
    void duplicateMigrationObjectFails() {
        assertThatThrownBy(() -> jdbcTemplate.execute("CREATE TABLE roles (id UUID PRIMARY KEY)"))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void rolesSeedRunsOnlyOnce() {
        Integer countBefore = jdbcTemplate.queryForObject("select count(*) from roles", Integer.class);

        flyway.migrate();

        Integer countAfter = jdbcTemplate.queryForObject("select count(*) from roles", Integer.class);
        assertThat(countBefore).isEqualTo(8);
        assertThat(countAfter).isEqualTo(8);
    }

    @Test
    void repositoryPersistsAndFindsProduct() {
        CategoryEntity category = categoryRepository.save(new CategoryEntity("Paes"));
        ProductEntity product = productRepository.save(new ProductEntity(
                category,
                "Pao frances",
                new BigDecimal("1.20"),
                UnitType.UNIT,
                PreparationSector.SEM_PREPARO));

        assertThat(productRepository.findById(product.getId()))
                .isPresent()
                .get()
                .extracting(ProductEntity::getName)
                .isEqualTo("Pao frances");
        assertThat(productRepository.findById(product.getId()).orElseThrow().isStockControlled()).isFalse();
    }

    @Test
    void auditServicePersistsLogInPostgres() {
        UUID actorUserId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        jdbcTemplate.update(
                "insert into users (id, name, username, password_hash) values (?, ?, ?, ?)",
                actorUserId,
                "Gerente",
                "gerente-audit",
                "hash");

        AuditLogEntity log = auditService.record(new AuditLogRequest(
                actorUserId,
                "ORDER_CANCELED",
                "Order",
                entityId,
                Map.of("reason", "cliente solicitou"),
                "127.0.0.1"));

        assertThat(log.getId()).isNotNull();
        assertThat(log.getAction()).isEqualTo("ORDER_CANCELED");
        entityManager.flush();
        assertThat(jdbcTemplate.queryForObject(
                        "select count(*) from audit_logs where action = 'ORDER_CANCELED'",
                        Integer.class))
                .isEqualTo(1);
    }

    @Test
    void syncEventRepositoryIsCreatedAndPersistsEvent() {
        SyncEventEntity event = new SyncEventEntity(
                "local-test-" + UUID.randomUUID(),
                SyncDirection.LOCAL_TO_ONLINE,
                SyncEnvironment.LOCAL,
                SyncEnvironment.ONLINE,
                "ORDER",
                "SALE_FINISHED",
                Map.of("orderId", UUID.randomUUID().toString()),
                SyncEventStatus.PENDING);

        SyncEventEntity saved = syncEventRepository.save(event);
        entityManager.flush();

        assertThat(syncEventRepository.findByIdempotencyKey(saved.getIdempotencyKey())).isPresent();
    }

    @Test
    void orderPersistsDeliveryAddressMapping() {
        CustomerAddressEntity address = customerAddressRepository.save(new CustomerAddressEntity(
                "Rua Central",
                "123",
                "Centro",
                "Cidade",
                "SP",
                "00000-000"));
        OrderEntity order = new OrderEntity(
                OrderOrigin.SITE,
                OrderStatus.CREATED,
                PaymentStatus.PENDING,
                DeliveryType.DELIVERY);
        order.setDeliveryAddress(address);

        OrderEntity saved = orderRepository.save(order);
        entityManager.flush();
        entityManager.clear();

        assertThat(orderRepository.findById(saved.getId()).orElseThrow().getDeliveryAddress())
                .extracting(CustomerAddressEntity::getStreet)
                .isEqualTo("Rua Central");
    }

    @Test
    void paymentPersistsOnlinePixMethod() {
        OrderEntity order = orderRepository.save(new OrderEntity(
                OrderOrigin.SITE,
                OrderStatus.PAYMENT_PENDING,
                PaymentStatus.PENDING,
                DeliveryType.PICKUP));
        PaymentEntity payment = paymentRepository.save(new PaymentEntity(
                order,
                PaymentMethod.ONLINE_PIX,
                PaymentStatus.PENDING,
                new BigDecimal("19.90")));

        entityManager.flush();
        entityManager.clear();

        assertThat(paymentRepository.findById(payment.getId()).orElseThrow().getMethod())
                .isEqualTo(PaymentMethod.ONLINE_PIX);
    }
}
