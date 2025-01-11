package com.bookwise.backend.model;

import lombok.Data;

@Data
public class ReadingDiaryEntry {
    private String id; // Optional: Matches the entryId in Firestore (include if entries are accessed independently)
    private String bookId; // ID of the book
    private String notes; // Personal notes
    private boolean isPublic; // Visibility: true = public, false = private
    private String timestamp; // Timestamp of the entry creation/update
}
