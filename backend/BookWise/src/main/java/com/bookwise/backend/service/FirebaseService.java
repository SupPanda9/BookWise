package com.bookwise.backend.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FirebaseService {

    public List<Map<String, Object>> getAllBooks() throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection("Books").get();
        QuerySnapshot querySnapshot = future.get();

        // Преобразуване на резултатите в Map<String, Object>
        List<Map<String, Object>> books = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
            books.add(document.getData());
        }
        return books;
    }
}
