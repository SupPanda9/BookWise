import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";
import styles from "../styles/BookDetails.module.css";
import "../styles/Modal.css";
import { useNavigate } from "react-router-dom";

const BookDetailsPage = () => {
    const navigate = useNavigate();

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
    const [isRead, setIsRead] = useState(false);

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

                const userResponse = await axios.get(`http://localhost:8080/users/${userId}`);
                setIsRead(userResponse.data.readBooks.includes(googleBooksId));

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

    const toggleReadStatus = async () => {
        const userId = localStorage.getItem("userId");
        if (!userId) {
            alert("Моля, влезте в профила си.");
            return;
        }
    
        try {
            if (isRead) {
                // Unmark book as read
                await axios.delete(`http://localhost:8080/collections/users/${userId}/read`, {
                    data: { bookId: googleBooksId },
                });
                setIsRead(false);
            } else {
                // Mark book as read
                await axios.post(`http://localhost:8080/collections/users/${userId}/read`, {
                    bookId: googleBooksId,
                });
                setIsRead(true);
            }
        } catch (err) {
            console.error("Error updating read status:", err);
            alert("Error updating read status.");
        }
    };

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

                const filteredCollections = response.data.filter(collection => collection.name !== "Read");
    
                setCollections(filteredCollections);
                const selected = filteredCollections
                    .filter((collection) => collection.books.includes(googleBooksId))
                    .map((collection) => collection.id);
                
                setSelectedCollections(new Set(selected));
            } catch (err) {
                console.error("Грешка при зареждане на колекции:", err);
            }
        };
    
        if (showModal) fetchCollections();
    }, [showModal, googleBooksId]);

    const toggleModal = () => {
        setShowModal(!showModal);
    };    
    
    useEffect(() => {
        if (showModal) {
            document.body.classList.add("modal-open");
        } else {
            document.body.classList.remove("modal-open");
        }
    }, [showModal]);

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

    if (loading) return <p>Loading...</p>;
    if (error) return <p style={{ color: "red" }}>{error}</p>;

    return (
        <div className={styles["book-details-container"]}>
            <div className={styles["book-title-container"]}>
                <h1 className={styles["book-title"]}>{book?.title}</h1>
                
                {/* Back Button */}
                <button className={styles.backButton} onClick={() => navigate(-1)}>
                    Back
                </button>
            </div>

            <div className={styles["book-details-content"]}>
                {/* Лява колона */}
                <div className={styles["book-info"]}>
                    <img className={styles["book-cover"]} src={book?.coverImage} alt={book?.title} />
                    <p><strong>Rating:</strong> {reviews.length === 0 ? "Няма достатъчно данни" : `${calculateAverageRating()} / 5`}</p>
                    <p><strong>Authors:</strong> {book?.authors?.join(", ")}</p>
                    <p><strong>Genres:</strong> {book?.genres?.join(", ")}</p>
                    <p><strong>Page count:</strong> {book?.pageCount}</p>

                    <div className={styles["button-container"]}>
                        <button className={`${styles["read-button"]} ${isRead ? "read" : "unread"}`} onClick={toggleReadStatus}>
                            {isRead ? "Remove from Read" : "Mark as Read"}
                        </button>

                        <button className={styles["collection-button"]} onClick={toggleModal}>Add to Collections</button>
                        {showModal && (
                            <>
                                <div className="modal-overlay" onClick={toggleModal}>
                                    <div className="modal" onClick={(e) => e.stopPropagation()}>
                                        <h3>Manage Collections</h3>
                                        <div className="collection-list">
                                            {collections.map((collection) => (
                                                <button key={collection.id} className={`collection-item ${selectedCollections.has(collection.id) ? "selected" : ""}`} onClick={() => toggleCollection(collection.id, !selectedCollections.has(collection.id))}>
                                                    {collection.name || "Без име"}
                                                </button>
                                            ))}
                                        </div>
                                        <hr />
                                        <h4>Create New Collection</h4>
                                        <input className="collection-input" type="text" value={newCollectionName} onChange={(e) => setNewCollectionName(e.target.value)} placeholder="Collection Name" />
                                        <div className="modal-buttons">
                                            <button className="create-collection">Create</button>
                                            <button className="close-modal" onClick={toggleModal}>Close</button>
                                        </div>
                                    </div>
                                </div>
                            </>
                        )}
                    </div>
                </div>
                
                {/* Дясна колона */}
                <div className={styles["book-description"]}>
                    <p><strong>Description:</strong> {book?.description}</p>
                </div>
            </div>

            <h2>Write Review</h2>
            <div className={styles["rating-buttons"]}>
                {[1, 2, 3, 4, 5].map((star) => (
                    <button 
                        key={star} 
                        className={`${styles["star-button"]} ${rating >= star ? styles["selected"] : ""}`} 
                        onClick={() => setRating(star)}
                    >
                        ★
                    </button>
                ))}
            </div>
            <div className={styles["review-input"]}>
                <textarea className={styles["review-textarea"]} placeholder="Write your review..." value={newReview} onChange={(e) => setNewReview(e.target.value)} />
            </div>
            <button className={styles["submit-review"]} onClick={handleAddReview}>Add Review</button>

            <h2>Reviews</h2>
            <div className={styles["review-list"]}>
                {reviews.length === 0 ? (
                    <p>No reviews found for this book. You can leave one :)</p>
                ) : (
                    reviews.slice(0, visibleReviews).map((review) => (
                        <div key={review.id} className={styles["review-card"]}>
                            {editingReview === review.id ? (
                                <div>
                                    <textarea className={styles["edit-textarea"]} value={editedText} onChange={(e) => setEditedText(e.target.value)} />
                                    <div className={styles["edit-rating"]}>
                                        {[1, 2, 3, 4, 5].map((star) => (
                                            <button key={star} className={`${styles["star-button"]} ${editedRating >= star ? styles["selected"] : ""}`} onClick={() => setEditedRating(star)}>★</button>
                                        ))}
                                    </div>
                                    <button className={styles["save-edit"]} onClick={submitEditReview}>Запази</button>
                                    <button className={styles["cancel-edit"]} onClick={() => setEditingReview(null)}>Отказ</button>
                                </div>
                            ) : (
                                <div className={styles["review-content"]}>
                                    <p><strong>Rating:</strong> {review.rating} ★</p>
                                    <p>{review.text}</p>
                                    <p className={styles["review-timestamp"]}>Written on: {formatTimestamp(review.timestamp)}</p>
                                    {localStorage.getItem("userId") === review.userId && (
                                        <div className={styles["review-actions"]}>
                                            <button className={styles["edit-review"]} onClick={() => handleEditReview(review)}>Edit</button>
                                            <button className={styles["delete-review"]} onClick={() => handleDeleteReview(review.id)}>Delete</button>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>
                    ))
                )}
                {reviews.length > visibleReviews && (
                    <button className={styles["load-more"]} onClick={() => setVisibleReviews((prev) => prev + 5)}>Load more</button>
                )}
            </div>
        </div>
    );
};

export default BookDetailsPage;
