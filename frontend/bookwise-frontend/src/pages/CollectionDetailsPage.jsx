import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "./api";
import styles from "../styles/CollectionDetails.module.css";

const CollectionDetailsPage = () => {
    const { collectionId } = useParams();
    const [collection, setCollection] = useState(null);
    const [error, setError] = useState("");
    const navigate = useNavigate();

    useEffect(() => {
        const fetchCollectionDetails = async () => {
            try {
                // Първо зареждаме детайлите за колекцията
                const collectionResponse = await api.get(`/collections/${collectionId}`);
                const collectionData = collectionResponse.data;

                // След това зареждаме книгите
                const booksResponse = await api.get(`/collections/${collectionId}/books/details`);
                const booksWithId = booksResponse.data.map((book) => ({
                    id: book.googleBooksId,
                    ...book,
                }));

                // Комбинираме информацията
                setCollection({ ...collectionData, books: booksWithId });

                console.log("Fetched collection:", collectionData);
                console.log("Fetched books from collection:", booksResponse.data);
            } catch (err) {
                console.error("Error fetching collection details:", err);
                setError("Failed to load collection details.");
            }
        };

        fetchCollectionDetails();
    }, [collectionId]);

    const removeBook = async (bookId) => {
        if (!window.confirm("Are you sure you want to remove this book from the collection?")) return;

        try {
            await api.delete(`/collections/${collectionId}/books/${bookId}`);
            setCollection((prev) => ({
                ...prev,
                books: prev.books.filter((book) => book.id !== bookId),
            }));
        } catch (err) {
            console.error("Error removing book:", err);
            setError("Failed to remove book.");
        }
    };

    if (error) return <p className={styles.error}>{error}</p>;
    if (!collection) return <p className={styles.loading}>Loading...</p>;

    return (
        <div className={styles.pageWrapper}>
            <header className={styles.header}>
                <h2>{collection.name}</h2>
                <button onClick={() => navigate("/collections")} className={styles.backButton}>
                    Back
                </button>
            </header>

            <div className={styles.bookGrid}>
                {collection.books.map((book) => (
                    <div key={book.id} className={styles.bookCard}>
                        <div
                            className={styles.bookContent}
                            onClick={() => navigate(`/books/${book.id}`)}
                        >
                            <img
                                src={book.coverImage}
                                alt={book.title}
                                className={styles.bookCover}
                            />
                            <h3 className={styles.bookTitle}>{book.title}</h3>
                        </div>
                        <button
                            onClick={(e) => {
                                e.stopPropagation();
                                removeBook(book.id);
                            }}
                            className={styles.deleteButton}
                        >
                            Remove
                        </button>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default CollectionDetailsPage;
