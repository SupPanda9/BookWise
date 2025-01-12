package com.bookwise.backend.controller.recommendation;

import com.bookwise.backend.service.RecommendationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping("/{userId}")
    public CompletableFuture<ResponseEntity<String>> getRecommendations(@PathVariable String userId,
                                                                        @RequestBody Map<String, Object> userRequest) {
        try {
            // Prepare payload for RabbitMQ
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", userId);
            payload.put("query", userRequest.get("query"));
            payload.put("genres", userRequest.get("genres"));
            payload.put("read_books", recommendationService.getReadBooks(userId));
            payload.put("requestId", recommendationService.generateRequestId(userId));

            return recommendationService.getRecommendationsForUserAsync(userId, payload)
                .thenApply(recommendationsJson -> ResponseEntity.ok(recommendationsJson))
                .exceptionally(e -> {
                    // Convert the error response to a JSON string
                    Map<String, String> errorResponse = Map.of(
                        "error", "Error fetching recommendations",
                        "details", e.getMessage()
                    );
                    String errorJson;
                    try {
                        errorJson = new ObjectMapper().writeValueAsString(errorResponse);
                    } catch (Exception ex) {
                        errorJson = "{\"error\":\"Failed to serialize error message\"}";
                    }
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorJson);
                });
        } catch (Exception e) {
            // Convert the immediate error to a JSON string
            Map<String, String> errorResponse = Map.of(
                "error", "Error processing request",
                "details", e.getMessage()
            );
            String errorJson;
            try {
                errorJson = new ObjectMapper().writeValueAsString(errorResponse);
            } catch (Exception ex) {
                errorJson = "{\"error\":\"Failed to serialize error message\"}";
            }
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorJson)
            );
        }
    }
}
