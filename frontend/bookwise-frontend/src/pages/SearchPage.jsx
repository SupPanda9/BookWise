import React, { useState } from "react";
import api from "./api";
import { useNavigate } from "react-router-dom";

const SearchPage = () => {
    const [query, setQuery] = useState(""); // Търсене
    const [books, setBooks] = useState([]); // Резултати от търсенето
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const navigate = useNavigate();

    const handleSearch = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError("");

        try {
            const response = await api.get("/books/search", {
                params: {
                    query,
                    maxResults: 12,
                    sort: "popularity",
                },
            });
            setBooks(response.data);
        } catch (err) {
            console.error("Грешка при търсенето:", err);
            setError("Грешка при зареждането на резултатите.");
        } finally {
            setLoading(false);
        }
    };

    const handleBookClick = (googleBooksId) => {
        console.log(googleBooksId);
        navigate(`/books/${googleBooksId}`);
    };

    return (
        <div style={{ padding: "20px" }}>
            <h1>Търсене на книги</h1>
            <form onSubmit={handleSearch} style={{ marginBottom: "20px" }}>
                <input
                    type="text"
                    placeholder="Търсете по заглавие, жанр, автор..."
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    style={{ padding: "10px", width: "80%", marginRight: "10px" }}
                />
                <button
                    type="submit"
                    style={{
                        padding: "10px 20px",
                        backgroundColor: "#007BFF",
                        color: "#fff",
                        border: "none",
                        borderRadius: "5px",
                        cursor: "pointer",
                    }}
                >
                    Търсене
                </button>
            </form>
            {loading && <p>Зареждане...</p>}
            {error && <p style={{ color: "red" }}>{error}</p>}
            <div style={{ display: "flex", flexWrap: "wrap", gap: "20px" }}>
                {books.map((book) => (
                    <div
                        key={book.googleBooksId}
                        onClick={() => handleBookClick(book.googleBooksId)}
                        style={{
                            width: "200px",
                            padding: "10px",
                            border: "1px solid #ccc",
                            borderRadius: "5px",
                            cursor: "pointer",
                            textAlign: "center",
                        }}
                    >
                        <img
                            src={book.coverImage}
                            alt={book.title}
                            style={{ width: "100%", height: "300px", objectFit: "cover" }}
                        />
                        <h3 style={{ fontSize: "16px", margin: "10px 0" }}>{book.title}</h3>
                        <p style={{ fontSize: "14px", color: "#666" }}>{book.authors.join(", ")}</p>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default SearchPage;
