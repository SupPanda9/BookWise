import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import api from "./api";
import styles from "../styles/Collections.module.css";

const CollectionsPage = () => {
    const [collections, setCollections] = useState([]);
    const [error, setError] = useState("");
    const [sortOrder, setSortOrder] = useState("asc");
    const navigate = useNavigate();
    const [showModal, setShowModal] = useState(false); 
    const [newCollectionName, setNewCollectionName] = useState("");

    useEffect(() => {
        const fetchCollections = async () => {
            const userId = localStorage.getItem("userId");
            if (!userId) {
                setError("Не сте влезли в системата.");
                return;
            }

            try {
                const response = await api.get("/collections", { params: { userId } });
                const sortedCollections = sortCollections(response.data, "asc");
                setCollections(sortedCollections);
            } catch (err) {
                console.error("Грешка при зареждане на колекции:", err);
                setError("Неуспешно зареждане на колекциите.");
            }
        };

        fetchCollections();
    }, []);

    const sortCollections = (collections, order) => {
        return [...collections].sort((a, b) => {
            if (a.name === "Read") return -1;
            if (b.name === "Read") return 1;
            return order === "asc" ? a.name.localeCompare(b.name) : b.name.localeCompare(a.name);
        });
    };

    const toggleSortOrder = () => {
        const newOrder = sortOrder === "asc" ? "desc" : "asc";
        setSortOrder(newOrder);
        setCollections(sortCollections(collections, newOrder));
    };

    const deleteCollection = async (collectionId, collectionName) => {
        if (collectionName === "Read") return;
        if (!window.confirm("Сигурни ли сте, че искате да изтриете тази колекция?")) return;

        try {
            await api.delete(`/collections/${collectionId}`);
            setCollections((prev) => prev.filter((collection) => collection.id !== collectionId));
        } catch (err) {
            console.error("Грешка при изтриване на колекция:", err);
            setError("Неуспешно изтриване на колекция.");
        }
    };

    const createCollection = async () => {
        const userId = localStorage.getItem("userId");

        if (!newCollectionName.trim()) {
            alert("Моля, въведете име за колекцията.");
            return;
        }

        const isDuplicate = collections.some(
            (collection) => collection.name.toLowerCase() === newCollectionName.trim().toLowerCase()
        );

        if (isDuplicate) {
            alert("Вече съществува колекция с това име.");
            return;
        }

        try {
            const response = await api.post("/collections", {
                userId,
                name: newCollectionName.trim(),
                isPublic: false,
            });

            const newCollection = response.data;
            setCollections((prev) => [...prev, newCollection]);
            setNewCollectionName("");
            setShowModal(false);
        } catch (err) {
            console.error("Грешка при създаване на колекция:", err);
        }
    };

    return (
        <div className={styles["collections-page-wrapper"]}>
            {/* Title Bar */}
            <nav className={styles.titleBar}>
                <h2 className={styles.title}>Collections</h2>
                <span className={styles.backButton} onClick={() => navigate("/dashboard")}>
                    Back
                </span>
            </nav>

            {/* Sort Button and Create Button*/}
            <div className={styles.buttonContainer}>
                <button className={styles.sortButton} onClick={toggleSortOrder}>
                    Sort {sortOrder === "asc" ? "↓" : "↑"}
                </button>
                <button className={styles.createButton} onClick={() => setShowModal(true)}>
                    Create Collection
                </button>
            </div>
            
            {/* Collections List */}
            <div className={styles.collectionsList}>
            {collections.map((collection) => (
                <div 
                    key={collection.id} 
                    className={styles.collectionItem}
                    onClick={() => {
                        if (collection.name === "Read") {
                            navigate(`/reading-diary/${collection.id}`);
                        } else {
                            navigate(`/collections/${collection.id}`);
                        }
                    }}
                >
                    <span>{collection.name}</span>

                    {collection.name !== "Read" && (
                        <button
                            className={styles.deleteButton}
                            onClick={(e) => {
                                e.stopPropagation(); // Prevents the navigation when clicking delete
                                deleteCollection(collection.id, collection.name);
                            }}
                        >
                            Delete
                        </button>
                    )}
                </div>
            ))}
        </div>

            {error && <p className={styles.errorText}>{error}</p>}

            {/* ✅ Модал за създаване на колекция */}
            {showModal && (
                <div className={styles.modalOverlay}>
                    <div className={styles.modal}>
                        <h3>Create a Collection</h3>
                        <input
                            type="text"
                            value={newCollectionName}
                            onChange={(e) => setNewCollectionName(e.target.value)}
                            placeholder="Collection Name"
                            className={styles.inputField}
                        />
                        <div className={styles.modalButtons}>
                            <button className={styles.createButton} onClick={createCollection}>
                                Create
                            </button>
                            <button className={styles.cancelButton} onClick={() => setShowModal(false)}>
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CollectionsPage;
