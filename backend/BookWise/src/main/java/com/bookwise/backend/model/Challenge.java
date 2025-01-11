package com.bookwise.backend.model;

import lombok.Data;

import java.util.Map;

@Data
public class Challenge {
    private String id; // Unique ID for the challenge
    private String name; // Name of the challenge
    private ChallengeType type; // Enum for type: BOOK_COUNT, PAGE_COUNT, GENRE, AUTHOR
    private Map<String, Object> criteria; // Criteria for the challenge (varies by type)
    private String startDate; // ISO 8601 format: "2025-01-01T00:00:00Z"
    private String endDate;   // ISO 8601 format: "2025-12-31T23:59:59Z"
    private Map<String, Participant> participants; // Map of participants (userId to Participant object)

    @Data
    public static class Participant {
        private int progress; // Progress of the participant
    }

    public enum ChallengeType {
        BOOK_COUNT, PAGE_COUNT, GENRE, AUTHOR
    }
}