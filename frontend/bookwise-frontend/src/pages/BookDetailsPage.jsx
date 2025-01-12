import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";

const BookDetailsPage = () => {
    const { googleBooksId } = useParams();
    const [book, setBook] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const fetchBookDetails = async () => {
            const userId = localStorage.getItem("userId"); // Извличаме userId
            if (!userId) {
                setError("Не сте влезли в системата.");
                setLoading(false);
                return;
            }

            try {
                const response = await axios.get(`http://localhost:8080/books/${googleBooksId}`, {
                    params: { userId }, // Подаваме userId
                });
                setBook(response.data);
            } catch (err) {
                console.error("Грешка при зареждане на детайлите на книгата:", err);
                setError("Грешка при зареждане на информацията за книгата.");
            } finally {
                setLoading(false);
            }
        };

        fetchBookDetails();
    }, [googleBooksId]);

    if (loading) return <p>Зареждане...</p>;
    if (error) return <p style={{ color: "red" }}>{error}</p>;

    return (
        <div style={{ padding: "20px" }}>
            <h1>{book.title}</h1>
            <img src={book.coverImage} alt={book.title} style={{ width: "300px", height: "450px", objectFit: "cover" }} />
            <p><strong>Автори:</strong> {book.authors.join(", ")}</p>
            <p><strong>Жанрове:</strong> {book.genres.join(", ")}</p>
            <p><strong>Описание:</strong> {book.description}</p>
            <p><strong>Страници:</strong> {book.pageCount}</p>
        </div>
    );
};

export default BookDetailsPage;
