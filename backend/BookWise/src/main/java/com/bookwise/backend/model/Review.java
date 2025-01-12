package com.bookwise.backend.model;

import lombok.Data;

@Data
public class Review {
    private String id;
    private boolean isPublic; // Дали ревюто е публично
    private int rating; // Оценка на книгата (напр. от 1 до 5)
    private String text; // Текст на ревюто
    private String timestamp; // Дата и час на ревюто
    private String userId; // ID на потребителя, който е написал ревюто
}
