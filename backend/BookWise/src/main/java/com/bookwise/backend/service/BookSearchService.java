package com.bookwise.backend.service;

import com.bookwise.backend.integration.GoogleBooksAPIClient;
import com.bookwise.backend.model.Book;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class BookSearchService {

    private final GoogleBooksAPIClient googleBooksAPIClient;

    public BookSearchService(GoogleBooksAPIClient googleBooksAPIClient) {
        this.googleBooksAPIClient = googleBooksAPIClient;
    }

    public List<Book> searchBooks(String query, String genre, String author, String isbn, int maxResults,
                                  int startIndex, String sort) {
        JsonNode apiResponse = googleBooksAPIClient.searchBooks(query, genre, author, isbn, maxResults, startIndex);
        List<Book> books = mapToBooks(apiResponse);

        // Сортиране
        if ("popularity".equalsIgnoreCase(sort)) {
            books.sort(Comparator.comparingInt(
                (Book book) -> {
                    Book.Popularity popularity = book.getPopularity() != null
                        ? book.getPopularity().get("last30Days")
                        : null;
                    return popularity != null ? popularity.getTotal() : 0;
                }
            ).reversed());
        } else if ("alphabetical".equalsIgnoreCase(sort)) {
            books.sort(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER));
        }

        return books;
    }

    public Book fetchBookById(String googleBooksId) {
        JsonNode apiResponse = googleBooksAPIClient.getBookById(googleBooksId);
        if (apiResponse != null && apiResponse.has("id")) {
            return mapToBook(apiResponse);
        }
        return null;
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

        // Четем полето genres, а не categories
        List<String> genres = parseArray(node.path("volumeInfo").path("categories"));
        if (genres == null || genres.isEmpty()) {
            genres = List.of("Unknown Genre"); // Ако няма жанрове, задаваме "Unknown Genre"
        } else {
            genres = cleanGenres(genres, book.getTitle()); // Почистваме жанровете
        }
        book.setGenres(genres);

        book.setDescription(node.path("volumeInfo").path("description").asText());
        book.setPageCount(node.path("volumeInfo").path("pageCount").asInt(0));
        book.setCoverImage(node.path("volumeInfo").path("imageLinks").path("thumbnail").asText());
        book.setIsbn(parseIsbn(node.path("volumeInfo").path("industryIdentifiers")));
        return book;
    }

    private List<String> cleanGenres(List<String> genres, String bookTitle) {
        List<String> cleanedGenres = new ArrayList<>();
        for (String genre : genres) {
            // Проверяваме дали жанрът съдържа името на книгата
            if (!genre.toLowerCase().contains(bookTitle.toLowerCase())) {
                cleanedGenres.add(genre);
            }
        }
        // Ако след почистване няма жанрове, задаваме "Unknown Genre"
        if (cleanedGenres.isEmpty()) {
            cleanedGenres.add("Unknown Genre");
        }
        return cleanedGenres;
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
