package com.bookwise.backend.service;

import com.bookwise.backend.model.ReadingDiaryEntry;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ReadingDiaryService {

    private final Firestore db = FirestoreClient.getFirestore();

    public void addEntry(String userId, String bookId, String notes, boolean isPublic) throws Exception {
        String timestamp = Instant.now().toString();
        var diaryDoc = db.collection("readingDiary").document(userId).get().get();

        @SuppressWarnings("unchecked")
        Map<String, Object> entries = diaryDoc.exists() ?
            (Map<String, Object>) diaryDoc.get("entries") :
            new HashMap<>();

        if (entries == null) {
            entries = new HashMap<>();
        }
        for (Object value : entries.values()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> entry = (Map<String, Object>) value;
            if (entry != null && bookId.equals(entry.get("bookId"))) {
                throw new Exception("An entry for this book already exists in the reading diary.");
            }
        }
        String entryId = UUID.randomUUID().toString();
        ReadingDiaryEntry newEntry = new ReadingDiaryEntry();
        newEntry.setId(entryId);
        newEntry.setBookId(bookId);
        newEntry.setNotes(notes);
        newEntry.setPublic(isPublic);
        newEntry.setTimestamp(timestamp);

        entries.put(entryId, newEntry);
        db.collection("readingDiary").document(userId).set(Map.of("entries", entries)).get();
    }

    public void updateEntry(String userId, String entryId, String notes, Boolean isPublic) throws Exception {
        Map<String, Object> entries = getDiaryEntries(userId);

        @SuppressWarnings("unchecked")
        Map<String, Object> entry = (Map<String, Object>) entries.get(entryId);
        if (entry == null) {
            throw new Exception("Diary entry not found.");
        }

        // Update fields in the entry
        if (notes != null) {
            entry.put("notes", notes);
        }
        if (isPublic != null) {
            entry.put("public", isPublic);
        }
        entry.put("timestamp", Instant.now().toString());

        entries.put(entryId, entry);
        saveDiaryEntries(userId, entries);
    }

    public void deleteEntry(String userId, String entryId) throws Exception {
        Map<String, Object> entries = getDiaryEntries(userId);

        entries.remove(entryId);
        saveDiaryEntries(userId, entries);
    }

    public Map<String, Object> getDiaryEntries(String userId) throws Exception {
        return getOrCreateDiaryEntries(userId);
    }

    private Map<String, Object> getOrCreateDiaryEntries(String userId) throws Exception {
        var diaryDoc = db.collection("readingDiary").document(userId).get().get();

        @SuppressWarnings("unchecked")
        Map<String, Object> entries = diaryDoc.exists() ?
            (Map<String, Object>) diaryDoc.get("entries") :
            new HashMap<>();

        return entries != null ? entries : new HashMap<>();
    }

    private void saveDiaryEntries(String userId, Map<String, Object> entries) throws Exception {
        db.collection("readingDiary").document(userId).set(Map.of("entries", entries)).get();
    }

    public Map<String, Object> getDiaryEntry(String userId, String entryId) throws Exception {
        Map<String, Object> entries = getOrCreateDiaryEntries(userId);

        @SuppressWarnings("unchecked")
        Map<String, Object> entry = (Map<String, Object>) entries.get(entryId);
        if (entry == null) {
            throw new Exception("Diary entry not found.");
        }

        return entry;
    }

}
