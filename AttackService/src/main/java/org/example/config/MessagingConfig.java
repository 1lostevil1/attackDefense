package org.example.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {

    public static final String ATTACK_QUEUE = "attack.queue";
    public static final String ATTACK_EXCHANGE = "attack.exchange";
    public static final String ATTACK_ROUTING_KEY = "attack.routingkey";

    @Bean
    public Queue attackQueue() { return QueueBuilder.durable(ATTACK_QUEUE).build(); }

    @Bean
    public DirectExchange attackExchange() { return new DirectExchange(ATTACK_EXCHANGE); }

    @Bean
    public Binding attackBinding(Queue attackQueue, DirectExchange attackExchange) {
        return BindingBuilder.bind(attackQueue).to(attackExchange).with(ATTACK_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        template.setReplyTimeout(5000);
        template.setExchange(ATTACK_EXCHANGE);
        template.setRoutingKey(ATTACK_ROUTING_KEY);
        return template;
    }
}
