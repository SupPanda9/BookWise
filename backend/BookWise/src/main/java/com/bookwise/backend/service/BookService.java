package com.bookwise.backend.service;

import com.bookwise.backend.model.Book;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class BookService {

    private final Firestore db = FirestoreClient.getFirestore();
    private static final String BOOK_COLLECTION = "books";
    private static final int WEEK = 7;
    private static final int MONTH = 30;

    public void incrementPopularity(String googleBooksId) throws ExecutionException, InterruptedException {
        var doc = db.collection(BOOK_COLLECTION).document(googleBooksId).get().get();
        if (!doc.exists()) return;

        Book book = doc.toObject(Book.class);
        if (book == null) return;

        book.incrementPopularity();
        db.collection(BOOK_COLLECTION).document(googleBooksId).set(book).get();
    }

    public Book getCachedBook(String googleBooksId) throws ExecutionException, InterruptedException {
        var doc = db.collection(BOOK_COLLECTION).document(googleBooksId).get().get();
        return doc.exists() ? doc.toObject(Book.class) : null;
    }

    public void cacheBook(Book book) throws ExecutionException, InterruptedException {
        if (book == null || book.getGoogleBooksId() == null) {
            throw new IllegalArgumentException("Book or Google Books ID cannot be null");
        }

        // Инициализиране на популярността
        initializePopularity(book);

        // Увери се, че жанровете са правилно настроени
        if (book.getGenres() == null || book.getGenres().isEmpty()) {
            book.setGenres(new ArrayList<>()); // Ако няма жанрове, създай празен списък
        }

        db.collection(BOOK_COLLECTION).document(book.getGoogleBooksId()).set(book).get();
        System.out.println("Book saved to Firestore: " + book.getTitle());
    }

    private void initializePopularity(Book book) {
        if (book.getPopularity() == null) {
            Map<String, Book.Popularity> popularity = new HashMap<>();
            popularity.put("last7Days", new Book.Popularity(WEEK));
            popularity.put("last30Days", new Book.Popularity(MONTH));

            Book.Popularity yearPopularity = new Book.Popularity(0); // Само total
            yearPopularity.setDays(null);
            yearPopularity.setTotal(0);
            popularity.put("thisYear", yearPopularity);

            book.setPopularity(popularity);
        }
    }

    public List<Book> getPopularBooksByGenre(String genre, String period)
        throws ExecutionException, InterruptedException {
        var booksCollection = db.collection("books").get().get();

        List<Book> filteredBooks = new ArrayList<>();
        for (var doc : booksCollection.getDocuments()) {
            Book book = doc.toObject(Book.class);

            if (book == null || book.getGenres() == null || !book.getGenres().contains(genre)) {
                continue;
            }

            Book.Popularity popularity = book.getPopularity() != null ? book.getPopularity().get(period) : null;
            if (popularity != null && popularity.getTotal() > 0) {
                filteredBooks.add(book);
            }
        }

        // Сортиране по популярност
        filteredBooks.sort(Comparator.comparingInt(
            (Book book) -> {
                Book.Popularity popularity = book.getPopularity().get(period);
                return popularity != null ? popularity.getTotal() : 0;
            }
        ).reversed());

        return filteredBooks;
    }
}
