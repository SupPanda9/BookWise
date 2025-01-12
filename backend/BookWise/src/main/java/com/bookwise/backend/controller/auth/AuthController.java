package com.bookwise.backend.controller.auth;

import com.bookwise.backend.model.User;
import com.bookwise.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final int MIN_PASSWORD_LENGTH = 8;

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            System.out.println("Register endpoint hit!");
            System.out.println("Received user: " + user);
            // Generate a unique ID for the user
            user.setId(UUID.randomUUID().toString());

            // Validate password length
            if (user.getPassword().length() < MIN_PASSWORD_LENGTH) {
                return ResponseEntity.badRequest().body("Password must be at least 8 characters.");
            }

            // Register the user
            userService.registerUser(user);
            return ResponseEntity.ok("User registered successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String email) {
        try {
            // Get user record from Firebase Authentication
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            UserRecord userRecord = firebaseAuth.getUserByEmail(email);

            // Check if the email is verified
            if (userRecord.isEmailVerified()) {
                // Update the user in Firestore
                Firestore db = FirestoreClient.getFirestore();
                db.collection("users").whereEqualTo("email", email).get().get()
                    .getDocuments().forEach(doc -> {
                        doc.getReference().update("enabled", true); // Set enabled to true
                    });

                return ResponseEntity.ok("Email verified and user enabled!");
            } else {
                return ResponseEntity.badRequest().body("Email not verified.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");

            // Authenticate user
            String token = userService.authenticateUser(email, password);

            String userId = userService.getUserIdByEmail(email);

            // Return the JWT token if authentication is successful
            return ResponseEntity.ok(Map.of(
                "token", token,
                "userId", userId
            ));
        } catch (IllegalArgumentException e) {
            // Handle invalid credentials
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        }
    }
}
