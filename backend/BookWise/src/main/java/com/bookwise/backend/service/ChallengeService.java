package com.bookwise.backend.service;

import com.bookwise.backend.model.Challenge;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ChallengeService {

    private final Firestore db = FirestoreClient.getFirestore();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm:ss a z");

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    public Challenge createChallenge(Challenge challenge) throws ExecutionException, InterruptedException {
        // Ensure date strings are formatted properly
        challenge.setStartDate(ISO_FORMATTER.format(Instant.parse(challenge.getStartDate())));
        challenge.setEndDate(ISO_FORMATTER.format(Instant.parse(challenge.getEndDate())));

        challenge.setId(db.collection("challenges").document().getId());
        db.collection("challenges").document(challenge.getId()).set(challenge).get();
        return challenge;
    }

    public Challenge getChallengeById(String id) throws ExecutionException, InterruptedException {
        var doc = db.collection("challenges").document(id).get().get();
        if (!doc.exists()) {
            throw new RuntimeException("Challenge not found");
        }
        return doc.toObject(Challenge.class);
    }

    public void joinChallenge(String challengeId, String userId) throws ExecutionException, InterruptedException {
        var doc = db.collection("challenges").document(challengeId).get().get();
        if (!doc.exists()) {
            throw new RuntimeException("Challenge not found");
        }

        Challenge challenge = doc.toObject(Challenge.class);

        // If user already joined, do nothing
        if (challenge.getParticipants() != null && challenge.getParticipants().containsKey(userId)) {
            return;
        }

        Challenge.Participant participant = new Challenge.Participant();
        participant.setProgress(0);

        if (challenge.getParticipants() == null) {
            challenge.setParticipants(new HashMap<>());
        }

        challenge.getParticipants().put(userId, participant);
        db.collection("challenges").document(challengeId).set(challenge).get();
    }

    public void updateProgress(String challengeId, String userId, int progress) throws ExecutionException, InterruptedException {
        var doc = db.collection("challenges").document(challengeId).get().get();
        if (!doc.exists()) {
            throw new RuntimeException("Challenge not found");
        }

        Challenge challenge = doc.toObject(Challenge.class);

        if (challenge.getParticipants() == null || !challenge.getParticipants().containsKey(userId)) {
            throw new RuntimeException("User has not joined the challenge");
        }

        Challenge.Participant participant = challenge.getParticipants().get(userId);
        participant.setProgress(participant.getProgress() + progress);

        challenge.getParticipants().put(userId, participant);
        db.collection("challenges").document(challengeId).set(challenge).get();
    }

    public void deleteChallenge(String challengeId) throws ExecutionException, InterruptedException {
        db.collection("challenges").document(challengeId).delete().get();
    }

    private void validateDates(String startDate, String endDate) {
        try {
            dateFormat.parse(startDate);
            dateFormat.parse(endDate);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format. Use: 'MMMM d, yyyy 'at' h:mm:ss a z'");
        }
    }

    public List<Challenge> getAllChallenges() throws ExecutionException, InterruptedException {
        List<Challenge> challenges = new ArrayList<>();
        var docs = db.collection("challenges").get().get();

        for (var doc : docs.getDocuments()) {
            challenges.add(doc.toObject(Challenge.class));
        }
        return challenges;
    }
}
