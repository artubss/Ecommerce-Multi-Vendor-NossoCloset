package com.zosh.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do RabbitMQ para processamento assíncrono
 * Fase 1 - Semana 1: Setup de Infraestrutura
 */
@Configuration
public class RabbitMQConfig {

    @Value("${spring.rabbitmq.username:admin}")
    private String username;

    // Exchanges
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String IMAGE_PROCESSING_EXCHANGE = "image.processing.exchange";

    // Queues
    public static final String CUSTOM_ORDER_QUEUE = "custom.order.queue";
    public static final String COLLECTIVE_ORDER_QUEUE = "collective.order.queue";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String IMAGE_PROCESSING_QUEUE = "image.processing.queue";

    // Routing Keys
    public static final String CUSTOM_ORDER_ROUTING_KEY = "order.custom";
    public static final String COLLECTIVE_ORDER_ROUTING_KEY = "order.collective";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.*";
    public static final String IMAGE_PROCESSING_ROUTING_KEY = "image.processing.*";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    // ===== EXCHANGES =====

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public DirectExchange imageProcessingExchange() {
        return new DirectExchange(IMAGE_PROCESSING_EXCHANGE);
    }

    // ===== QUEUES =====

    @Bean
    public Queue customOrderQueue() {
        return QueueBuilder.durable(CUSTOM_ORDER_QUEUE)
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "dlx.custom.order")
                .build();
    }

    @Bean
    public Queue collectiveOrderQueue() {
        return QueueBuilder.durable(COLLECTIVE_ORDER_QUEUE)
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "dlx.collective.order")
                .build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-message-ttl", 300000) // 5 minutos
                .build();
    }

    @Bean
    public Queue imageProcessingQueue() {
        return QueueBuilder.durable(IMAGE_PROCESSING_QUEUE)
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "dlx.image.processing")
                .build();
    }

    // ===== BINDINGS =====

    @Bean
    public Binding customOrderBinding() {
        return BindingBuilder.bind(customOrderQueue())
                .to(orderExchange())
                .with(CUSTOM_ORDER_ROUTING_KEY);
    }

    @Bean
    public Binding collectiveOrderBinding() {
        return BindingBuilder.bind(collectiveOrderQueue())
                .to(orderExchange())
                .with(COLLECTIVE_ORDER_ROUTING_KEY);
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public Binding imageProcessingBinding() {
        return BindingBuilder.bind(imageProcessingQueue())
                .to(imageProcessingExchange())
                .with(IMAGE_PROCESSING_ROUTING_KEY);
    }

    // ===== DEAD LETTER QUEUE =====

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("dlx.exchange");
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("dlx.queue").build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("dlx.*");
    }
}
