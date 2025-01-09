package com.bookwise.backend.messaging;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue bookwiseQueue() {
        return new Queue("recommendation_request_queue", false); // false -> non-durable queue
    }

    @Bean
    public Queue responseQueue() {
        return new Queue("recommendations_response_queue", true); // durable queue
    }
}
