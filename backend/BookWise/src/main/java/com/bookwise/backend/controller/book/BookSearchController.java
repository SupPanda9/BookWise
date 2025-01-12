package com.bookwise.backend.controller.book;

import com.bookwise.backend.model.Book;
import com.bookwise.backend.service.BookSearchService;
import com.bookwise.backend.service.BookService;
import com.bookwise.backend.service.UserPreferencesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookSearchController {

    private final BookSearchService bookSearchService;
    private final BookService bookService;
    private final UserPreferencesService preferencesService;

    public BookSearchController(BookSearchService bookSearchService, BookService bookService) {
        this.bookSearchService = bookSearchService;
        this.bookService = bookService;
        this.preferencesService = new UserPreferencesService();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(
        @RequestParam String query,
        @RequestParam(required = false) String genre,
        @RequestParam(required = false) String author,
        @RequestParam(required = false) String isbn,
        @RequestParam(defaultValue = "10") int maxResults,
        @RequestParam(defaultValue = "0") int startIndex,
        @RequestParam(defaultValue = "popularity") String sort
    ) {
        List<Book> books = bookSearchService.searchBooks(query, genre, author, isbn, maxResults, startIndex, sort);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{googleBooksId}")
    public ResponseEntity<?> openBookPage(@PathVariable String googleBooksId, @RequestParam String userId) {
        try {
            // 1. Проверка дали книгата е налична в кеша (базата данни)
            Book cachedBook = bookService.getCachedBook(googleBooksId);
            if (cachedBook != null) {
                System.out.println("Book fetched from the database: " + cachedBook.getTitle());
                return ResponseEntity.ok(cachedBook);
            }

            // 2. Ако книгата не е в базата, вземи я от Google Books API
            Book fetchedBook = bookSearchService.fetchBookById(googleBooksId);
            if (fetchedBook != null) {
                bookService.cacheBook(fetchedBook);
                preferencesService.updateGenres(userId, fetchedBook.getGenres());
                bookService.incrementPopularity(googleBooksId);
                return ResponseEntity.ok(fetchedBook);
            }

            // 3. Ако книгата не е намерена нито в базата, нито в API-то, върни грешка
            System.out.println("Book not found: " + googleBooksId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @GetMapping("/popular")
    public ResponseEntity<?> getPopularBooks(
        @RequestParam String genre,
        @RequestParam String period) {
        try {
            List<Book> books = bookService.getPopularBooksByGenre(genre, period);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
