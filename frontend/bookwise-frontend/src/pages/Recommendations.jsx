import React, { useState, useEffect } from "react";
import api from "./api";
import { useNavigate } from "react-router-dom";
import styles from "../styles/Recommendations.module.css";

const Recommendations = () => {
    const navigate = useNavigate();
    const [query, setQuery] = useState("");
    const [genres, setGenres] = useState("");
    const [recommendations, setRecommendations] = useState([]);
    const [visibleBooks, setVisibleBooks] = useState(10);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false); // 🔄 New loading state

    useEffect(() => {
        const savedRecommendations = sessionStorage.getItem("recommendations");
        if (savedRecommendations) {
            setRecommendations(JSON.parse(savedRecommendations));
        }
    }, []);

    const fetchRecommendations = async () => {
        const userId = localStorage.getItem("userId");
        if (!userId) {
            setError("Не сте влезли в системата.");
            return;
        }

        const wordCount = query.trim().split(/\s+/).length;
        if (wordCount < 4) {
            setError("Please, enter at least 4 words in the query field!");
            return;
        }

        // Валидация на жанровете
        const genreArray = genres.split(",").map((genre) => genre.trim());
        if (genres && genreArray.some((genre) => genre === "")) {
            setError("Please, separate genres with commas without additional spaces.");
            return;
        }

        setLoading(true); // Show loading spinner
        setError("");

        try {
            const response = await api.post(`/recommendations/${userId}`, {
                query,
                genres: genres.split(",").map((genre) => genre.trim()),
            });

            if (!response.data || !response.data.recommendations) {
                throw new Error("Invalid response format from backend");
            }

            sessionStorage.setItem("recommendations", JSON.stringify(response.data.recommendations));
            setRecommendations(response.data.recommendations);
            setVisibleBooks(10);
        } catch (err) {
            setError("Неуспешно зареждане на препоръките.");
        } finally {
            setLoading(false); // Hide loading spinner after request
        }
    };

    return (
        <div className={styles.pageWrapper}>
            {/* Header */}
            <header className={styles.header}>
                <h2>Recommendations</h2>
                <button onClick={() => navigate("/dashboard")} className={styles.backButton}>
                    Back to Dashboard
                </button>
            </header>

            {/* Instructions */}
            <section className={styles.instructions}>
                <h2 className={styles.title}>Discover Your Next Favorite Book!</h2>
                <p className={styles.subtitle}>
                    Enter a prompt to get recommendations. You can also specify genres (e.g., Sci-Fi, Romance) to refine your search.
                </p>
            </section>

            {/* Search Bars */}
            <div className={styles.controls}>
                <input
                    type="text"
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    placeholder="Search by title, author, or keyword"
                    className={styles.input}
                />
                <input
                    type="text"
                    value={genres}
                    onChange={(e) => setGenres(e.target.value)}
                    placeholder="Genres (comma-separated)"
                    className={styles.input}
                />
                <button onClick={fetchRecommendations} className={styles.fetchButton} disabled={loading}>
                    {loading ? "Loading..." : "Get Recommendations"} {/* Change button text while loading */}
                </button>
            </div>

            {/* Loading Spinner */}
            {loading && <div className={styles.spinner}></div>}

            {error && <p className={styles.error}>{error}</p>}

            {/* 📚 Recommendations */}
            <div className={styles.recommendationsContainer}>
                {recommendations.slice(0, visibleBooks).map((book, index) => (
                    <div key={index} className={styles.bookCard} onClick={() => navigate(`/books/${book.googleBooksId}`)}>
                        <img 
                            src={book.thumbnail || "https://via.placeholder.com/150"} 
                            alt={`${book.title} thumbnail`} 
                            className={styles.bookImage} 
                        />
                        <div className={styles.bookDetails}>
                            <h4>{book.title || "Няма заглавие"}</h4>
                            <p><strong>Authors:</strong> {book.authors?.join(", ") || "Неизвестни автори"}</p>
                            <p><strong>Genres:</strong> {book.categories?.join(", ") || "Няма жанрове"}</p>
                            <p><strong>Description:</strong> {book.description || "Няма описание"}</p>
                            <p><strong>Similarity score:</strong> {(book.similarity * 100).toFixed(2)}%</p>
                        </div>
                    </div>
                ))}

                {visibleBooks < recommendations.length && (
                    <button onClick={() => setVisibleBooks((prev) => prev + 10)} className={styles.loadMoreButton}>
                        Show more recommendations
                    </button>
                )}
            </div>
        </div>
    );
};

export default Recommendations;
