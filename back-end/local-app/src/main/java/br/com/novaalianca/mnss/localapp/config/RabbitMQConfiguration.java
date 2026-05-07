package br.com.novaalianca.mnss.localapp.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfiguration {

    // ── Exchange principal ────────────────────────────────────────────────────
    public static final String LOCAL_EXCHANGE   = "mnss.local.events";
    public static final String DLX_EXCHANGE     = "mnss.local.dlx";

    // ── Filas ─────────────────────────────────────────────────────────────────
    public static final String SYNC_OUTBOX_QUEUE = "mnss.sync.outbox";
    public static final String KDS_EVENTS_QUEUE  = "mnss.kds.events";
    public static final String PRINT_JOBS_QUEUE  = "mnss.print.jobs";
    public static final String DLQ_QUEUE         = "mnss.dead.letter";

    // ── Routing keys ─────────────────────────────────────────────────────────
    public static final String RK_SYNC_OUTBOX = "sync.outbox.#";
    public static final String RK_KDS_EVENTS  = "kds.#";
    public static final String RK_PRINT_JOBS  = "print.#";

    @Bean
    public TopicExchange localEventsExchange() {
        return ExchangeBuilder.topicExchange(LOCAL_EXCHANGE).durable(true).build();
    }

    @Bean
    public FanoutExchange deadLetterExchange() {
        return ExchangeBuilder.fanoutExchange(DLX_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue syncOutboxQueue() {
        return QueueBuilder.durable(SYNC_OUTBOX_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-message-ttl", 300_000)  // 5 min TTL
                .build();
    }

    @Bean
    public Queue kdsEventsQueue() {
        return QueueBuilder.durable(KDS_EVENTS_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .build();
    }

    @Bean
    public Queue printJobsQueue() {
        return QueueBuilder.durable(PRINT_JOBS_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_QUEUE).build();
    }

    @Bean
    public Binding syncOutboxBinding() {
        return BindingBuilder.bind(syncOutboxQueue()).to(localEventsExchange()).with(RK_SYNC_OUTBOX);
    }

    @Bean
    public Binding kdsEventsBinding() {
        return BindingBuilder.bind(kdsEventsQueue()).to(localEventsExchange()).with(RK_KDS_EVENTS);
    }

    @Bean
    public Binding printJobsBinding() {
        return BindingBuilder.bind(printJobsQueue()).to(localEventsExchange()).with(RK_PRINT_JOBS);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange());
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        factory.setDefaultRequeueRejected(false);
        factory.setPrefetchCount(10);
        return factory;
    }
}
