import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import "../styles/Collections.css"; // Включваме стилове за картите

const CollectionsPage = () => {
    const [collections, setCollections] = useState([]);
    const [error, setError] = useState("");
    const navigate = useNavigate();

    useEffect(() => {
        const fetchCollections = async () => {
            const userId = localStorage.getItem("userId");
            if (!userId) {
                setError("Не сте влезли в системата.");
                return;
            }

            try {
                const response = await axios.get("http://localhost:8080/collections", {
                    params: { userId },
                });
                setCollections(response.data);
            } catch (err) {
                console.error("Грешка при зареждане на колекции:", err);
                setError("Неуспешно зареждане на колекциите.");
            }
        };

        fetchCollections();
    }, []);

    const deleteCollection = async (collectionId) => {
        if (!window.confirm("Сигурни ли сте, че искате да изтриете тази колекция?")) return;

        try {
            await axios.delete(`http://localhost:8080/collections/${collectionId}`);
            setCollections((prev) => prev.filter((collection) => collection.id !== collectionId));
        } catch (err) {
            console.error("Грешка при изтриване на колекция:", err);
            setError("Неуспешно изтриване на колекция.");
        }
    };

    if (error) return <p style={{ color: "red" }}>{error}</p>;

    return (
        <div style={{ padding: "20px" }}>
            <h1>Вашите колекции</h1>
            <div className="collections-grid">
                {collections.map((collection) => (
                    <div
                        key={collection.id}
                        className="collection-card"
                        onClick={() => navigate(`/collections/${collection.id}`)} // Пренасочване при клик върху картата
                        style={{ cursor: "pointer" }} // Добавяме визуална индикация за клик
                    >
                        <h2>{collection.name}</h2>
                        <p>Публична: {collection.isPublic ? "Да" : "Не"}</p>
                        <button
                            onClick={(e) => {
                                e.stopPropagation(); // Предотвратява пренасочването при клик върху бутона
                                deleteCollection(collection.id);
                            }}
                            className="btn-danger"
                        >
                            Изтрий
                        </button>
                    </div>
                ))}
            </div>
        </div>
    );    
};

export default CollectionsPage;
