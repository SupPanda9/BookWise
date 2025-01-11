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

    public JsonNode searchBooks(String query, int maxResults, int startIndex) {
        String url = String.format("%s?q=%s&maxResults=%d&startIndex=%d&key=%s",
            API_URL, query, maxResults, startIndex, apiKey);
        try {
            String response = restTemplate.getForObject(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch books from Google Books API", e);
        }
    }
}
