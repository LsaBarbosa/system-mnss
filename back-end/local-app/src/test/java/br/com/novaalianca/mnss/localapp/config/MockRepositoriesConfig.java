package br.com.novaalianca.mnss.localapp.config;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MockRepositoriesConfig {
    @MockBean private br.com.novaalianca.mnss.localapp.domain.payment.PaymentRepository paymentRepository;
    @MockBean private br.com.novaalianca.mnss.localapp.domain.order.OrderRepository orderRepository;
    @MockBean private br.com.novaalianca.mnss.localapp.domain.order.OrderItemRepository orderItemRepository;
    @MockBean private br.com.novaalianca.mnss.localapp.domain.cash.CashRegisterRepository cashRegisterRepository;
    @MockBean private br.com.novaalianca.mnss.localapp.domain.cash.CashMovementRepository cashMovementRepository;
    @MockBean private br.com.novaalianca.mnss.localapp.domain.stock.StockMovementRepository stockMovementRepository;
    @MockBean private br.com.novaalianca.mnss.localapp.domain.kds.KdsTicketRepository kdsTicketRepository;
    @MockBean private br.com.novaalianca.mnss.localapp.domain.kds.KdsTicketItemRepository kdsTicketItemRepository;
    @MockBean private br.com.novaalianca.mnss.localapp.domain.sync.SyncEventRepository syncEventRepository;
    @MockBean private br.com.novaalianca.mnss.localapp.domain.audit.AuditLogRepository auditLogRepository;
    @MockBean private br.com.novaalianca.mnss.localapp.domain.customer.CustomerAddressRepository customerAddressRepository;
    @MockBean private br.com.novaalianca.mnss.localapp.domain.customer.CustomerRepository customerRepository;
    @MockBean private br.com.novaalianca.mnss.localapp.domain.catalog.CategoryRepository categoryRepository;
    @MockBean private br.com.novaalianca.mnss.localapp.domain.catalog.ProductRepository productRepository;
    @MockBean private br.com.novaalianca.mnss.localapp.domain.catalog.ProductAvailabilityRepository productAvailabilityRepository;
    @MockBean private org.springframework.web.client.RestTemplate restTemplate;
    @MockBean private br.com.novaalianca.mnss.localapp.security.auth.AuthService authService;
    @MockBean private br.com.novaalianca.mnss.localapp.security.user.RoleRepository roleRepository;
    @MockBean private br.com.novaalianca.mnss.localapp.security.user.UserRepository userRepository;
}
