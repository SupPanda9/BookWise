package com.bookwise.backend.controller.book;

import com.bookwise.backend.model.Book;
import com.bookwise.backend.model.Review;
import com.bookwise.backend.service.BookService;
import com.bookwise.backend.service.ReviewService;
import com.bookwise.backend.service.UserPreferencesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserPreferencesService preferencesService;
    private final BookService bookService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
        this.preferencesService = new UserPreferencesService();
        this.bookService = new BookService();
    }

    @PostMapping("/{bookId}")
    public ResponseEntity<?> addReview(@PathVariable String bookId, @RequestBody Review review) {
        try {
            reviewService.addReview(bookId, review);

            Book book = bookService.getCachedBook(bookId);
            if (book != null && book.getGenres() != null && !book.getGenres().isEmpty()) {
                preferencesService.updateGenres(review.getUserId(), book.getGenres());
            } else {
                System.out.println("Genres for book not found or empty.");
            }

            return ResponseEntity.ok("Review added successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<?> getReviews(@PathVariable String bookId) {
        try {
            return ResponseEntity.ok(reviewService.getReviews(bookId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{bookId}/{reviewId}")
    public ResponseEntity<?> editReview(
        @PathVariable String bookId,
        @PathVariable String reviewId,
        @RequestBody Review updatedReview) {
        try {
            reviewService.editReview(bookId, reviewId, updatedReview);
            return ResponseEntity.ok("Review updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{bookId}/{reviewId}")
    public ResponseEntity<?> deleteReview(
        @PathVariable String bookId,
        @PathVariable String reviewId) {
        try {
            reviewService.deleteReview(bookId, reviewId);
            return ResponseEntity.ok("Review deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
