package com.bookwise.backend.service;

import com.bookwise.backend.exceptions.DuplicateEmailException;
import com.bookwise.backend.model.User;
import com.bookwise.backend.security.JwtTokenProvider;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class UserService {
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public UserService(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public void registerUser(User user) throws Exception {
        Firestore db = FirestoreClient.getFirestore();

        boolean emailExists = !db.collection("users")
            .whereEqualTo("email", user.getEmail())
            .get()
            .get()
            .isEmpty();

        if (emailExists) {
            throw new DuplicateEmailException("Email is already registered.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false); // User is disabled until email verification
        user.setRoles(List.of("USER")); // Default role
        user.setCollections(List.of()); // Initialize collections as an empty list
        user.setReadBooks(List.of()); // Initialize readBooks as an empty list

        User.Preferences preferences = new User.Preferences();
        List<User.Preferences.Genre> updatedGenres = user.getPreferences().getGenres().stream()
            .peek(genre -> genre.setLastActive(Instant.now().toString())) // Set current timestamp
            .toList();
        preferences.setGenres(updatedGenres);
        user.setPreferences(preferences);

        // Save the user in Firestore
        FirestoreClient.getFirestore().collection("users").document(user.getId()).set(user).get();
    }

    public String authenticateUser(String email, String password) throws Exception {
        Firestore db = FirestoreClient.getFirestore();

        // Fetch user by email
        var userQuery = db.collection("users").whereEqualTo("email", email).get().get();
        if (userQuery.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        var userDocument = userQuery.getDocuments().get(0);
        User user = userDocument.toObject(User.class);

        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        // Check if the user is enabled
        if (!user.isEnabled()) {
            throw new IllegalArgumentException("Please verify your email before logging in.");
        }

        // Generate JWT token
        return jwtTokenProvider.generateToken(user);
    }
}
