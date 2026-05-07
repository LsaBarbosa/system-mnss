package br.com.novaalianca.mnss.localapp.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Configuration
public class MockRepositoriesConfig {
    @MockitoBean private br.com.novaalianca.mnss.localapp.domain.payment.PaymentRepository paymentRepository;
    @MockitoBean private br.com.novaalianca.mnss.localapp.domain.order.OrderRepository orderRepository;
    @MockitoBean private br.com.novaalianca.mnss.localapp.domain.order.OrderItemRepository orderItemRepository;
    @MockitoBean private br.com.novaalianca.mnss.localapp.domain.cash.CashRegisterRepository cashRegisterRepository;
    @MockitoBean private br.com.novaalianca.mnss.localapp.domain.cash.CashMovementRepository cashMovementRepository;
    @MockitoBean private br.com.novaalianca.mnss.localapp.domain.stock.StockMovementRepository stockMovementRepository;
    @MockitoBean private br.com.novaalianca.mnss.localapp.domain.kds.KdsTicketRepository kdsTicketRepository;
    @MockitoBean private br.com.novaalianca.mnss.localapp.domain.kds.KdsTicketItemRepository kdsTicketItemRepository;
    @MockitoBean private br.com.novaalianca.mnss.localapp.domain.sync.SyncEventRepository syncEventRepository;
    @MockitoBean private br.com.novaalianca.mnss.localapp.domain.audit.AuditLogRepository auditLogRepository;
    @MockitoBean private br.com.novaalianca.mnss.localapp.domain.customer.CustomerAddressRepository customerAddressRepository;
    @MockitoBean private br.com.novaalianca.mnss.localapp.domain.customer.CustomerRepository customerRepository;
    @MockitoBean private br.com.novaalianca.mnss.localapp.domain.catalog.CategoryRepository categoryRepository;
    @MockitoBean private br.com.novaalianca.mnss.localapp.domain.catalog.ProductRepository productRepository;
    @MockitoBean private br.com.novaalianca.mnss.localapp.domain.catalog.ProductAvailabilityRepository productAvailabilityRepository;
    @MockitoBean private org.springframework.web.client.RestTemplate restTemplate;
    @MockitoBean private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;
    @MockitoBean private br.com.novaalianca.mnss.localapp.security.auth.AuthService authService;
    @MockitoBean private br.com.novaalianca.mnss.localapp.security.user.RoleRepository roleRepository;
    @MockitoBean private br.com.novaalianca.mnss.localapp.security.user.UserRepository userRepository;
}
