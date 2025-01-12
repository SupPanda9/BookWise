package com.bookwise.backend.model;

import lombok.Data;

@Data
public class GenrePreference {
    private String genre; // Жанр
    private String lastActive; // Последна активност във формат ISO8601
}
