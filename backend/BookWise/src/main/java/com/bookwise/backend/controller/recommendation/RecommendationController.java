package com.bookwise.backend.controller.recommendation;

import com.bookwise.backend.service.RecommendationService;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

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
            SecurityContext securityContext = SecurityContextHolder.getContext(); // ✅ Preserve authentication context

            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", userId);
            payload.put("query", userRequest.get("query"));
            payload.put("genres", userRequest.get("genres"));
            payload.put("read_books", recommendationService.getReadBooks(userId));
            payload.put("requestId", recommendationService.generateRequestId(userId));

            return recommendationService.getRecommendationsForUserAsync(userId, payload)
                .thenApply(recommendationsJson -> {
                    SecurityContextHolder.setContext(securityContext); // ✅ Restore authentication
                    System.out.println("✅ Sending response to frontend: " + recommendationsJson);
                    return ResponseEntity.ok(recommendationsJson);
                });
        } catch (Exception e) {
            System.err.println("❌ Immediate error processing request: " + e.getMessage());
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to process request\"}")
            );
        }
    }
}
