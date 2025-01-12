import React, { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

const LoginPage = () => {
    const [formData, setFormData] = useState({ email: "", password: "" });
    const [message, setMessage] = useState("");
    const navigate = useNavigate();

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post("http://localhost:8080/auth/login", formData);
            localStorage.setItem("token", response.data.token);
            setMessage("Входът е успешен!");

            console.log("Пренасочваме към таблото...");
            navigate("/dashboard");
        } catch (error) {
            setMessage(error.response?.data || "Error occurred");
        }
    };

    return (
        <div>
            <h1>Вход</h1>
            <form onSubmit={handleSubmit}>
                <input
                    type="email"
                    name="email"
                    placeholder="Имейл"
                    onChange={handleChange}
                    required
                />
                <input
                    type="password"
                    name="password"
                    placeholder="Парола"
                    onChange={handleChange}
                    required
                />
                <button type="submit">Вход</button>
            </form>
            {message && <p>{message}</p>}
        </div>
    );
};

export default LoginPage;
