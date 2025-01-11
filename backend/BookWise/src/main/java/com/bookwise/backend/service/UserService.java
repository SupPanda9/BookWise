package com.bookwise.backend.service;

import com.bookwise.backend.dtos.UpdateProfileRequest;
import com.bookwise.backend.exceptions.DuplicateEmailException;
import com.bookwise.backend.model.Collection;
import com.bookwise.backend.model.User;
import com.bookwise.backend.security.JwtTokenProvider;
import com.bookwise.backend.security.PasswordValidator;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

        // Check if the email already exists
        boolean emailExists = !db.collection("users").whereEqualTo("email", user.getEmail()).get().get().isEmpty();

        if (emailExists) {
            throw new DuplicateEmailException("Email is already registered.");
        }
        PasswordValidator.validatePassword(user.getPassword(), user.getUsername(), user.getEmail());

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

    public void updateUserProfile(String userId, UpdateProfileRequest request) throws Exception {
        Firestore db = FirestoreClient.getFirestore();

        // Fetch and validate user
        User user = fetchUserById(userId, db);

        // Update individual fields
        if (request.getPassword() != null) {
            updatePassword(user, request.getPassword());
        }
        if (request.getUsername() != null) {
            updateUsername(user, request.getUsername());
        }
        if (request.getEmail() != null) {
            updateEmail(userId, user, request.getEmail(), db);
        }
        if (request.getPreferences() != null) {
            updatePreferences(user, request.getPreferences());
        }
        if (request.getIsPublic() != null) {
            user.setEnabled(request.getIsPublic());
        }
        if (request.getBooksToAdd() != null || request.getBooksToRemove() != null) {
            updateReadBooks(userId, user, request.getBooksToAdd(), request.getBooksToRemove(), db);
        }

        // Save updated user
        saveUser(userId, user, db);
    }

    private User fetchUserById(String userId, Firestore db) throws Exception {
        var userDoc = db.collection("users").document(userId).get().get();
        if (!userDoc.exists()) {
            throw new Exception("User not found.");
        }

        User user = userDoc.toObject(User.class);
        if (user == null) {
            throw new Exception("User document could not be deserialized.");
        }

        return user;
    }

    private void updatePassword(User user, String password) throws Exception {
        PasswordValidator.validatePassword(password, user.getUsername(), user.getEmail());
        user.setPassword(passwordEncoder.encode(password));
    }

    private void updateUsername(User user, String username) {
        user.setUsername(username);
    }

    private void updateEmail(String userId, User user, String email, Firestore db) throws Exception {
        var emailQuery = db.collection("users").whereEqualTo("email", email).get().get();
        if (!emailQuery.isEmpty() && !emailQuery.getDocuments().get(0).getId().equals(userId)) {
            throw new Exception("Email is already in use.");
        }
        user.setEmail(email);
    }

    private void updatePreferences(User user, List<String> preferences) {
        User.Preferences newPreferences = new User.Preferences();
        newPreferences.setGenres(preferences.stream().map(genre -> {
            User.Preferences.Genre newGenre = new User.Preferences.Genre();
            newGenre.setGenre(genre);
            newGenre.setLastActive(Instant.now().toString());
            return newGenre;
        }).toList());
        user.setPreferences(newPreferences);
    }

    private void updateReadBooks(String userId, User user, List<String> booksToAdd, List<String> booksToRemove,
                                 Firestore db) throws Exception {
        List<String> currentReadBooks = user.getReadBooks();

        // Add books
        if (booksToAdd != null) {
            booksToAdd.forEach(bookId -> {
                if (!currentReadBooks.contains(bookId)) {
                    currentReadBooks.add(bookId);
                }
            });
        }

        // Remove books
        if (booksToRemove != null) {
            currentReadBooks.removeAll(booksToRemove);
        }

        user.setReadBooks(currentReadBooks);

        // Synchronize with the "Read" collection
        Collection readCollection = getOrCreateReadCollection(userId, db);
        synchronizeReadCollection(readCollection, booksToAdd, booksToRemove, db);
    }

    private Collection getOrCreateReadCollection(String userId, Firestore db) throws Exception {
        var querySnapshot = db.collection("collections")
            .whereEqualTo("userId", userId)
            .whereEqualTo("name", "Read")
            .get()
            .get();

        if (querySnapshot.isEmpty()) {
            Collection readCollection = new Collection();
            readCollection.setId(UUID.randomUUID().toString());
            readCollection.setUserId(userId);
            readCollection.setName("Read");
            readCollection.setPublic(false);
            readCollection.setBooks(new ArrayList<>());
            db.collection("collections").document(readCollection.getId()).set(readCollection).get();
            return readCollection;
        } else {
            return querySnapshot.getDocuments().get(0).toObject(Collection.class);
        }
    }

    private void synchronizeReadCollection(Collection readCollection, List<String> booksToAdd,
                                           List<String> booksToRemove, Firestore db) throws Exception {
        // Add books to the collection
        if (booksToAdd != null) {
            booksToAdd.forEach(bookId -> {
                if (!readCollection.getBooks().contains(bookId)) {
                    readCollection.getBooks().add(bookId);
                }
            });
        }

        // Remove books from the collection
        if (booksToRemove != null) {
            readCollection.getBooks().removeAll(booksToRemove);
        }

        // Save the updated collection
        db.collection("collections").document(readCollection.getId()).set(readCollection).get();
    }

    private void saveUser(String userId, User user, Firestore db) throws Exception {
        db.collection("users").document(userId).set(user).get();
    }
}
