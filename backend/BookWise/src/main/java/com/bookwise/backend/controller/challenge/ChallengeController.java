package com.bookwise.backend.controller.challenge;

import com.bookwise.backend.model.Challenge;
import com.bookwise.backend.service.ChallengeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;

    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @GetMapping
    public ResponseEntity<?> getAllChallenges() {
        try {
            return ResponseEntity.ok(challengeService.getAllChallenges());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to fetch challenges: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<Challenge> createChallenge(@RequestBody Challenge challenge) {
        try {
            System.out.println("Received challenge: " + challenge);
            Challenge createdChallenge = challengeService.createChallenge(challenge);
            System.out.println("Saved challenge: " + createdChallenge);
            return ResponseEntity.ok(createdChallenge);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Challenge> getChallengeById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(challengeService.getChallengeById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<?> joinChallenge(@PathVariable String id, @RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.badRequest().body("Missing userId");
            }
            challengeService.joinChallenge(id, userId);
            return ResponseEntity.ok("Joined challenge successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/progress")
    public ResponseEntity<?> updateProgress(@PathVariable String id, @RequestBody Map<String, Object> request) {
        try {
            String userId = (String) request.get("userId");
            Integer progress = (Integer) request.get("progress");

            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.badRequest().body("Missing userId");
            }
            if (progress == null) {
                return ResponseEntity.badRequest().body("Missing progress");
            }

            challengeService.updateProgress(id, userId, progress);
            return ResponseEntity.ok("Progress updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChallenge(@PathVariable String id) {
        try {
            challengeService.deleteChallenge(id);
            return ResponseEntity.ok("Challenge deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
