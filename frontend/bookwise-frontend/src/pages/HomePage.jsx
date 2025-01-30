import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "./api";
import styles from "../styles/HomePage.module.css"; 

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
            const response = await api.post("/auth/login", formData, {
                headers: {
                    "Content-Type": "application/json",
                },
            });

            const token = response.data.token;
            if (token) {
                localStorage.setItem("jwtToken", token);
                localStorage.setItem("userId", response.data.userId);
            }
            setMessage("Successfully logged in!");

            // Пренасочване към Dashboard
            navigate("/dashboard");
        } catch (error) {
            console.error("Login error:", error);
            setMessage(error.response?.data || "Login error.");
        }
    };

    return (
        <div className={styles.pageWrapper}>
            {/* Left Side: Login Form */}
            <div className={styles.leftContainer}>
                <h1 className={styles.formTitle}>Welcome to BookWise!</h1>
                <p className={styles.subtitle}>Log into your account or register if you don't have one.</p>

                <form onSubmit={handleLogin} className={styles.loginForm}>
                    <div className={styles.inputRow}>
                        <input
                            type="email"
                            name="email"
                            placeholder="Имейл"
                            onChange={handleChange}
                            value={formData.email}
                            required
                        />
                    </div>
                    <div className={styles.inputRow}>
                        <input
                            type="password"
                            name="password"
                            placeholder="Парола"
                            onChange={handleChange}
                            value={formData.password}
                            required
                        />
                    </div>

                    <div className={styles.buttonsRow}>
                        <button type="submit" className={styles.loginButton}>
                            Login
                        </button>
                        <button
                            type="button"
                            onClick={() => navigate("/registration")}
                            className={styles.registerButton}
                        >
                            Register
                        </button>
                    </div>
                </form>

                {message && (
                    <p className={`${styles.message} ${message.includes("успешен") ? styles.success : styles.error}`}>
                        {message}
                    </p>
                )}
            </div>

            {/* Right Side: Books Image */}
            <div className={styles.rightContainer}></div>
        </div>
    );
};

export default HomePage;
