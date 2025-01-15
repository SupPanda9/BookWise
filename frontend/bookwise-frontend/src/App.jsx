import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import HomePage from "./pages/HomePage";
import RegistrationPage from "./pages/RegistrationPage";
import Dashboard from "./pages/Dashboard";
import SearchPage from "./pages/SearchPage";
import BookDetailsPage from "./pages/BookDetailsPage";
import CollectionsPage from "./pages/CollectionsPage";
import CollectionDetailsPage from "./pages/CollectionDetailsPage";
import ProfileSettingsPage from "./pages/ProfileSettingPage";

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<HomePage />} />
                <Route path="/registration" element={<RegistrationPage />} />
                <Route path="/dashboard" element={<Dashboard />} />
                {/* Placeholder маршрути */}
                <Route path="/collections" element={<CollectionsPage />} />
                <Route path="/collections/:collectionId" element={<CollectionDetailsPage />} />
                <Route path="/profile" element={<ProfileSettingsPage />} />
                <Route path="/challenges" element={<h1>Предизвикателства</h1>} />
                <Route path="/search" element={<SearchPage />} />
                <Route path="/books/:googleBooksId" element={<BookDetailsPage />} />
            </Routes>
        </Router>
    );
}

export default App;
