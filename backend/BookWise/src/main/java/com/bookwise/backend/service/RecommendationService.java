package com.bookwise.backend.service;

import com.bookwise.backend.messaging.RabbitMQConsumer;
import com.bookwise.backend.messaging.RabbitMQProducer;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class RecommendationService {

    private static final int THREAD_POOL_CORE_SIZE = 5;
    private static final int THREAD_POOL_MAX_SIZE = 10;
    private static final int THREAD_POOL_QUEUE_CAPACITY = 25;
    private static final int TIMEOUT_SECONDS = 90;
    private static final int SLEEP_INTERVAL_MS = 100;

    private final Firestore db = FirestoreClient.getFirestore();
    private final RabbitMQProducer rabbitMQProducer;
    private final RabbitMQConsumer rabbitMQConsumer;
    private final ThreadPoolTaskExecutor taskExecutor;

    public RecommendationService(RabbitMQProducer rabbitMQProducer, RabbitMQConsumer rabbitMQConsumer) {
        this.rabbitMQProducer = rabbitMQProducer;
        this.rabbitMQConsumer = rabbitMQConsumer;

        this.taskExecutor = new ThreadPoolTaskExecutor();
        this.taskExecutor.setCorePoolSize(THREAD_POOL_CORE_SIZE);
        this.taskExecutor.setMaxPoolSize(THREAD_POOL_MAX_SIZE);
        this.taskExecutor.setQueueCapacity(THREAD_POOL_QUEUE_CAPACITY);
        this.taskExecutor.setThreadNamePrefix("RecommendationThread-");
        this.taskExecutor.initialize();
    }

    public CompletableFuture<String> getRecommendationsForUserAsync(String userId, Map<String, Object> payload) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Debug: Print the payload sent to RabbitMQ
                System.out.println("Sending to RabbitMQ: " + payload);

                String requestId = rabbitMQProducer.sendMessage(userId, payload);
                System.out.println("Generated Request ID: " + requestId);

                // Wait for response
                long startTime = System.currentTimeMillis();
                String response;
                while ((response = rabbitMQConsumer.getResponse(requestId)) == null) {
                    if (System.currentTimeMillis() - startTime > TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS)) {
                        throw new RuntimeException("Timeout waiting for recommendations.");
                    }
                    Thread.sleep(SLEEP_INTERVAL_MS); // Short wait
                }

                // Debug: Print received response
//                System.out.println("Received Response: " + response);

                rabbitMQConsumer.removeResponse(requestId); // Clean up
                return response;
            } catch (Exception e) {
                System.err.println("Error in RecommendationService: " + e.getMessage());
                throw new RuntimeException("Failed to fetch recommendations: " + e.getMessage(), e);
            }
        }, taskExecutor);
    }

    public List<Map<String, String>> getReadBooks(String userId) throws Exception {
        var userDoc = db.collection("users").document(userId).get().get();
        if (!userDoc.exists()) {
            throw new RuntimeException("User not found: " + userId);
        }
        Map<String, Object> userData = userDoc.getData();
        if (userData == null || !userData.containsKey("readBooks")) {
            return List.of(); // Връщане на празен списък, ако няма прочетени книги
        }

        @SuppressWarnings("unchecked")
        List<String> readBooks = (List<String>) userData.get("readBooks");

        // Преобразуване на списъка от низове в списък от речници
        List<Map<String, String>> formattedReadBooks = new ArrayList<>();
        for (String bookId : readBooks) {
            var bookDoc = db.collection("books").document(bookId).get().get();
            if (bookDoc.exists()) {
                Map<String, Object> bookData = bookDoc.getData();
                if (bookData != null && bookData.containsKey("title")) {
                    Map<String, String> bookEntry = new HashMap<>();
                    bookEntry.put("bookId", bookId);
                    bookEntry.put("title", (String) bookData.get("title")); // Добавяне на заглавието
                    formattedReadBooks.add(bookEntry);
                }
            }
        }

        return formattedReadBooks;
    }

    public String generateRequestId(String userId) {
        return userId; // Използваме userId директно като requestId
    }
}
