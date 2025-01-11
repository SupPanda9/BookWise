package com.bookwise.backend.controller.readingdiary;

import com.bookwise.backend.service.ReadingDiaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/readingDiary")
public class ReadingDiaryController {

    @Autowired
    private ReadingDiaryService readingDiaryService;

    @PostMapping("/{userId}")
    public ResponseEntity<?> addEntry(
        @PathVariable String userId,
        @RequestBody Map<String, Object> request) {
        try {
            String bookId = (String) request.get("bookId");
            String notes = (String) request.get("notes");
            Boolean isPublic = (Boolean) request.get("isPublic");

            readingDiaryService.addEntry(userId, bookId, notes, isPublic != null && isPublic);

            return ResponseEntity.ok("Diary entry added successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{userId}/{entryId}")
    public ResponseEntity<?> updateEntry(
        @PathVariable String userId,
        @PathVariable String entryId,
        @RequestBody Map<String, Object> request
    ) {
        try {
            String notes = (String) request.get("notes");
            Boolean isPublic = (Boolean) request.get("isPublic");

            readingDiaryService.updateEntry(userId, entryId, notes, isPublic);
            return ResponseEntity.ok("Diary entry updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{userId}/{entryId}")
    public ResponseEntity<?> deleteEntry(
        @PathVariable String userId,
        @PathVariable String entryId
    ) {
        try {
            readingDiaryService.deleteEntry(userId, entryId);
            return ResponseEntity.ok("Diary entry deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getDiaryEntries(@PathVariable String userId) {
        try {
            Map<String, Object> entries = readingDiaryService.getDiaryEntries(userId);
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while fetching the reading diary entries.");
        }
    }

    @GetMapping("/{userId}/{entryId}")
    public ResponseEntity<?> getDiaryEntry(
        @PathVariable String userId,
        @PathVariable String entryId
    ) {
        try {
            Map<String, Object> entry = readingDiaryService.getDiaryEntry(userId, entryId);
            return ResponseEntity.ok(entry);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
