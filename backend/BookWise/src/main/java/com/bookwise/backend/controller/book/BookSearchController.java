package com.bookwise.backend.controller.book;

import com.bookwise.backend.service.BookSearchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/externalBooks")
public class BookSearchController {

    private final BookSearchService bookSearchService;

    public BookSearchController(BookSearchService bookSearchService) {
        this.bookSearchService = bookSearchService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchBooks(
        @RequestParam String query,
        @RequestParam(defaultValue = "10") int maxResults,
        @RequestParam(defaultValue = "0") int startIndex
    ) {
        try {
            return ResponseEntity.ok(bookSearchService.searchBooks(query, maxResults, startIndex));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
