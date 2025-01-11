package com.bookwise.backend.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Collection {
    private String id; // Unique collection ID
    private String userId; // ID of the user who owns the collection
    private String name; // Name of the collection (e.g., "Favorites")
    private boolean isPublic; // Visibility of the collection
    private List<String> books = new ArrayList<>(); // List of book IDs (googleBooksId)
}
