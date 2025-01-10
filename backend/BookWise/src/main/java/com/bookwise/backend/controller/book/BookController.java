package com.bookwise.backend.controller.book;

import com.bookwise.backend.service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class BookController {

    @Autowired
    private FirebaseService firebaseService;

    @GetMapping("/books")
    public List<Map<String, Object>> getBooks() {
        try {
            return firebaseService.getAllBooks();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(Map.of("error", e.getMessage()));
        }
    }
}

