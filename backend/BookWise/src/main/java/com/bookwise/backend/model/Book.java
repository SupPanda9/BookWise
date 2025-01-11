package com.bookwise.backend.model;

import lombok.Data;

import java.util.List;

@Data
public class Book {
    private String googleBooksId;
    private String title;
    private List<String> authors;
    private List<String> genres;
    private String description;
    private Integer pageCount;
    private String coverImage;
    private String isbn;
}
