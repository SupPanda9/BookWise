import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

const HomePage = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        email: "",
        password: "",
    });
    const [message, setMessage] = useState("");

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post("http://localhost:8080/auth/login", formData, {
                headers: {
                    "Content-Type": "application/json",
                },
            });

            localStorage.setItem("userId", response.data.userId);
            localStorage.setItem("token", response.data.token);
            setMessage("Входът е успешен!");

            // Пренасочване към Dashboard
            navigate("/dashboard");
        } catch (error) {
            console.error("Грешка при входа:", error);
            setMessage(error.response?.data || "Грешка при входа.");
        }
    };

    return (
        <div style={{ padding: "20px", textAlign: "center" }}>
            <h1>Добре дошли в BookWise!</h1>
            <p>Влезте в своя акаунт или се регистрирайте, ако нямате такъв.</p>
            <form onSubmit={handleLogin} style={{ marginBottom: "20px" }}>
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
                <button
                    type="submit"
                    style={{
                        padding: "10px 20px",
                        backgroundColor: "#28a745",
                        color: "#fff",
                        border: "none",
                        borderRadius: "5px",
                        cursor: "pointer",
                    }}
                >
                    Вход
                </button>
            </form>
            <button
                onClick={() => navigate("/registration")}
                style={{
                    padding: "10px 20px",
                    backgroundColor: "#007BFF",
                    color: "#fff",
                    border: "none",
                    borderRadius: "5px",
                    cursor: "pointer",
                }}
            >
                Регистрация
            </button>
            {message && <p style={{ marginTop: "20px", color: message.includes("успешен") ? "green" : "red" }}>{message}</p>}
        </div>
    );
};

export default HomePage;
