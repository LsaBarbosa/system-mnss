package br.com.novaalianca.mnss.onlineapp.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitMQConfiguration {

    public static final String LOCAL_EXCHANGE   = "mnss.local.events";
    public static final String ONLINE_EXCHANGE  = "mnss.online.events";
    public static final String DLX_EXCHANGE     = "mnss.online.dlx";

    public static final String SYNC_INBOX_QUEUE = "mnss.sync.inbox";
    public static final String ORDER_NOTIFY_QUEUE = "mnss.order.notifications";
    public static final String DEAD_LETTER_QUEUE  = "mnss.dead.letter";

    @Bean
    public TopicExchange localEventsExchange() {
        return ExchangeBuilder.topicExchange(LOCAL_EXCHANGE).durable(true).build();
    }

    @Bean
    public TopicExchange onlineEventsExchange() {
        return ExchangeBuilder.topicExchange(ONLINE_EXCHANGE).durable(true).build();
    }

    @Bean
    public FanoutExchange deadLetterExchange() {
        return ExchangeBuilder.fanoutExchange(DLX_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue syncInboxQueue() {
        return QueueBuilder.durable(SYNC_INBOX_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-message-ttl", 300_000)
                .build();
    }

    @Bean
    public Queue orderNotifyQueue() {
        return QueueBuilder.durable(ORDER_NOTIFY_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding syncInboxBinding(Queue syncInboxQueue, TopicExchange localEventsExchange) {
        return BindingBuilder.bind(syncInboxQueue).to(localEventsExchange).with("sync.#");
    }

    @Bean
    public Binding orderNotifyBinding(Queue orderNotifyQueue, TopicExchange onlineEventsExchange) {
        return BindingBuilder.bind(orderNotifyQueue).to(onlineEventsExchange).with("order.#");
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, FanoutExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setPrefetchCount(5);
        return factory;
    }
}
