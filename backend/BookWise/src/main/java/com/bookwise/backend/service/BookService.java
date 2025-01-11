package com.bookwise.backend.service;

import com.bookwise.backend.model.Book;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class BookService {

    private static final int DAYS_IN_PERIOD = 30;

    private final Firestore db = FirestoreClient.getFirestore();

    public Book getBookDetails(String googleBooksId) throws ExecutionException, InterruptedException {
        Book cachedBook = getCachedBook(googleBooksId);
        if (cachedBook != null) {
            return cachedBook;
        }
        throw new RuntimeException("Book is not cached.");
    }

    public void cacheBook(Book book) throws ExecutionException, InterruptedException {
        db.collection("books").document(book.getGoogleBooksId()).set(book).get();
    }

    public void incrementPopularity(String googleBooksId) throws ExecutionException, InterruptedException {
        var doc = db.collection("books").document(googleBooksId).get().get();
        if (!doc.exists()) return;

        @SuppressWarnings("unchecked")
        Map<String, Object> popularity = (Map<String, Object>) doc.get("popularity");
        if (popularity == null) popularity = new HashMap<>();

        updatePopularity(popularity, "last7Days");
        updatePopularity(popularity, "last30Days");

        int thisYear = Optional.ofNullable((Integer) popularity.get("thisYear")).orElse(0);
        popularity.put("thisYear", thisYear + 1);

        db.collection("books").document(googleBooksId).update("popularity", popularity).get();
    }

    private void updatePopularity(Map<String, Object> popularity, String key) {
        @SuppressWarnings("unchecked")
        Map<String, Object> period = (Map<String, Object>) popularity.get(key);
        if (period == null) period = new HashMap<>();

        @SuppressWarnings("unchecked")
        List<Integer> days = (List<Integer>) period.get("days");
        if (days == null) days = new ArrayList<>(Collections.nCopies(DAYS_IN_PERIOD, 0));

        int todayIndex = LocalDate.now().getDayOfMonth() - 1;
        days.set(todayIndex, days.get(todayIndex) + 1);

        int total = days.stream().mapToInt(Integer::intValue).sum();
        period.put("days", days);
        period.put("total", total);

        popularity.put(key, period);
    }

    private Book getCachedBook(String googleBooksId) throws ExecutionException, InterruptedException {
        var doc = db.collection("books").document(googleBooksId).get().get();
        return doc.exists() ? doc.toObject(Book.class) : null;
    }
}
