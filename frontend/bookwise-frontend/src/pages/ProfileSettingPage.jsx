import React, { useState, useEffect } from "react";
import api from "./api";

const ProfileSettingsPage = () => {
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
        <div style={{ padding: "20px" }}>
            <h1>Настройки на профила</h1>
            {error && <p style={{ color: "red" }}>{error}</p>}
            {successMessage && <p style={{ color: "green" }}>{successMessage}</p>}
            <div>
                <label>Потребителско име:</label>
                <input
                    type="text"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    style={{ padding: "10px", margin: "10px 0", display: "block" }}
                />
                <button
                    onClick={() => updateField("username", username)}
                    style={buttonStyle}
                >
                    Актуализирай потребителското име
                </button>
            </div>
            <div>
                <label>Имейл:</label>
                <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    style={{ padding: "10px", margin: "10px 0", display: "block" }}
                />
                <button
                    onClick={() => {
                        if (validateEmail(email)) {
                            updateField("email", email);
                        } else {
                            setError("Моля, въведете валиден имейл.");
                        }
                    }}
                    style={buttonStyle}
                >
                    Актуализирай имейла
                </button>
            </div>
            <div>
                <label>Парола:</label>
                <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    style={{ padding: "10px", margin: "10px 0", display: "block" }}
                />
                <button
                    onClick={() => {
                        if (validatePassword(password)) {
                            updateField("password", password);
                        } else {
                            setError("Паролата трябва да бъде поне 8 символа.");
                        }
                    }}
                    style={buttonStyle}
                >
                    Актуализирай паролата
                </button>
            </div>
        </div>
    );
};

const buttonStyle = {
    padding: "10px 20px",
    backgroundColor: "#007BFF",
    color: "#fff",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
    marginTop: "10px",
};

export default ProfileSettingsPage;
