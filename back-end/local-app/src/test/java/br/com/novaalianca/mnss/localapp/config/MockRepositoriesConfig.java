package br.com.novaalianca.mnss.localapp.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MockRepositoriesConfig {
    @Bean
    @Primary
    br.com.novaalianca.mnss.localapp.domain.payment.PaymentRepository mockPaymentRepository() {
        return Mockito.mock(br.com.novaalianca.mnss.localapp.domain.payment.PaymentRepository.class);
    }

    @Bean
    @Primary
    br.com.novaalianca.mnss.localapp.domain.order.OrderRepository mockOrderRepository() {
        return Mockito.mock(br.com.novaalianca.mnss.localapp.domain.order.OrderRepository.class);
    }

    @Bean
    @Primary
    br.com.novaalianca.mnss.localapp.domain.order.OrderItemRepository mockOrderItemRepository() {
        return Mockito.mock(br.com.novaalianca.mnss.localapp.domain.order.OrderItemRepository.class);
    }

    @Bean
    @Primary
    br.com.novaalianca.mnss.localapp.domain.cash.CashRegisterRepository mockCashRegisterRepository() {
        return Mockito.mock(br.com.novaalianca.mnss.localapp.domain.cash.CashRegisterRepository.class);
    }

    @Bean
    @Primary
    br.com.novaalianca.mnss.localapp.domain.cash.CashMovementRepository mockCashMovementRepository() {
        return Mockito.mock(br.com.novaalianca.mnss.localapp.domain.cash.CashMovementRepository.class);
    }

    @Bean
    @Primary
    br.com.novaalianca.mnss.localapp.domain.stock.StockMovementRepository mockStockMovementRepository() {
        return Mockito.mock(br.com.novaalianca.mnss.localapp.domain.stock.StockMovementRepository.class);
    }

    @Bean
    @Primary
    br.com.novaalianca.mnss.localapp.domain.stock.StockBalanceRepository mockStockBalanceRepository() {
        return Mockito.mock(br.com.novaalianca.mnss.localapp.domain.stock.StockBalanceRepository.class);
    }

    @Bean
    @Primary
    br.com.novaalianca.mnss.localapp.domain.kds.KdsTicketRepository mockKdsTicketRepository() {
        return Mockito.mock(br.com.novaalianca.mnss.localapp.domain.kds.KdsTicketRepository.class);
    }

    @Bean
    @Primary
    br.com.novaalianca.mnss.localapp.domain.kds.KdsTicketItemRepository mockKdsTicketItemRepository() {
        return Mockito.mock(br.com.novaalianca.mnss.localapp.domain.kds.KdsTicketItemRepository.class);
    }

    @Bean
    @Primary
    br.com.novaalianca.mnss.localapp.domain.sync.SyncEventRepository mockSyncEventRepository() {
        return Mockito.mock(br.com.novaalianca.mnss.localapp.domain.sync.SyncEventRepository.class);
    }

    @Bean
    @Primary
    br.com.novaalianca.mnss.localapp.domain.audit.AuditLogRepository mockAuditLogRepository() {
        return Mockito.mock(br.com.novaalianca.mnss.localapp.domain.audit.AuditLogRepository.class);
    }

    @Bean
    @Primary
    br.com.novaalianca.mnss.localapp.domain.customer.CustomerAddressRepository mockCustomerAddressRepository() {
        return Mockito.mock(br.com.novaalianca.mnss.localapp.domain.customer.CustomerAddressRepository.class);
    }

    @Bean
    @Primary
    br.com.novaalianca.mnss.localapp.domain.customer.CustomerRepository mockCustomerRepository() {
        return Mockito.mock(br.com.novaalianca.mnss.localapp.domain.customer.CustomerRepository.class);
    }

    @Bean
    @Primary
    br.com.novaalianca.mnss.localapp.domain.catalog.CategoryRepository mockCategoryRepository() {
        return Mockito.mock(br.com.novaalianca.mnss.localapp.domain.catalog.CategoryRepository.class);
    }

    @Bean
    @Primary
    br.com.novaalianca.mnss.localapp.domain.catalog.ProductRepository mockProductRepository() {
        return Mockito.mock(br.com.novaalianca.mnss.localapp.domain.catalog.ProductRepository.class);
    }

    @Bean
    @Primary
    br.com.novaalianca.mnss.localapp.domain.catalog.ProductAvailabilityRepository mockProductAvailabilityRepository() {
        return Mockito.mock(br.com.novaalianca.mnss.localapp.domain.catalog.ProductAvailabilityRepository.class);
    }

    @Bean
    @Primary
    org.springframework.web.client.RestTemplate mockRestTemplate() {
        return Mockito.mock(org.springframework.web.client.RestTemplate.class);
    }

    @Bean
    @Primary
    org.springframework.amqp.rabbit.core.RabbitTemplate mockRabbitTemplate() {
        return Mockito.mock(org.springframework.amqp.rabbit.core.RabbitTemplate.class);
    }

    @Bean
    @Primary
    br.com.novaalianca.mnss.localapp.security.auth.AuthService mockAuthService() {
        return Mockito.mock(br.com.novaalianca.mnss.localapp.security.auth.AuthService.class);
    }

    @Bean
    @Primary
    br.com.novaalianca.mnss.localapp.security.user.RoleRepository mockRoleRepository() {
        return Mockito.mock(br.com.novaalianca.mnss.localapp.security.user.RoleRepository.class);
    }

    @Bean
    @Primary
    br.com.novaalianca.mnss.localapp.security.user.UserRepository mockUserRepository() {
        return Mockito.mock(br.com.novaalianca.mnss.localapp.security.user.UserRepository.class);
    }
}
