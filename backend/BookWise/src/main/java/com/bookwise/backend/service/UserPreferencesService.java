package com.bookwise.backend.service;

import com.bookwise.backend.model.GenrePreference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class UserPreferencesService {

    private final Firestore db = FirestoreClient.getFirestore();
    private static final String USERS_COLLECTION = "users";
    private static final long EXPIRATION_MONTHS = 6; // Срок на валидност на интереса (в месеци)

    public List<GenrePreference> getPreferences(String userId) throws ExecutionException, InterruptedException {
        var doc = db.collection(USERS_COLLECTION).document(userId).get().get();
        if (!doc.exists()) {
            throw new RuntimeException("User not found");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawPreferences = (List<Map<String, Object>>) doc.get("preferences");

        if (rawPreferences == null) return new ArrayList<>();
        List<GenrePreference> preferences = new ArrayList<>();
        for (Map<String, Object> raw : rawPreferences) {
            GenrePreference preference = new GenrePreference();
            preference.setGenre((String) raw.get("genre"));
            preference.setLastActive((String) raw.get("lastActive"));
            preferences.add(preference);
        }
        return preferences;
    }

    public void updateGenres(String userId, List<String> genres) throws ExecutionException, InterruptedException {
        var userDoc = db.collection("users").document(userId).get().get();
        if (!userDoc.exists()) {
            throw new RuntimeException("User not found");
        }
        Map<String, Object> userData = userDoc.getData();
        if (userData == null) {
            throw new RuntimeException("Invalid user data");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> preferences = (Map<String, Object>) userData.get("preferences");
        if (preferences == null) {
            preferences = new HashMap<>();
        }
        List<Map<String, Object>> genrePreferences = (List<Map<String, Object>>) preferences.get("genres");
        if (genrePreferences == null) {
            genrePreferences = new ArrayList<>();
        }
        for (String genre : genres) {
            boolean exists = genrePreferences.stream().anyMatch(pref -> genre.equals(pref.get("genre")));
            if (!exists) {
                Map<String, Object> newGenre = new HashMap<>();
                newGenre.put("genre", genre);
                newGenre.put("lastActive", java.time.ZonedDateTime.now().toString());
                genrePreferences.add(newGenre);
            }
        }
        preferences.put("genres", genrePreferences);
        db.collection("users").document(userId).update("preferences", preferences).get();
    }

    public void removeExpiredPreferences(String userId, int expirationDays)
        throws ExecutionException, InterruptedException {
        var userDoc = db.collection("users").document(userId).get().get();
        if (!userDoc.exists()) {
            throw new RuntimeException("User not found");
        }
        Map<String, Object> userData = userDoc.getData();
        if (userData == null) {
            throw new RuntimeException("Invalid user data");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> preferences = (Map<String, Object>) userData.get("preferences");
        if (preferences == null) {
            return; // No preferences to clean
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> genres = (List<Map<String, Object>>) preferences.get("genres");
        if (genres == null) {
            return; // No genres to clean
        }
        var now = java.time.ZonedDateTime.now();
        var threshold = now.minusDays(expirationDays);
        genres.removeIf(genre -> {
            String lastActiveStr = (String) genre.get("lastActive");
            if (lastActiveStr == null) return true; // Remove if `lastActive` is missing
            var lastActive = java.time.ZonedDateTime.parse(lastActiveStr);
            return lastActive.isBefore(threshold);
        });
        preferences.put("genres", genres);
        db.collection("users").document(userId).update("preferences", preferences).get();
    }

}
