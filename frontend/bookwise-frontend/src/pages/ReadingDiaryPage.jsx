import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import api from "./api";
import styles from "../styles/ReadingDiary.module.css";

const ReadingDiaryPage = () => {
    const { collectionId } = useParams(); // Get Read collection ID
    const [books, setBooks] = useState([]); // Books from "Read" collection
    const [diaryEntries, setDiaryEntries] = useState({}); // User notes
    const [selectedBook, setSelectedBook] = useState(null); // Book currently being edited
    const [tempNotes, setTempNotes] = useState(""); // Temporary text for editing
    const navigate = useNavigate();
    const userId = localStorage.getItem("userId");

    useEffect(() => {
        if (!collectionId) return;
        fetchReadCollection();
        fetchDiaryEntries();
    }, [collectionId]);

    /** Fetch books from the "Read" collection */
    const fetchReadCollection = async () => {
        try {
            const response = await api.get(`/collections/${collectionId}/books/details`);
            setBooks(response.data);
        } catch (err) {
            console.error("Error fetching 'Read' collection:", err);
        }
    };

    /** Fetch existing diary entries */
    const fetchDiaryEntries = async () => {
        try {
            const response = await api.get(`/readingDiary/${userId}`);
            const entries = response.data.entries || {}; 
    
            // Map entries correctly by bookId
            const mappedEntries = Object.values(entries).reduce((acc, entry) => {
                acc[entry.bookId] = entry.notes;
                return acc;
            }, {});
    
            setDiaryEntries(mappedEntries);
        } catch (err) {
            console.error("Error fetching diary entries:", err);
        }
    };    

    /** Open modal for editing a note */
    const openEditModal = (book) => {
        setSelectedBook(book);
        setTempNotes(diaryEntries[book.googleBooksId] || ""); // Load existing notes
    };

    /** Close modal */
    const closeEditModal = () => {
        setSelectedBook(null);
        setTempNotes("");
    };

    /** Save the edited notes */
    const saveNotes = async () => {
        if (!selectedBook) return;
        const bookId = selectedBook.googleBooksId;

        try {
            await api.post(`/readingDiary/${userId}`, {
                bookId,
                notes: tempNotes,
                isPublic: false,
            });

            setDiaryEntries((prev) => ({ ...prev, [bookId]: tempNotes }));
            closeEditModal();
        } catch (err) {
            console.error("Error updating diary entry:", err);
        }
    };


    /** Remove book from diary (same as marking as unread) */
    /** Remove book from diary and mark as unread */
    const toggleReadStatus = async (bookId) => {
        if (!window.confirm("Are you sure you want to remove this book from your diary?")) return;

        try {
            // Step 1: Remove from Read Collection
            await api.delete(`/collections/users/${userId}/read`, {
                data: { bookId },
            });

            // Step 2: Remove from Reading Diary
            await api.delete(`/readingDiary/${userId}/${bookId}`);

            // Step 3: Update State
            setBooks((prev) => prev.filter((book) => book.googleBooksId !== bookId));
            setDiaryEntries((prev) => {
                const updatedEntries = { ...prev };
                delete updatedEntries[bookId];
                return updatedEntries;
            });
        } catch (err) {
            console.error("Error removing book from diary:", err);
        }
    };

    return (
        <div className={styles.pageWrapper}>
            <header className={styles.header}>
                <h2>Reading Diary</h2>
                <button onClick={() => navigate("/collections")} className={styles.backButton}>
                    Back
                </button>
            </header>

            <div className={styles.entryList}>
                {books.length === 0 ? (
                    <p>No books in your reading diary yet.</p>
                ) : (
                    books.map((book) => (
                        <div key={book.googleBooksId} className={styles.entryCard}>
                            <div className={styles.bookInfo}>
                                <img src={book.coverImage} alt={book.title} className={styles.bookCover} />
                                <div>
                                    <h3>{book.title}</h3>
                                    <p><strong>Author:</strong> {book.authors?.join(", ") || "Unknown"}</p>
                                </div>
                            </div>

                            {/* Show Notes Only When Clicking "Edit" */}
                            <button onClick={() => openEditModal(book)} className={styles.editButton}>
                                {diaryEntries[book.googleBooksId] ? "Edit Notes" : "Add Notes"}
                            </button>

                            <button onClick={() => toggleReadStatus(book.googleBooksId)} className={styles.deleteButton}>
                                Remove from Diary
                            </button>
                        </div>
                    ))
                )}
            </div>

            {/* Modal for Editing Notes */}
            {selectedBook && (
                <div className={styles.modalOverlay}>
                    <div className={styles.modal}>
                        <h3>Edit Notes for {selectedBook.title}</h3>
                        <textarea
                            className={styles.notesInput}
                            value={tempNotes}
                            onChange={(e) => setTempNotes(e.target.value)}
                        />
                        <div className={styles.modalButtons}>
                            <button onClick={saveNotes} className={styles.saveButton}>
                                Save
                            </button>
                            <button onClick={closeEditModal} className={styles.cancelButton}>
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ReadingDiaryPage;
