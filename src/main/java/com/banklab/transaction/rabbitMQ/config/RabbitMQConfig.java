package com.banklab.transaction.rabbitMQ.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory("localhost");
        factory.setUsername("guest");
        factory.setPassword("guest");
        return factory;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO); // 자동 ack
        return factory;
    }


    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    public Queue queueFetch() {
        return new Queue(RabbitMQConstant.QUEUE_TRANSACTION_FETCH, true);
    }

    @Bean
    public Queue queueSave() {
        return new Queue(RabbitMQConstant.QUEUE_TRANSACTION_SAVE, true);
    }

    @Bean
    public Queue queueCategorizeInternal() {
        return new Queue(RabbitMQConstant.QUEUE_CATEGORIZE_INTERNAL, true);
    }

    @Bean
    public Queue queueCategorizeExternal() {
        return new Queue(RabbitMQConstant.QUEUE_CATEGORIZE_EXTERNAL, true);
    }

    @Bean
    public Queue queueCategorySave() {
        return new Queue(RabbitMQConstant.QUEUE_CATEGORY_SAVE, true);
    }

    @Bean
    public Queue queueSummarySave() {
        return new Queue(RabbitMQConstant.QUEUE_SUMMARY_SAVE, true);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(RabbitMQConstant.EXCHANGE);
    }

    @Bean
    public Binding bindingFetch() {
        return BindingBuilder.bind(queueFetch())
                .to(exchange())
                .with(RabbitMQConstant.ROUTING_TRANSACTION_FETCH);
    }

    @Bean
    public Binding bindingSave() {
        return BindingBuilder.bind(queueSave())
                .to(exchange())
                .with(RabbitMQConstant.ROUTING_TRANSACTION_SAVE);
    }

    @Bean
    public Binding bindingCategorizeInternal() {
        return BindingBuilder.bind(queueCategorizeInternal())
                .to(exchange())
                .with(RabbitMQConstant.ROUTING_CATEGORIZE_INTERNAL);
    }

    @Bean
    public Binding bindingCategorizeExternal() {
        return BindingBuilder.bind(queueCategorizeExternal())
                .to(exchange())
                .with(RabbitMQConstant.ROUTING_CATEGORIZE_EXTERNAL);
    }

    @Bean
    public Binding bindingCategorySave() {
        return BindingBuilder.bind(queueCategorySave())
                .to(exchange())
                .with(RabbitMQConstant.ROUTING_CATEGORY_SAVE);
    }

    @Bean
    public Binding bindingSummarySave() {
        return BindingBuilder.bind(queueSummarySave())
                .to(exchange())
                .with(RabbitMQConstant.ROUTING_SUMMARY_SAVE);
    }
}
