import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

const Dashboard = () => {
    const navigate = useNavigate();
    const [query, setQuery] = useState(""); // Query for recommendations
    const [genres, setGenres] = useState(""); // Genres for recommendations
    const [recommendations, setRecommendations] = useState([]); // List of books
    const [visibleBooks, setVisibleBooks] = useState(10); // Number of books to show initially
    const [error, setError] = useState("");
    const [showButton, setShowButton] = useState(true); // Control button visibility

    const handleLogout = () => {
        localStorage.removeItem("token");
        navigate("/");
    };

    const fetchRecommendations = async () => {
        const userId = localStorage.getItem("userId");
        if (!userId) {
            setError("Не сте влезли в системата.");
            return;
        }

        try {
            setShowButton(false); // Hide the button when fetching starts
            const response = await axios.post(`http://localhost:8080/recommendations/${userId}`, {
                query,
                genres: genres.split(",").map((genre) => genre.trim()),
            });

            console.log("Recommendations response:", response.data);
            if (!response.data || !response.data.recommendations) {
                throw new Error("Invalid response format from backend");
            }

            setRecommendations(response.data.recommendations);
            setVisibleBooks(10);
            setError("");
        } catch (err) {
            console.error("Грешка при зареждане на препоръките:", err);
            setError("Неуспешно зареждане на препоръките.");
            setShowButton(true);
        }
    };

    return (
        <div style={{ padding: "20px" }}>
            <header
                style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    marginBottom: "20px",
                    borderBottom: "1px solid #ccc",
                    paddingBottom: "10px",
                }}
            >
                <h1>BookWise</h1>
                <nav style={{ display: "flex", gap: "15px" }}>
                    <button onClick={() => navigate("/search")} style={buttonStyle}>
                        Търсене на книги
                    </button>
                    <button onClick={() => navigate("/collections")} style={buttonStyle}>
                        Колекции
                    </button>
                    <button onClick={() => navigate("/profile")} style={buttonStyle}>
                        Настройки на профила
                    </button>
                    <button onClick={() => navigate("/challenges")} style={buttonStyle}>
                        Предизвикателства
                    </button>
                </nav>
                <button
                    onClick={handleLogout}
                    style={{
                        ...buttonStyle,
                        backgroundColor: "#dc3545",
                        color: "#fff",
                    }}
                >
                    Изход
                </button>
            </header>
            <main>
                <h2>Добре дошли в BookWise!</h2>
                <p>Изберете опция от менюто, за да започнете.</p>

                <h3>Препоръки</h3>
                <div>
                    <input
                        type="text"
                        value={query}
                        onChange={(e) => setQuery(e.target.value)}
                        placeholder="Въведете заявка"
                        style={{ padding: "10px", marginRight: "10px" }}
                    />
                    <input
                        type="text"
                        value={genres}
                        onChange={(e) => setGenres(e.target.value)}
                        placeholder="Жанрове (разделени със запетаи)"
                        style={{ padding: "10px", marginRight: "10px" }}
                    />
                    {showButton && (
                        <button onClick={fetchRecommendations} style={buttonStyle}>
                            Вземете препоръки
                        </button>
                    )}
                </div>

                {error && <p style={{ color: "red" }}>{error}</p>}

                <div style={{ marginTop: "20px" }}>
                    {recommendations.slice(0, visibleBooks).map((book, index) => (
                        <div
                            key={index}
                            style={{
                                marginBottom: "20px",
                                border: "1px solid #ccc",
                                padding: "20px",
                                backgroundColor: "#fff",
                                color: "#000",
                                borderRadius: "10px",
                                boxShadow: "0 4px 8px rgba(0,0,0,0.1)",
                                cursor: "pointer",
                            }}
                            onClick={() => navigate(`/books/${book.googleBooksId}`)} // Тук използваме googleBooksId за пренасочване
                        >
                            <div style={{ display: "flex", gap: "15px" }}>
                                <img
                                    src={book.thumbnail || "https://via.placeholder.com/150"}
                                    alt={`${book.title} thumbnail`}
                                    style={{
                                        width: "150px",
                                        height: "200px",
                                        objectFit: "cover",
                                        borderRadius: "5px",
                                    }}
                                />
                                <div>
                                    <h4>{book.title || "Няма заглавие"}</h4>
                                    <p>
                                        <strong>Google Books ID:</strong> {book.googleBooksId || "Няма ID"}
                                    </p>
                                    <p>
                                        <strong>Автори:</strong> {book.authors?.join(", ") || "Неизвестни автори"}
                                    </p>
                                    <p>
                                        <strong>Жанрове:</strong> {book.categories?.join(", ") || "Няма жанрове"}
                                    </p>
                                    <p>
                                        <strong>Описание:</strong> {book.description || "Няма описание"}
                                    </p>
                                    <p>
                                        <strong>Коефициент на точност:</strong> {book.similarity || "Няма рейтинг"}
                                    </p>
                                </div>
                            </div>
                        </div>
                    ))}
                    {visibleBooks < recommendations.length && (
                        <button
                            onClick={() => setVisibleBooks((prev) => prev + 10)}
                            style={{ padding: "10px", marginTop: "10px" }}
                        >
                            Покажи още
                        </button>
                    )}
                </div>
            </main>
        </div>
    );
};

const buttonStyle = {
    padding: "10px 15px",
    backgroundColor: "#007BFF",
    color: "#fff",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
};

export default Dashboard;
