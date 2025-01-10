package com.bookwise.backend.service;

import com.bookwise.backend.model.User;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class UserService {
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void registerUser(User user) throws Exception {
        // Hash the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set default values
        user.setEnabled(false); // User is disabled until email verification
        user.setRoles(List.of("USER")); // Default role
        user.setCollections(List.of()); // Initialize collections as an empty list
        user.setReadBooks(List.of()); // Initialize readBooks as an empty list

        // Process preferences with current timestamp
        User.Preferences preferences = new User.Preferences();
        List<User.Preferences.Genre> updatedGenres = user.getPreferences().getGenres().stream()
            .peek(genre -> {
                genre.setLastActive(Instant.now().toString()); // Set current timestamp
            })
            .toList();
        preferences.setGenres(updatedGenres);
        user.setPreferences(preferences);

        // Save the user in Firebase
        Firestore db = FirestoreClient.getFirestore();
        db.collection("users").document(user.getId()).set(user).get();
    }
}
