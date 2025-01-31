package com.bookwise.backend.service;

import com.bookwise.backend.model.ReadingDiaryEntry;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
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
        DocumentReference userRef = db.collection("readingDiary").document(userId);
        ApiFuture<DocumentSnapshot> future = userRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            Map<String, Object> data = document.getData();
            System.out.println("🔥 Fetched diary data: " + data); // Debug log
            return data;
        } else {
            System.out.println("❌ No diary entries found for user: " + userId);
            return new HashMap<>();
        }
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

    public void addOrUpdateEntry(String userId, String bookId, String notes, boolean isPublic) throws Exception {
        DocumentReference userRef = db.collection("readingDiary").document(userId);
        ApiFuture<DocumentSnapshot> future = userRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            Map<String, Object> data = document.getData();
            Map<String, Object> entries = (Map<String, Object>) data.get("entries");

            if (entries == null) {
                entries = new HashMap<>();
            }

            if (entries.containsKey(bookId)) {
                // Update existing entry
                Map<String, Object> entry = (Map<String, Object>) entries.get(bookId);
                entry.put("notes", notes);
                entry.put("public", isPublic);
            } else {
                // Create new entry
                Map<String, Object> newEntry = new HashMap<>();
                newEntry.put("id", UUID.randomUUID().toString());
                newEntry.put("bookId", bookId);
                newEntry.put("notes", notes);
                newEntry.put("public", isPublic);
                newEntry.put("timestamp", Instant.now().toString());

                entries.put(bookId, newEntry);
            }

            userRef.update("entries", entries).get();
        } else {
            // Create new document
            Map<String, Object> entries = new HashMap<>();
            Map<String, Object> newEntry = new HashMap<>();
            newEntry.put("id", UUID.randomUUID().toString());
            newEntry.put("bookId", bookId);
            newEntry.put("notes", notes);
            newEntry.put("public", isPublic);
            newEntry.put("timestamp", Instant.now().toString());

            entries.put(bookId, newEntry);

            Map<String, Object> diaryData = new HashMap<>();
            diaryData.put("entries", entries);

            userRef.set(diaryData).get();
        }
    }
}
