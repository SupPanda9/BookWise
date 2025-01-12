package com.bookwise.backend.controller.book;

import com.bookwise.backend.model.Book;
import com.bookwise.backend.service.BookSearchService;
import com.bookwise.backend.service.BookService;
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

    public BookSearchController(BookSearchService bookSearchService, BookService bookService) {
        this.bookSearchService = bookSearchService;
        this.bookService = bookService;
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
    public ResponseEntity<?> openBookPage(@PathVariable String googleBooksId) {
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
                System.out.println("Book fetched from Google Books API: " + fetchedBook.getTitle());
                // Запази книгата в базата данни
                bookService.cacheBook(fetchedBook);
                System.out.println("Book cached successfully: " + fetchedBook.getTitle());
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

}
