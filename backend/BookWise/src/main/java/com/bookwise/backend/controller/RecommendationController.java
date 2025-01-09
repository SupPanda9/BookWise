package com.bookwise.backend.controller;

import com.bookwise.backend.messaging.RabbitMQProducer;
import com.bookwise.backend.messaging.RabbitMQConsumer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

    private final RabbitMQProducer producer;
    private final RabbitMQConsumer consumer;

    public RecommendationController(RabbitMQProducer producer, RabbitMQConsumer consumer) {
        this.producer = producer;
        this.consumer = consumer;
    }

    @PostMapping
    public ResponseEntity<String> getRecommendations(@RequestBody Map<String, Object> payload) {
        // Send request to RabbitMQ and get requestId
        String requestId = producer.sendMessage("recommendation_request_queue", payload);

        // Wait for the response (simulate blocking or async handling)
        String recommendations = consumer.getResponse(requestId);

        // Optionally remove the stored response
        consumer.removeResponse(requestId);

        return ResponseEntity.ok(recommendations);
    }
}
