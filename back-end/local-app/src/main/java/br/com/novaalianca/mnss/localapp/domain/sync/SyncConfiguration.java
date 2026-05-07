package br.com.novaalianca.mnss.localapp.domain.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SyncConfiguration {

    @Bean
    @ConditionalOnBean(SyncEventRepository.class)
    public SyncEventService syncEventService(SyncEventRepository repository,
                                             SyncEventRabbitPublisher rabbitPublisher) {
        return new SyncEventService(repository, rabbitPublisher);
    }

    @Bean
    @ConditionalOnBean(SyncEventRepository.class)
    public SyncOutboxWorker syncOutboxWorker(SyncEventRepository repository, RestTemplate restTemplate, ObjectMapper objectMapper) {
        return new SyncOutboxWorker(repository, restTemplate, objectMapper);
    }

    @Bean
    public SyncInboxService syncInboxService(
            br.com.novaalianca.mnss.localapp.domain.order.OrderRepository orderRepository,
            br.com.novaalianca.mnss.localapp.domain.order.OrderItemRepository orderItemRepository,
            br.com.novaalianca.mnss.localapp.domain.customer.CustomerRepository customerRepository,
            br.com.novaalianca.mnss.localapp.domain.catalog.ProductRepository productRepository,
            br.com.novaalianca.mnss.localapp.domain.kds.KdsService kdsService,
            SyncEventRepository syncEventRepository,
            ObjectMapper objectMapper) {
        return new SyncInboxService(orderRepository, orderItemRepository, customerRepository, productRepository, kdsService, syncEventRepository, objectMapper);
    }

    @Bean
    public SyncPullWorker syncPullWorker(SyncInboxService inboxService, RestTemplate restTemplate) {
        return new SyncPullWorker(inboxService, restTemplate);
    }
}
