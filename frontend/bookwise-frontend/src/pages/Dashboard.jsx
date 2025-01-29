import React, { useState, useEffect, useRef } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import styles from "../styles/Dashboard.module.css";

const Dashboard = () => {
    const navigate = useNavigate();
    const [query, setQuery] = useState("");
    const [books, setBooks] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [showDropdown, setShowDropdown] = useState(false);
    const searchRef = useRef(null); // Track search container

    // Detect clicks outside search container
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (searchRef.current && !searchRef.current.contains(event.target)) {
                setShowDropdown(false); // Hide dropdown when clicking outside
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const handleInputChange = (e) => {
        setQuery(e.target.value);
        setShowDropdown(true); // Show dropdown when typing
        fetchBooks(e.target.value);
    };

    const fetchBooks = async (searchTerm) => {
        if (!searchTerm) {
            setBooks([]);
            return;
        }

        setLoading(true);
        setError("");

        try {
            const response = await axios.get("http://localhost:8080/books/search", {
                params: { query: searchTerm, maxResults: 5, sort: "popularity" },
            });
            setBooks(response.data);
        } catch (err) {
            setError("Error loading search results.");
        } finally {
            setLoading(false);
        }
    };

    const handleBookClick = (googleBooksId) => {
        navigate(`/books/${googleBooksId}`);
        setQuery("");
        setBooks([]);
        setShowDropdown(false); // Hide dropdown after selection
    };

    return (
        <div className={styles.pageWrapper}>
            <nav className={styles.navbar}>
                <h2 className={styles.logo}>BookWise</h2>
                <div className={styles.navLinks}>
                    <button onClick={() => navigate("/recommendations")}>Recommendations</button>
                    <button onClick={() => navigate("/collections")}>Collections</button>
                    <button onClick={() => navigate("/profile")}>Profile</button>
                    <button onClick={() => navigate("/challenges")}>Challenges</button>
                    <button onClick={() => {
                        localStorage.removeItem("token");
                        localStorage.removeItem("userId");
                        navigate("/");
                    }} className={styles.logoutButton}>Logout</button>
                </div>
            </nav>

            <main className={styles.mainContent}>
                <h1 className={styles.welcomeTitle}>Welcome to BookWise!</h1>

                {/* Search Bar */}
                <div className={styles.searchContainer}>
                    <input
                        type="text"
                        placeholder="Search for books..."
                        value={query}
                        onChange={handleInputChange}
                        className={styles.searchInput}
                    />
                    {loading && <p className={styles.loadingText}>Loading...</p>}

                    {/* Search Results Dropdown */}
                    {books.length > 0 && (
                        <div className={styles.searchDropdown}>
                            {books.map((book) => (
                                <div key={book.googleBooksId} className={styles.searchResult} onClick={() => handleBookClick(book.googleBooksId)}>
                                    <img src={book.coverImage} alt={book.title} className={styles.bookImage} />
                                    <div className={styles.bookInfo}>
                                        <h4>{book.title}</h4>
                                        <p>{book.authors?.join(", ")}</p>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                {error && <p className={styles.errorText}>{error}</p>}
            </main>
        </div>
    );
};

export default Dashboard;
