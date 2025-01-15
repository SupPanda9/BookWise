package com.bookwise.backend.controller.auth;

import com.bookwise.backend.dtos.UpdateProfileRequest;
import com.bookwise.backend.model.User;
import com.bookwise.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable String userId) {
        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found: " + e.getMessage());
        }
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<?> updateUserProfile(
        @PathVariable String userId,
        @RequestBody UpdateProfileRequest request) {
        System.out.println("Получена заявка за актуализация на профил за потребител: " + userId);
        System.out.println("Данни: " + request);

        try {
            userService.updateUserProfile(userId, request);
            return ResponseEntity.ok("Profile updated successfully!");
        } catch (Exception e) {
            System.out.println("Грешка при актуализация: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

}
