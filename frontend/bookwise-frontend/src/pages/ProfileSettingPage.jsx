import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import api from "./api";
import styles from "../styles/ProfileSettings.module.css"; // Import the CSS file

const ProfileSettingsPage = () => {
    const navigate = useNavigate(); // Navigation hook

    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [successMessage, setSuccessMessage] = useState("");

    useEffect(() => {
        const fetchUserProfile = async () => {
            const userId = localStorage.getItem("userId");
            if (!userId) {
                setError("Не сте влезли в системата.");
                return;
            }

            try {
                const response = await api.get(`/users/${userId}`);
                setUsername(response.data.username || "");
                setEmail(response.data.email || "");
                setError("");
            } catch (err) {
                console.error("Грешка при зареждане на профила:", err);
                setError("Неуспешно зареждане на профила.");
            }
        };

        fetchUserProfile();
    }, []);

    const updateField = async (field, value) => {
        const userId = localStorage.getItem("userId");
        if (!userId) {
            setError("Не сте влезли в системата.");
            return;
        }

        try {
            await api.put(`/users/${userId}/profile`, { [field]: value });
            setSuccessMessage(`Успешно актуализирано: ${field}`);
            setError("");
        } catch (err) {
            console.error(`Грешка при актуализиране на ${field}:`, err);
            setError(`Неуспешно актуализиране на ${field}.`);
        }
    };

    const validateEmail = (email) => /\S+@\S+\.\S+/.test(email);
    const validatePassword = (password) => password.length >= 8;

    return (
        <div className={styles.pageWrapper}>
            {/* Brown Box Header with Gold Animation */}
            <header className={styles.header}>
                <h2>Profile Settings</h2>
                <button onClick={() => navigate("/dashboard")} className={styles.backButton}>
                    Back
                </button>
            </header>

            {error && <p className={`${styles.message} ${styles.error}`}>{error}</p>}
            {successMessage && <p className={`${styles.message} ${styles.success}`}>{successMessage}</p>}

            <div className={styles.fieldWrapper}>
                <label className={styles.label}>Username:</label>
                <input
                    type="text"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    className={styles.input}
                />
                <button onClick={() => updateField("username", username)} className={styles.button}>
                    Change Username
                </button>
            </div>

            <div className={styles.fieldWrapper}>
                <label className={styles.label}>Email:</label>
                <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className={styles.input}
                />
                <button
                    onClick={() => {
                        if (validateEmail(email)) {
                            updateField("email", email);
                        } else {
                            setError("Enter valid email.");
                        }
                    }}
                    className={styles.button}
                >
                    Change Email
                </button>
            </div>

            <div className={styles.fieldWrapper}>
                <label className={styles.label}>Password:</label>
                <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className={styles.input}
                />
                <button
                    onClick={() => {
                        if (validatePassword(password)) {
                            updateField("password", password);
                        } else {
                            setError("Password must be at least 8 characters.");
                        }
                    }}
                    className={styles.button}
                >
                    Change Password
                </button>
            </div>
        </div>
    );
};

export default ProfileSettingsPage;
