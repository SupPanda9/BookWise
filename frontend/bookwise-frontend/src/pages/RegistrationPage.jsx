import React, { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import styles from "../styles/RegistrationPage.module.css";

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
            setMessage("Registration successful!");
        } catch (error) {
            console.error("Registration error:", error);
            setMessage(error.response?.data || "Registration error.");
        }
    };

    return (
        <div className={styles.pageWrapper}>
            <div className={styles.formContainer}>
                <h1 className={styles.formTitle}>Registration</h1>
                <form onSubmit={handleSubmit} className={styles.registerForm}>
                    <div className={styles.inputRow}>
                        <input
                            name="username"
                            placeholder="Username"
                            onChange={handleChange}
                            value={formData.username}
                            required
                            style={{ padding: "10px", marginBottom: "10px", width: "100%" }}
                        />
                    </div>
                    <div className={styles.inputRow}>
                        <input
                            type="email"
                            name="email"
                            placeholder="Email"
                            onChange={handleChange}
                            value={formData.email}
                            required
                            style={{ padding: "10px", marginBottom: "10px", width: "100%" }}
                        />
                    </div>
                    <div className={styles.inputRow}>
                        <input
                            type="password"
                            name="password"
                            placeholder="Password"
                            onChange={handleChange}
                            value={formData.password}
                            required
                            style={{ padding: "10px", marginBottom: "20px", width: "100%" }}
                        />
                    </div>
                    <div className={styles.genresTitle}>Choose preferred genres: </div>
                    <div className={`${styles.genresRow} ${styles.genresRow5}`}>
                        {popularGenres.slice(0, 5).map((genre) => (
                            <button
                                key={genre}
                                type="button"
                                onClick={() => handleGenreSelect(genre)}
                                className={
                                    formData.preferences.includes(genre)
                                        ? `${styles.genreButton} ${styles.selected}`
                                        : styles.genreButton
                                }
                            >
                                {genre}
                            </button>
                        ))}
                    </div>
                    <div className={`${styles.genresRow} ${styles.genresRow4}`}>
                        {popularGenres.slice(5, 9).map((genre) => (
                        <button
                            key={genre}
                            type="button"
                            onClick={() => handleGenreSelect(genre)}
                            className={
                            formData.preferences.includes(genre)
                                ? `${styles.genreButton} ${styles.selected}`
                                : styles.genreButton
                            }
                        >
                            {genre}
                        </button>
                        ))}
                    </div>
                    <div className={styles.buttonsRow}>
                        <button type="submit" className={styles.registerButton}>
                            Register
                            </button>
                            <button
                            type="button"
                            onClick={() => navigate("/")}
                            className={styles.backButton}
                            >
                            Back to Home page
                        </button>
                    </div>
                </form>
                {message && <p style={{ marginTop: "20px", color: "red" }}>{message}</p>}
            </div>
        </div>
    );
};

export default RegistrationPage;
