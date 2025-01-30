import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "./api";

const CollectionDetailsPage = () => {
    const { collectionId } = useParams();
    const [collection, setCollection] = useState(null);
    const [error, setError] = useState("");
    const navigate = useNavigate();

    useEffect(() => {
        const fetchBooksInCollection = async () => {
            try {
                const response = await api.get(`/collections/${collectionId}/books/details`);
                const booksWithId = response.data.map((book) => ({
                    id: book.googleBooksId, // Задаваме googleBooksId като id
                    ...book, // Запазваме останалите полета
                }));
                setCollection({ ...collection, books: booksWithId });
                console.log("Fetched books from collection:", response.data);
            } catch (err) {
                console.error("Грешка при зареждане на книгите в колекцията:", err);
                setError("Неуспешно зареждане на книгите.");
            }
        };
    
        fetchBooksInCollection();
    }, [collectionId]);    

    const removeBook = async (bookId) => {
        if (!window.confirm("Сигурни ли сте, че искате да премахнете тази книга от колекцията?")) return;

        try {
            await api.delete(`/collections/${collectionId}/books/${bookId}`);
            setCollection((prev) => ({
                ...prev,
                books: prev.books.filter((book) => book.id !== bookId),
            }));
        } catch (err) {
            console.error("Грешка при премахване на книга:", err);
            setError("Неуспешно премахване на книга.");
        }
    };

    const togglePublic = async () => {
        try {
            const response = await api.put(`/collections/${collectionId}`, {
                ...collection,
                isPublic: !collection.isPublic,
            });
            setCollection(response.data);
        } catch (err) {
            console.error("Грешка при промяна на публичността:", err);
        }
    };

    if (error) return <p style={{ color: "red" }}>{error}</p>;
    if (!collection) return <p>Зареждане...</p>;

    return (
        <div style={{ padding: "20px" }}>
            <h1>{collection.name}</h1>
            <button onClick={togglePublic}>
                Публична: {collection.isPublic ? "Да" : "Не"}
            </button>
            <div className="books-grid">
                {collection.books.map((book) => (
                    <div key={book.id} className="book-card">
                        <div
                            className="book-content"
                            onClick={() => navigate(`/books/${book.id}`)} // Пренасочване към детайлите
                        >
                            <img
                                src={book.coverImage}
                                alt={book.title}
                                style={{ width: "150px", height: "200px", objectFit: "cover" }}
                            />
                            <h3>{book.title}</h3>
                        </div>
                        <button
                            onClick={(e) => {
                                e.stopPropagation(); // Предотвратява отварянето на страницата на книгата
                                removeBook(book.id);
                            }}
                            className="btn-danger"
                        >
                            Премахни
                        </button>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default CollectionDetailsPage;
