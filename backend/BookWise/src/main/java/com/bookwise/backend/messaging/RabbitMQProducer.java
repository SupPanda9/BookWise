package com.bookwise.backend.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public String sendMessage(String queueName, Map<String, Object> payload) {
        try {
            // Generate a unique requestId
            String requestId = UUID.randomUUID().toString();
            payload.put("requestId", requestId);

            System.out.println("Publishing to RabbitMQ [Exchange: recommendations_exchange]: " + payload);
            // Convert payload to JSON and send
            String message = objectMapper.writeValueAsString(payload);
            rabbitTemplate.convertAndSend("recommendations_exchange", "recommendations_request_queue", message);
            System.out.println("Message sent to queue '" + queueName + "': " + message);

            return requestId; // Return the generated requestId
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}