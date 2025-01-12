package com.bookwise.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GoogleBooksAPIClient {

    private static final String API_URL = "https://www.googleapis.com/books/v1/volumes";

    private final RestTemplate restTemplate;
    private final String apiKey;

    public GoogleBooksAPIClient(RestTemplate restTemplate, @Value("${google.books.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    public JsonNode searchBooks(String query, String genre, String author, String isbn, int maxResults,
                                int startIndex) {
        StringBuilder urlBuilder = new StringBuilder(API_URL + "?q=");
        if (query != null && !query.isEmpty()) {
            urlBuilder.append(query).append("+");
        }
        if (genre != null && !genre.isEmpty()) {
            urlBuilder.append("subject:").append("\"").append(genre).append("\"").append("+");
        }
        if (author != null && !author.isEmpty()) {
            urlBuilder.append("inauthor:").append("\"").append(author).append("\"").append("+");
        }
        if (isbn != null && !isbn.isEmpty()) {
            urlBuilder.append("isbn:").append(isbn).append("+");
        }
        if (urlBuilder.charAt(urlBuilder.length() - 1) == '+') {
            urlBuilder.setLength(urlBuilder.length() - 1);
        }
        urlBuilder.append("&maxResults=").append(maxResults)
            .append("&startIndex=").append(startIndex)
            .append("&langRestrict=en")
            .append("&key=").append(apiKey);

        String url = urlBuilder.toString();
        try {
            String response = restTemplate.getForObject(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch books from Google Books API", e);
        }
    }

    public JsonNode getBookById(String googleBooksId) {
        String url = API_URL + "/" + googleBooksId + "?key=" + apiKey;
        try {
            String response = restTemplate.getForObject(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch book by ID: " + googleBooksId, e);
        }
    }

}
