import React, { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

const RegistrationPage = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        username: "",
        email: "",
        password: "",
        preferences: [], // Списък от избрани жанрове
    });
    const [message, setMessage] = useState("");

    // Списък с популярни жанрове
    const popularGenres = [
        "Fantasy",
        "Science Fiction",
        "Mystery",
        "Thriller",
        "Romance",
        "Horror",
        "Adventure",
        "Biography",
        "Historical",
    ];

    // Обработва промяната в текстовите полета
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    // Обработва избора на жанрове
    const handleGenreSelect = (genre) => {
        setFormData((prevData) => {
            console.log(prevData.preferences);
            if (prevData.preferences.includes(genre)) {
                // Премахва жанра, ако вече е избран
                return {
                    ...prevData,
                    preferences: prevData.preferences.filter((g) => g !== genre),
                };
            } else {
                // Добавя жанра, ако не е избран
                return {
                    ...prevData,
                    preferences: [...prevData.preferences, genre],
                };
            }
        });
    };

    // Обработва изпращането на формата
    const handleSubmit = async (e) => {
        e.preventDefault();

        // Създаване на обект `preferences` за бекенда
        const payload = {
            username: formData.username,
            email: formData.email,
            password: formData.password,
            preferences: {
                genres: formData.preferences.map((genre) => ({
                    genre,
                    lastActive: new Date().toISOString(), // Добавя текущата дата
                })),
            },
        };

        try {
            const response = await axios.post("http://localhost:8080/auth/register", payload, {
                headers: {
                    "Content-Type": "application/json",
                },
            });
            await axios.post("http://localhost:8080/auth/send-confirmation", null, {
                params: { email: formData.email },
            });
            setMessage("Регистрацията е успешна!");
        } catch (error) {
            console.error("Грешка при регистрацията:", error);
            setMessage(error.response?.data || "Грешка при регистрацията.");
        }
    };

    return (
        <div style={{ padding: "20px" }}>
            <h1>Регистрация</h1>
            <form onSubmit={handleSubmit}>
                <div>
                    <input
                        name="username"
                        placeholder="Потребителско име"
                        onChange={handleChange}
                        value={formData.username}
                        required
                        style={{ padding: "10px", marginBottom: "10px", width: "100%" }}
                    />
                </div>
                <div>
                    <input
                        type="email"
                        name="email"
                        placeholder="Имейл"
                        onChange={handleChange}
                        value={formData.email}
                        required
                        style={{ padding: "10px", marginBottom: "10px", width: "100%" }}
                    />
                </div>
                <div>
                    <input
                        type="password"
                        name="password"
                        placeholder="Парола"
                        onChange={handleChange}
                        value={formData.password}
                        required
                        style={{ padding: "10px", marginBottom: "20px", width: "100%" }}
                    />
                </div>
                <div>
                    <h4>Избери жанрове:</h4>
                    <div style={{ display: "flex", flexWrap: "wrap", gap: "10px" }}>
                        {popularGenres.map((genre) => (
                            <button
                                key={genre}
                                type="button"
                                onClick={() => handleGenreSelect(genre)}
                                style={{
                                    padding: "10px 20px",
                                    borderRadius: "5px",
                                    border: "1px solid #ccc",
                                    backgroundColor: formData.preferences.includes(genre)
                                        ? "#007BFF"
                                        : "#fff",
                                    color: formData.preferences.includes(genre) ? "#fff" : "#000",
                                    cursor: "pointer",
                                }}
                            >
                                {genre}
                            </button>
                        ))}
                    </div>
                </div>
                <button
                    type="submit"
                    style={{
                        marginTop: "20px",
                        padding: "10px 20px",
                        backgroundColor: "#28a745",
                        color: "#fff",
                        border: "none",
                        borderRadius: "5px",
                        cursor: "pointer",
                    }}
                >
                    Регистрация
                </button>
            </form>
            <button
                onClick={() => navigate("/")}
                style={{
                    marginTop: "20px",
                    padding: "10px 20px",
                    backgroundColor: "#007BFF",
                    color: "#fff",
                    border: "none",
                    borderRadius: "5px",
                    cursor: "pointer",
                }}
            >
                Връщане към вход
            </button>
            {message && <p style={{ marginTop: "20px", color: "red" }}>{message}</p>}
        </div>
    );
};

export default RegistrationPage;
