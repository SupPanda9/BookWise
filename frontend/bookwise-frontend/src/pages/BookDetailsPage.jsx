import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";
import "../styles/Modal.css";

const BookDetailsPage = () => {
    const { googleBooksId } = useParams();
    const [book, setBook] = useState(null);
    const [reviews, setReviews] = useState([]); // Списък с ревюта
    const [newReview, setNewReview] = useState(""); // Ново ревю
    const [rating, setRating] = useState(0); // Рейтинг от 1 до 5
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [visibleReviews, setVisibleReviews] = useState(5); // Започваме с 5 ревюта
    const [editingReview, setEditingReview] = useState(null); // Текущо ревю за редакция
    const [editedText, setEditedText] = useState(""); // Променен текст
    const [editedRating, setEditedRating] = useState(0); // Променен рейтинг

    const [showModal, setShowModal] = useState(false); // Управление на видимостта на модала
    const [collections, setCollections] = useState([]); // Всички колекции на потребителя
    const [selectedCollections, setSelectedCollections] = useState(new Set()); // Избраните колекции
    const [newCollectionName, setNewCollectionName] = useState(""); // Име на новата колекция

    useEffect(() => {
        const fetchBookDetails = async () => {
            const userId = localStorage.getItem("userId"); // Извличаме userId

            if (!userId) {
                setError("Не сте влезли в системата.");
                setLoading(false);
                return;
            }

            try {
                const response = await axios.get(`http://localhost:8080/books/${googleBooksId}`, {
                    params: { userId },
                });
                setBook(response.data);

                // Зареждане на ревюта
                const reviewsResponse = await axios.get(`http://localhost:8080/reviews/${googleBooksId}`);
                const reviewsData = Object.values(reviewsResponse.data);

                // Ако няма ревюта, показваме празно съобщение
                if (reviewsData.length === 0) {
                    console.log("Няма намерени ревюта за тази книга.");
                    setReviews([]);
                } else {
                    setReviews(Object.values(reviewsResponse.data));
                }
                
            } catch (err) {
                console.error("Грешка при зареждане на детайлите на книгата:", err);
                setError("Грешка при зареждане на информацията за книгата.");
            } finally {
                setLoading(false);
            }
        };

        fetchBookDetails();
    }, [googleBooksId]);

    const handleAddReview = async () => {
        const userId = localStorage.getItem("userId");
        if (!userId || !newReview || rating < 1) {
            alert("Моля, попълнете ревюто и изберете рейтинг.");
            return;
        }
    
        try {
            const response = await axios.post(`http://localhost:8080/reviews/${googleBooksId}`, {
                text: newReview,
                rating,
                userId,
            });
    
            // Добавяне на новото ревю към съществуващия списък
            setReviews((prevReviews) => [
                ...prevReviews,
                {
                    id: response.data.id,
                    userId: response.data.userId,
                    rating: response.data.rating,
                    text: response.data.text,
                    timestamp: response.data.timestamp || null,
                },
            ]);
    
            setNewReview("");
            setRating(0);
        } catch (err) {
            console.error("Грешка при добавяне на ревю:", err);
            setError("Неуспешно добавяне на ревю.");
        }
    };

    const handleDeleteReview = async (reviewId) => {
        if (!window.confirm("Сигурни ли сте, че искате да изтриете това ревю?")) {
            return;
        }
    
        try {
            await axios.delete(`http://localhost:8080/reviews/${googleBooksId}/${reviewId}`, {
                params: { userId: localStorage.getItem("userId") },
            });
    
            // Актуализиране на списъка с ревюта
            setReviews((prevReviews) => prevReviews.filter((review) => review.id !== reviewId));
        } catch (err) {
            console.error("Грешка при изтриване на ревю:", err);
            setError("Неуспешно изтриване на ревю.");
        }
    };
    
    const handleEditReview = (review) => {
        console.log("Стартира редакция на ревю:", review); // Лог на ревюто
        setEditingReview(review.id); // Задаваме текущото ревю за редакция
        setEditedText(review.text); // Задаваме текста
        setEditedRating(review.rating); // Задаваме рейтинга
    };
    
    const toggleModal = () => {
        setShowModal(!showModal);
    };    
    
    const submitEditReview = async () => {
        try {
            await axios.put(`http://localhost:8080/reviews/${googleBooksId}/${editingReview}`, {
                text: editedText,
                rating: editedRating,
                userId: localStorage.getItem("userId"),
            });
    
            // Актуализиране на списъка с ревюта
            setReviews((prevReviews) =>
                prevReviews.map((review) =>
                    review.id === editingReview
                        ? { ...review, text: editedText, rating: editedRating }
                        : review
                )
            );
    
            // Изчистване на състоянията за редакция
            setEditingReview(null);
            setEditedText("");
            setEditedRating(0);
        } catch (err) {
            console.error("Грешка при редактиране на ревю:", err);
            setError("Неуспешно редактиране на ревю.");
        }
    };

    const calculateAverageRating = () => {
        if (reviews.length === 0) return 0;

        const totalRating = reviews.reduce((sum, review) => sum + review.rating, 0);
        return (totalRating / reviews.length).toFixed(1); // Закръгляне до 1 знак
    };

    const formatTimestamp = (timestamp) => {
        if (!timestamp) return "Неизвестна дата";
    
        const date = new Date(timestamp); // Конвертиране на ISO 8601 в обект Date
        return new Intl.DateTimeFormat("bg-BG", {
            year: "numeric",
            month: "long",
            day: "numeric",
            hour: "2-digit",
            minute: "2-digit",
        }).format(date); // Форматиране в четим формат
    };    

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
                const selected = response.data
                    .filter((collection) => collection.books.includes(googleBooksId))
                    .map((collection) => collection.id);
                setSelectedCollections(new Set(selected));
            } catch (err) {
                console.error("Грешка при зареждане на колекции:", err);
            }
        };
    
        if (showModal) fetchCollections();
    }, [showModal, googleBooksId]);

    const toggleCollection = async (collectionId, isSelected) => {
        try {
            if (isSelected) {
                await axios.post(`http://localhost:8080/collections/${collectionId}/books`, {
                    bookId: googleBooksId,
                });
            } else {
                await axios.delete(`http://localhost:8080/collections/${collectionId}/books/${googleBooksId}`);
            }
    
            // Обновяване на състоянието
            setSelectedCollections((prev) => {
                const updated = new Set(prev);
                if (isSelected) {
                    updated.add(collectionId);
                } else {
                    updated.delete(collectionId);
                }
                return updated;
            });
    
            // Уверете се, че се обновява интерфейсът
            setCollections((prevCollections) =>
                prevCollections.map((collection) =>
                    collection.id === collectionId
                        ? { ...collection, isSelected: !isSelected }
                        : collection
                )
            );
        } catch (err) {
            console.error("Грешка при управление на колекция:", err);
        }
    };
    
    const createCollection = async () => {
        const userId = localStorage.getItem("userId");
        if (!newCollectionName.trim()) {
            alert("Моля, въведете име за колекцията.");
            return;
        }
    
        // Проверка за дублиращо име
        const isDuplicate = collections.some(
            (collection) => collection.name.toLowerCase() === newCollectionName.trim().toLowerCase()
        );
        if (isDuplicate) {
            alert("Вече съществува колекция с това име.");
            return;
        }
    
        try {
            const response = await axios.post("http://localhost:8080/collections", {
                userId,
                name: newCollectionName.trim(),
                isPublic: false,
            });
    
            const newCollection = response.data;
    
            // Актуализираме списъка с колекции
            setCollections((prev) => [...prev, newCollection]);
    
            // Добавяме новата колекция към избраните
            setSelectedCollections((prev) => new Set([...prev, newCollection.id]));
    
            // Принудително обновяване на модала
            setShowModal(false); // Затваряме модала
            setTimeout(() => setShowModal(true), 0); // Отваряме го отново
    
            // Изчистваме формата
            setNewCollectionName("");
        } catch (err) {
            console.error("Грешка при създаване на колекция:", err);
        }
    };

    if (loading) return <p>Зареждане...</p>;
    if (error) return <p style={{ color: "red" }}>{error}</p>;

    return (
        <div style={{ padding: "20px" }}>

            <h1>{book.title}</h1>
            <img src={book.coverImage} alt={book.title} style={{ width: "300px", height: "450px", objectFit: "cover" }} />
            <h2>Среден рейтинг</h2>
            <p style={{ fontSize: "18px", fontWeight: "bold" }}>
                {reviews.length === 0 ? "Няма достатъчно данни" : `${calculateAverageRating()} / 5`}
            </p>
            <hr />
            <p><strong>Автори:</strong> {book.authors.join(", ")}</p>
            <p><strong>Жанрове:</strong> {book.genres.join(", ")}</p>
            <p><strong>Описание:</strong> {book.description}</p>
            <p><strong>Страници:</strong> {book.pageCount}</p>

            <hr />

            <h2>Напишете ревю</h2>
            <textarea
                placeholder="Напишете вашето ревю..."
                value={newReview}
                onChange={(e) => setNewReview(e.target.value)}
                style={{ width: "100%", height: "100px", marginBottom: "10px" }}
            />
            <div>
                {[1, 2, 3, 4, 5].map((star) => (
                    <button
                        key={star}
                        onClick={() => setRating(star)}
                        style={{
                            padding: "10px",
                            marginRight: "5px",
                            backgroundColor: rating >= star ? "#FFD700" : "#ccc",
                            border: "none",
                            borderRadius: "5px",
                            cursor: "pointer",
                        }}
                    >
                        ★
                    </button>
                ))}
            </div>
            <button
                onClick={handleAddReview}
                style={{
                    marginTop: "10px",
                    padding: "10px 20px",
                    backgroundColor: "#28a745",
                    color: "#fff",
                    border: "none",
                    borderRadius: "5px",
                    cursor: "pointer",
                }}
            >
                Добави ревю
            </button>

            <button onClick={toggleModal}>Добави към колекции</button>
            {showModal && ( // Тук започва модалът
                <>
                    <div className="modal-overlay" onClick={toggleModal}></div>
                    <div className="modal">
                        <h3>Управление на колекции</h3>
                        <div>
                            {collections.map((collection) => (
                                <button
                                    key={collection.id}
                                    onClick={() => toggleCollection(collection.id, !selectedCollections.has(collection.id))}
                                    style={{
                                        padding: "10px 20px",
                                        margin: "5px 0",
                                        backgroundColor: selectedCollections.has(collection.id) ? "#007BFF" : "#fff",
                                        color: selectedCollections.has(collection.id) ? "#fff" : "#000",
                                        border: "1px solid #ccc",
                                        borderRadius: "5px",
                                        cursor: "pointer",
                                        textAlign: "center",
                                        display: "block",
                                    }}
                                >
                                    {collection.name || "Без име"}
                                </button>
                            ))}
                        </div>
                        <hr />
                        <h4>Създай нова колекция</h4>
                        <input
                            type="text"
                            value={newCollectionName}
                            onChange={(e) => setNewCollectionName(e.target.value)}
                            placeholder="Име на колекцията"
                        />
                        <button onClick={createCollection}>Създай</button>
                        <button onClick={toggleModal}>Затвори</button>
                    </div>
                </>
            )}


            <h2>Ревюта</h2>
            <div>
                {reviews.length === 0 ? (
                    <p>Няма намерени ревюта за тази книга.</p>
                ) : (
                    reviews.slice(0, visibleReviews).map((review, index) => (
                        <div key={index} style={{ borderBottom: "1px solid #ccc", marginBottom: "10px" }}>
                            {editingReview === review.id ? ( // Режим на редакция
                                <div>
                                    <textarea
                                        value={editedText}
                                        onChange={(e) => setEditedText(e.target.value)} // Променяме текста
                                        style={{ width: "100%", height: "100px", marginBottom: "10px" }}
                                    />
                                    <div>
                                        {[1, 2, 3, 4, 5].map((star) => (
                                            <button
                                                key={star}
                                                onClick={() => setEditedRating(star)} // Променяме рейтинга
                                                style={{
                                                    padding: "10px",
                                                    marginRight: "5px",
                                                    backgroundColor: editedRating >= star ? "#FFD700" : "#ccc",
                                                    border: "none",
                                                    borderRadius: "5px",
                                                    cursor: "pointer",
                                                }}
                                            >
                                                ★
                                            </button>
                                        ))}
                                    </div>
                                    <button
                                        onClick={submitEditReview} // Изпращаме редакцията
                                        style={{
                                            marginTop: "10px",
                                            padding: "10px 20px",
                                            backgroundColor: "#28a745",
                                            color: "#fff",
                                            border: "none",
                                            borderRadius: "5px",
                                            cursor: "pointer",
                                        }}
                                    >
                                        Запази
                                    </button>
                                    <button
                                        onClick={() => setEditingReview(null)} // Излизане от редакция
                                        style={{
                                            marginTop: "10px",
                                            padding: "10px 20px",
                                            backgroundColor: "#dc3545",
                                            color: "#fff",
                                            border: "none",
                                            borderRadius: "5px",
                                            cursor: "pointer",
                                        }}
                                    >
                                        Отказ
                                    </button>
                                </div>
                            ) : ( // Обикновен режим
                                <div>
                                    <p><strong>Рейтинг:</strong> {review.rating} звезди</p>
                                    <p>{review.text}</p>
                                    <p style={{ fontStyle: "italic", color: "#666" }}>
                                        Написано на: {formatTimestamp(review.timestamp)}
                                    </p>
                                    {localStorage.getItem("userId") === review.userId && ( // Ако ревюто е на текущия потребител
                                        <div>
                                            <button
                                                onClick={() => handleEditReview(review)} // Стартиране на редакция
                                                style={{
                                                    marginRight: "10px",
                                                    padding: "5px 10px",
                                                    backgroundColor: "#007BFF",
                                                    color: "#fff",
                                                    border: "none",
                                                    borderRadius: "5px",
                                                    cursor: "pointer",
                                                }}
                                            >
                                                Редактиране
                                            </button>
                                            <button
                                                onClick={() => handleDeleteReview(review.id)} // Изтриване на ревю
                                                style={{
                                                    padding: "5px 10px",
                                                    backgroundColor: "#dc3545",
                                                    color: "#fff",
                                                    border: "none",
                                                    borderRadius: "5px",
                                                    cursor: "pointer",
                                                }}
                                            >
                                                Изтриване
                                            </button>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>
                    ))
                )}
                {reviews.length > visibleReviews && ( // Бутон "Зареди още"
                    <button
                        onClick={() => setVisibleReviews((prev) => prev + 5)}
                        style={{
                            padding: "10px",
                            backgroundColor: "#007BFF",
                            color: "#fff",
                            border: "none",
                            borderRadius: "5px",
                            cursor: "pointer",
                        }}
                    >
                        Зареди още
                    </button>
                )}
            </div>

        </div>
    );
};

export default BookDetailsPage;
