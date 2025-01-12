import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import HomePage from "./pages/HomePage";
import RegistrationPage from "./pages/RegistrationPage";
import LoginPage from "./pages/LoginPage";
import Dashboard from "./pages/Dashboard";

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<HomePage />} />
                <Route path="/registration" element={<RegistrationPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/dashboard" element={<Dashboard />} />
                {/* Placeholder маршрути */}
                <Route path="/search" element={<h1>Търсене на книги</h1>} />
                <Route path="/collections" element={<h1>Колекции</h1>} />
                <Route path="/profile" element={<h1>Настройки на профила</h1>} />
                <Route path="/challenges" element={<h1>Предизвикателства</h1>} />
            </Routes>
        </Router>
    );
}

export default App;
