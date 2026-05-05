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
    public SyncEventService syncEventService(SyncEventRepository repository) {
        return new SyncEventService(repository);
    }

    @Bean
    @ConditionalOnBean(SyncEventRepository.class)
    public SyncOutboxWorker syncOutboxWorker(SyncEventRepository repository, RestTemplate restTemplate, ObjectMapper objectMapper) {
        return new SyncOutboxWorker(repository, restTemplate, objectMapper);
    }
}
