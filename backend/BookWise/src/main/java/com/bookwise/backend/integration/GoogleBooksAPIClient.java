package com.bookwise.backend.integration;

import org.springframework.web.client.RestTemplate;

public class GoogleBooksAPIClient {
    private static final String API_URL = "https://www.googleapis.com/books/v1/volumes";
    private final String apiKey;

    public GoogleBooksAPIClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public String searchBooks(String query) {
        String url = API_URL + "?q=" + query + "&key=" + apiKey;
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, String.class);
    }
}
