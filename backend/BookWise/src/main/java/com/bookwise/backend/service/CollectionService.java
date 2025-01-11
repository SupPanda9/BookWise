package com.bookwise.backend.service;

import com.bookwise.backend.model.Collection;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CollectionService {

    private final Firestore db = FirestoreClient.getFirestore();

    public String createCollection(String userId, String name, boolean isPublic) throws Exception {
        // Create a new collection
        Collection collection = new Collection();
        collection.setId(UUID.randomUUID().toString());
        collection.setUserId(userId);
        collection.setName(name);
        collection.setPublic(isPublic);
        collection.setBooks(new ArrayList<>());

        // Save the collection in Firestore
        db.collection("collections").document(collection.getId()).set(collection).get();
        return collection.getId();
    }

    public List<Collection> getCollections(String userId) throws Exception {
        // Query collections for the user
        var querySnapshot = db.collection("collections")
            .whereEqualTo("userId", userId).get().get();
        return querySnapshot.toObjects(Collection.class);
    }

    public Collection getCollectionById(String id) throws Exception {
        var doc = db.collection("collections").document(id).get().get();
        if (!doc.exists()) {
            throw new Exception("Collection not found.");
        }
        return doc.toObject(Collection.class);
    }

    public void addBookToCollection(String collectionId, String bookId) throws Exception {
        var collectionDoc = db.collection("collections").document(collectionId).get().get();
        if (!collectionDoc.exists()) {
            throw new Exception("Collection not found.");
        }

        var collection = collectionDoc.toObject(Collection.class);
        if (collection.getBooks() == null) { // Initialize if null
            collection.setBooks(new ArrayList<>());
        }

        if (!collection.getBooks().contains(bookId)) {
            collection.getBooks().add(bookId);
            db.collection("collections").document(collectionId).set(collection).get();
        }
    }

    public void removeBookFromCollection(String collectionId, String bookId) throws Exception {
        var collectionDoc = db.collection("collections").document(collectionId).get().get();
        if (!collectionDoc.exists()) {
            throw new Exception("Collection not found.");
        }

        // Parse the collection
        var collection = collectionDoc.toObject(Collection.class);
        if (collection.getBooks() == null || !collection.getBooks().contains(bookId)) {
            throw new Exception("Book not found in collection.");
        }

        // Remove the book from the collection
        collection.getBooks().remove(bookId);
        db.collection("collections").document(collectionId).set(collection).get();
    }

    public void updateCollection(String collectionId, String name, boolean isPublic) throws Exception {
        db.collection("collections").document(collectionId).update(
            "name", name,
            "isPublic", isPublic
        ).get();
    }

    public void deleteCollection(String collectionId) throws Exception {
        db.collection("collections").document(collectionId).delete().get();
    }
}
