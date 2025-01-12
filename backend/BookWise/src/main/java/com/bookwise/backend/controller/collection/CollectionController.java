package com.bookwise.backend.controller.collection;

import com.bookwise.backend.model.Book;
import com.bookwise.backend.model.Collection;
import com.bookwise.backend.service.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/collections")
public class CollectionController {

    @Autowired
    private CollectionService collectionService;

    @PostMapping
    public ResponseEntity<?> createCollection(@RequestBody Map<String, Object> requestBody) {
        try {
            String userId = (String) requestBody.get("userId");
            String name = (String) requestBody.get("name");
            boolean isPublic = (Boolean) requestBody.get("isPublic");

            String collectionId = collectionService.createCollection(userId, name, isPublic);
            Map<String, Object> newCollection = Map.of(
                "id", collectionId,
                "name", name,
                "isPublic", isPublic,
                "books", List.of() // Празен списък с книги, ако колекцията е нова
            );

            return ResponseEntity.ok(newCollection);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getCollections(@RequestParam String userId) {
        try {
            List<Collection> collections = collectionService.getCollections(userId);
            return ResponseEntity.ok(collections);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCollectionById(@PathVariable String id) {
        try {
            Collection collection = collectionService.getCollectionById(id);
            return ResponseEntity.ok(collection);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{collectionId}/books")
    public ResponseEntity<?> addBookToCollection(@PathVariable String collectionId,
                                                 @RequestBody Map<String, String> requestBody) {
        try {
            String bookId = requestBody.get("bookId"); // Extract bookId from JSON body
            collectionService.addBookToCollection(collectionId, bookId);
            return ResponseEntity.ok("Book added to collection.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/{collectionId}/books/details")
    public ResponseEntity<?> getBooksInCollectionWithDetails(@PathVariable String collectionId) {
        try {
            List<Book> books = collectionService.getBooksInCollectionWithDetails(collectionId);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching book details: " + e.getMessage());
        }
    }

    @DeleteMapping("/{collectionId}/books/{bookId}")
    public ResponseEntity<?> removeBookFromCollection(@PathVariable String collectionId, @PathVariable String bookId) {
        try {
            collectionService.removeBookFromCollection(collectionId, bookId);
            return ResponseEntity.ok("Book removed from collection.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCollection(@PathVariable String id, @RequestParam String name,
                                              @RequestParam boolean isPublic) {
        try {
            collectionService.updateCollection(id, name, isPublic);
            return ResponseEntity.ok("Collection updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCollection(@PathVariable String id) {
        try {
            collectionService.deleteCollection(id);
            return ResponseEntity.ok("Collection deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
