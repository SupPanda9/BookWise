import React, { useEffect } from "react";
import { BrowserRouter as Router, Routes, Route, useNavigate } from "react-router-dom";
import HomePage from "./pages/HomePage";
import RegistrationPage from "./pages/RegistrationPage";
import Dashboard from "./pages/Dashboard";
import SearchPage from "./pages/SearchPage";
import BookDetailsPage from "./pages/BookDetailsPage";
import CollectionsPage from "./pages/CollectionsPage";
import CollectionDetailsPage from "./pages/CollectionDetailsPage";
import ProfileSettingsPage from "./pages/ProfileSettingPage";
import EmailConfirmationPage from "./pages/EmailConfirmationPage";
import Recommendations from "./pages/Recommendations";
import "../src/config/firebaseConfig";
import { jwtDecode } from "jwt-decode";

const logout = (navigate) => {
    console.warn("🔴 Token expired. Logging out...");
    localStorage.removeItem("jwtToken");
    localStorage.removeItem("userId");
    sessionStorage.clear();
    navigate("/");
    window.location.reload(); // ✅ Ensure all cached state is cleared
};


// ✅ New component to check token expiration inside the Router
const AuthWrapper = ({ children }) => {
    const navigate = useNavigate();

    useEffect(() => {
        const token = localStorage.getItem("jwtToken");

        if (token) {
            try {
                const decoded = jwtDecode(token);
                const currentTime = Date.now() / 1000; // Convert to seconds

                if (decoded.exp < currentTime) {
                    logout(navigate); // ✅ Token already expired → log out immediately
                } else {
                    // ✅ Set a timer to log out exactly when the token expires
                    const timeUntilExpiration = (decoded.exp - currentTime) * 1000; // Convert to milliseconds
                    console.log(`⏳ Token will expire in ${Math.round(timeUntilExpiration / 1000)} seconds`);

                    const logoutTimer = setTimeout(() => logout(navigate), timeUntilExpiration);

                    // ✅ Cleanup the timer if the component unmounts
                    return () => clearTimeout(logoutTimer);
                }
            } catch (error) {
                console.error("⚠️ Invalid token:", error);
                logout(navigate);
            }
        }
    }, [navigate]);

    return children;
};

function App() {
    return (
        <Router>
            <AuthWrapper>
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/registration" element={<RegistrationPage />} />
                    <Route path="/dashboard" element={<Dashboard />} />
                    <Route path="/collections" element={<CollectionsPage />} />
                    <Route path="/collections/:collectionId" element={<CollectionDetailsPage />} />
                    <Route path="/profile" element={<ProfileSettingsPage />} />
                    <Route path="/challenges" element={<h1>Предизвикателства</h1>} />
                    <Route path="/search" element={<SearchPage />} />
                    <Route path="/books/:googleBooksId" element={<BookDetailsPage />} />
                    <Route path="/confirm-email" element={<EmailConfirmationPage />} />
                    <Route path="/recommendations" element={<Recommendations />} />
                </Routes>
            </AuthWrapper>
        </Router>
    );
}

export default App;
