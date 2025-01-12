package com.bookwise.backend.controller.user;

import com.bookwise.backend.model.GenrePreference;
import com.bookwise.backend.service.UserPreferencesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/preferences")
public class UserPreferencesController {

    private final UserPreferencesService preferencesService;

    public UserPreferencesController(UserPreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getPreferences(@PathVariable String userId) {
        try {
            List<GenrePreference> preferences = preferencesService.getPreferences(userId);
            return ResponseEntity.ok(preferences);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{userId}/update")
    public ResponseEntity<?> updateGenrePreferences(
        @PathVariable String userId,
        @RequestBody Map<String, List<String>> requestBody
    ) {
        try {
            List<String> genres = requestBody.get("genres");
            preferencesService.updateGenres(userId, genres);
            return ResponseEntity.ok("Genre preferences updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{userId}/cleanup")
    public ResponseEntity<?> removeExpiredPreferences(
        @PathVariable String userId,
        @RequestParam(defaultValue = "180") int expirationDays // Default: 6 months
    ) {
        try {
            preferencesService.removeExpiredPreferences(userId, expirationDays);
            return ResponseEntity.ok("Expired preferences removed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
