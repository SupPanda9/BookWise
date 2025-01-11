package com.bookwise.backend.service;

import com.bookwise.backend.integration.GoogleBooksAPIClient;
import com.bookwise.backend.model.Book;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookSearchService {

    private final GoogleBooksAPIClient googleBooksAPIClient;

    public BookSearchService(GoogleBooksAPIClient googleBooksAPIClient) {
        this.googleBooksAPIClient = googleBooksAPIClient;
    }

    public List<Book> searchBooks(String query, int maxResults, int startIndex) {
        JsonNode apiResponse = googleBooksAPIClient.searchBooks(query, maxResults, startIndex);
        return mapToBooks(apiResponse);
    }

    private List<Book> mapToBooks(JsonNode node) {
        List<Book> books = new ArrayList<>();
        if (node == null || !node.has("items")) {
            return books;
        }
        for (JsonNode item : node.get("items")) {
            books.add(mapToBook(item));
        }
        return books;
    }

    private Book mapToBook(JsonNode node) {
        Book book = new Book();
        book.setGoogleBooksId(node.get("id").asText());
        book.setTitle(node.path("volumeInfo").path("title").asText());
        book.setAuthors(parseArray(node.path("volumeInfo").path("authors")));
        book.setGenres(parseArray(node.path("volumeInfo").path("categories")));
        book.setDescription(node.path("volumeInfo").path("description").asText());
        book.setPageCount(node.path("volumeInfo").path("pageCount").asInt(0));
        book.setCoverImage(node.path("volumeInfo").path("imageLinks").path("thumbnail").asText());
        book.setIsbn(parseIsbn(node.path("volumeInfo").path("industryIdentifiers")));
        return book;
    }

    private List<String> parseArray(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                list.add(item.asText());
            }
        }
        return list;
    }

    private String parseIsbn(JsonNode node) {
        if (node == null || !node.isArray()) return null;
        for (JsonNode item : node) {
            if ("ISBN_13".equals(item.get("type").asText())) {
                return item.get("identifier").asText();
            }
        }
        return null;
    }
}