package com.bookwise.backend.service;

import com.bookwise.backend.model.Review;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final Firestore db = FirestoreClient.getFirestore();
    private static final String REVIEW_COLLECTION = "reviews";

    @Autowired
    private BookService bookService; // Добавяме BookService

    public void addReview(String bookId, Review review) throws ExecutionException, InterruptedException {
        var doc = db.collection(REVIEW_COLLECTION).document(bookId).get().get();

        @SuppressWarnings("unchecked")
        Map<String, Object> reviews = doc.exists()
            ? (Map<String, Object>) doc.get("reviews")
            : new HashMap<>();

        // Генериране на уникален ID за ревюто
        String reviewId = UUID.randomUUID().toString();
        review.setId(reviewId);

        // Проверете дали текстът на ревюто е зададен
        if (review.getText() == null || review.getText().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }

        System.out.println(review);

        // Добавяне на ревюто
        reviews.put(reviewId, Map.of(
            "id", review.getId(),
            "userId", review.getUserId(),
            "rating", review.getRating(),
            "text", review.getText(), // Добавяме текста
            "timestamp", Instant.now().toString() // Задаваме текущо време
        ));

        // Актуализация на документа
        db.collection(REVIEW_COLLECTION).document(bookId).set(Map.of("reviews", reviews)).get();
        System.out.println("Review added with ID: " + reviewId);
    }

    public Map<String, Object> getReviews(String bookId) throws ExecutionException, InterruptedException {
        var doc = db.collection(REVIEW_COLLECTION).document(bookId).get().get();

        if (!doc.exists()) {
            System.out.println("Документът за ревюта не съществува за книга с ID: " + bookId);
            return Map.of(); // Връщаме празна карта
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> reviews = (Map<String, Object>) doc.get("reviews");
        return reviews != null ? reviews : Map.of();
    }

    public void editReview(String bookId, String reviewId, Review updatedReview)
        throws ExecutionException, InterruptedException {
        var doc = db.collection(REVIEW_COLLECTION).document(bookId).get().get();

        if (!doc.exists()) {
            throw new RuntimeException("No reviews found for the book");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> reviews = (Map<String, Object>) doc.get("reviews");
        if (reviews == null || !reviews.containsKey(reviewId)) {
            throw new RuntimeException("Review not found: " + reviewId);
        }

        updatedReview.setId(reviewId);
        updatedReview.setTimestamp(Instant.now().toString());
        reviews.put(reviewId, updatedReview);

        db.collection(REVIEW_COLLECTION).document(bookId).update("reviews", reviews).get();
        System.out.println("Review updated for book: " + bookId + " with reviewId: " + reviewId);
    }

    public void deleteReview(String bookId, String reviewId) throws ExecutionException, InterruptedException {
        var doc = db.collection(REVIEW_COLLECTION).document(bookId).get().get();

        if (!doc.exists()) {
            throw new RuntimeException("No reviews found for the book");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> reviews = (Map<String, Object>) doc.get("reviews");
        if (reviews == null || !reviews.containsKey(reviewId)) {
            throw new RuntimeException("Review not found: " + reviewId);
        }

        reviews.remove(reviewId);

        db.collection(REVIEW_COLLECTION).document(bookId).update("reviews", reviews).get();
        System.out.println("Review deleted for book: " + bookId + " with reviewId: " + reviewId);
    }
}
