package com.fitness.activityservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.queue.name}")
    private String queueName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    // Declare Queue
    @Bean
    public Queue activityQueue(){
        return new Queue(queueName, true);
    }

    // Declare Exchange
    @Bean
    public TopicExchange activityExchange() {
        return new TopicExchange(exchangeName);
    }

    // Bind Queue to Exchange using Routing Key
    @Bean
    public Binding binding(Queue activityQueue, TopicExchange activityExchange){
        return BindingBuilder
                .bind(activityQueue)
                .to(activityExchange)
                .with(routingKey);
    }

    // Convert Java <> JSON
    @Bean
    public MessageConverter jsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}
