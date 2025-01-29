package com.bookwise.backend.service;

import com.bookwise.backend.model.Book;
import com.bookwise.backend.model.Collection;
import com.bookwise.backend.model.User;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CollectionService {

    private final Firestore db = FirestoreClient.getFirestore();

    @Autowired
    private BookService bookService;

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
        var collectionDoc = db.collection("collections").document(collectionId).get().get();
        if (!collectionDoc.exists()) {
            throw new Exception("Collection not found.");
        }

        Collection collection = collectionDoc.toObject(Collection.class);
        if (collection != null && "Read".equals(collection.getName())) {
            throw new Exception("The 'Read' collection cannot be deleted.");
        }

        db.collection("collections").document(collectionId).delete().get();
    }

    public List<Book> getBooksInCollectionWithDetails(String collectionId) throws Exception {
        var collectionDoc = db.collection("collections").document(collectionId).get().get();
        if (!collectionDoc.exists()) {
            throw new Exception("Collection not found.");
        }

        var collection = collectionDoc.toObject(Collection.class);
        if (collection.getBooks() == null || collection.getBooks().isEmpty()) {
            return new ArrayList<>(); // Връщаме празен списък, ако няма книги
        }

        List<Book> books = new ArrayList<>();
        for (String bookId : collection.getBooks()) {
            var bookDoc = db.collection("books").document(bookId).get().get();
            if (bookDoc.exists()) {
                Book book = bookDoc.toObject(Book.class);
                books.add(book);
            } else {
                System.err.println("Book not found in Firestore for ID: " + bookId);
                Book book = new Book();
                book.setGoogleBooksId(bookId);
                book.setDescription("Информацията за книгата не е налична");
                book.setCoverImage(null);
                books.add(book);
            }
        }
        return books;
    }

    public void markBookAsRead(String userId, String bookId) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        var userDoc = db.collection("users").document(userId).get().get();
        if (!userDoc.exists()) {
            throw new Exception("User not found.");
        }

        User user = userDoc.toObject(User.class);
        if (user == null) {
            throw new Exception("Failed to fetch user data.");
        }

        // Ensure readBooks list exists
        List<String> readBooks = user.getReadBooks();
        if (!readBooks.contains(bookId)) {
            readBooks.add(bookId);
            user.setReadBooks(readBooks);
            db.collection("users").document(userId).set(user);
        }

        // Ensure "Read" collection is updated
        addBookToReadCollection(userId, bookId);
    }

    public void addBookToReadCollection(String userId, String bookId) throws Exception {
        Collection readCollection = getOrCreateReadCollection(userId, db);
        if (!readCollection.getBooks().contains(bookId)) {
            readCollection.getBooks().add(bookId);
            db.collection("collections").document(readCollection.getId()).set(readCollection);
        }
    }

    private Collection getOrCreateReadCollection(String userId, Firestore db) throws Exception {
        var querySnapshot = db.collection("collections")
            .whereEqualTo("userId", userId)
            .whereEqualTo("name", "Read")
            .get()
            .get();

        if (querySnapshot.isEmpty()) {
            Collection readCollection = new Collection();
            readCollection.setId(UUID.randomUUID().toString());
            readCollection.setUserId(userId);
            readCollection.setName("Read");
            readCollection.setPublic(false);
            readCollection.setBooks(new ArrayList<>());
            db.collection("collections").document(readCollection.getId()).set(readCollection).get();
            return readCollection;
        } else {
            return querySnapshot.getDocuments().get(0).toObject(Collection.class);
        }
    }

    public void unmarkBookAsRead(String userId, String bookId) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        var userDoc = db.collection("users").document(userId).get().get();
        if (!userDoc.exists()) {
            throw new Exception("User not found.");
        }

        User user = userDoc.toObject(User.class);
        if (user == null) {
            throw new Exception("Failed to fetch user data.");
        }

        // Remove book from readBooks
        List<String> readBooks = user.getReadBooks();
        readBooks.remove(bookId);
        user.setReadBooks(readBooks);
        db.collection("users").document(userId).set(user);

        // Remove book from "Read" collection
        Collection readCollection = getOrCreateReadCollection(userId, db);
        removeBookFromCollection(readCollection.getId(), bookId);
    }
}
