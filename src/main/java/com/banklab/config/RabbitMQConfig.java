package com.banklab.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_TRANSACTION_FETCH="transaction.fetch";
    public static final String QUEUE_TRANSACTION_SAVE="transaction.save";
    public static final String QUEUE_CATEGORIZE="transaction.categorize";
    public static final String QUEUE_GEMINI="transaction.categorize.gemini";
    public static final String QUEUE_CATEGORY_SAVE="transaction.category.save";
    public static final String QUEUE_SUMMARY="transaction.summary";

    public static final String EXCHANGE_NAME="account.sync.exchange";
    public static final String ROUTING_KEY="account.sync.key";


    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory("localhost");
        factory.setUsername("guest");
        factory.setPassword("guest");
        return factory;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(connectionFactory());
    }

    @Bean
    public Queue transactionFetchQueue() {
        return new Queue(QUEUE_TRANSACTION_FETCH, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(transactionFetchQueue()).to(exchange).with(ROUTING_KEY);
    }
}
