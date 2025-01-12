package com.bookwise.backend.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange recommendationsExchange() {
        return new TopicExchange("recommendations_exchange", true, false);
    }

    @Bean
    public Queue recommendationsRequestQueue() {
        return new Queue("recommendations_request_queue", true);
    }

    @Bean
    public Queue recommendationsResponseQueue() {
        return new Queue("recommendations_response_queue", true);
    }

    @Bean
    public Binding recommendationsBinding() {
        return BindingBuilder.bind(recommendationsRequestQueue())
            .to(recommendationsExchange())
            .with("recommendations_routing_key");
    }

    @Bean
    public Binding bindQueueToExchange() {
        return BindingBuilder.bind(recommendationsRequestQueue())
            .to(recommendationsExchange())
            .with("recommendations_request_queue");
    }
}
