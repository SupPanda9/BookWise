package com.bookwise.backend.messaging;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RabbitMQConsumer {

    private final ConcurrentHashMap<String, String> responseStore = new ConcurrentHashMap<>();

    @RabbitListener(queues = "recommendations_response_queue")
    public void receiveRecommendations(String message) {
        try {
            System.out.println("Received recommendations from Python: " + message);

            // Parse the message to extract requestId with type safety
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> response = mapper.readValue(message, new TypeReference<>() { });

            String requestId = (String) response.get("requestId");

            // Store the recommendations in memory using the requestId
            responseStore.put(requestId, message);
        } catch (Exception e) {
            System.err.println("Failed to process recommendations: " + e.getMessage());
        }
    }

    public String getResponse(String requestId) {
        return responseStore.get(requestId);
    }

    public void removeResponse(String requestId) {
        responseStore.remove(requestId);
    }
}
