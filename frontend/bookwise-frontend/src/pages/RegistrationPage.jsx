import React, { useState, useEffect } from "react";
import api from "./api";
import { useNavigate } from "react-router-dom";
import styles from "../styles/RegistrationPage.module.css";

const RegistrationPage = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        username: "",
        email: "",
        password: "",
        preferences: [], // List of selected genres
    });
    const [message, setMessage] = useState("");
    const [isSuccess, setIsSuccess] = useState(false); // Track success for redirection

    const popularGenres = [
        "Fantasy", "Science Fiction", "Mystery", "Thriller", 
        "Romance", "Horror", "Adventure", "Biography", "Historical"
    ];

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleGenreSelect = (genre) => {
        setFormData((prevData) => {
            if (prevData.preferences.includes(genre)) {
                return { ...prevData, preferences: prevData.preferences.filter((g) => g !== genre) };
            } else {
                return { ...prevData, preferences: [...prevData.preferences, genre] };
            }
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        const payload = {
            username: formData.username,
            email: formData.email,
            password: formData.password,
            preferences: {
                genres: formData.preferences.map((genre) => ({
                    genre,
                    lastActive: new Date().toISOString(),
                })),
            },
        };

        try {
            await api.post("/auth/register", payload, {
                headers: { "Content-Type": "application/json" },
            });

            await api.post("/auth/send-confirmation", null, {
                params: { email: formData.email },
            });

            setMessage("🎉 Registration successful! Redirecting to homepage...");
            setIsSuccess(true);

            // Redirect after 5 seconds
            setTimeout(() => navigate("/"), 5000);
        } catch (error) {
            console.error("Registration error:", error);
            setMessage(error.response?.data || "Registration error.");
            setIsSuccess(false);
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
                        />
                    </div>

                    <div className={styles.genresTitle}>Choose preferred genres:</div>
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
                        <button type="submit" className={styles.registerButton}>Register</button>
                        <button type="button" onClick={() => navigate("/")} className={styles.backButton}>
                            Back to Home Page
                        </button>
                    </div>
                </form>

                {message && (
                    <p className={`${styles.message} ${isSuccess ? styles.success : styles.error}`}>
                        {message}
                    </p>
                )}
            </div>
        </div>
    );
};

export default RegistrationPage;
