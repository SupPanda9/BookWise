import React from "react";
import { useNavigate } from "react-router-dom";

const Dashboard = () => {
    const navigate = useNavigate();

    const handleLogout = () => {
        localStorage.removeItem("token"); // Премахваме токена при изход
        navigate("/"); // Пренасочване към началната страница
    };

    return (
        <div style={{ padding: "20px" }}>
            <header
                style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    marginBottom: "20px",
                    borderBottom: "1px solid #ccc",
                    paddingBottom: "10px",
                }}
            >
                <h1>BookWise</h1>
                <nav style={{ display: "flex", gap: "15px" }}>
                    <button
                        onClick={() => navigate("/search")}
                        style={buttonStyle}
                    >
                        Търсене на книги
                    </button>
                    <button
                        onClick={() => navigate("/collections")}
                        style={buttonStyle}
                    >
                        Колекции
                    </button>
                    <button
                        onClick={() => navigate("/profile")}
                        style={buttonStyle}
                    >
                        Настройки на профила
                    </button>
                    <button
                        onClick={() => navigate("/challenges")}
                        style={buttonStyle}
                    >
                        Предизвикателства
                    </button>
                </nav>
                <button
                    onClick={handleLogout}
                    style={{
                        ...buttonStyle,
                        backgroundColor: "#dc3545",
                        color: "#fff",
                    }}
                >
                    Изход
                </button>
            </header>
            <main>
                <h2>Добре дошли в BookWise!</h2>
                <p>Изберете опция от менюто, за да започнете.</p>
            </main>
        </div>
    );
};

const buttonStyle = {
    padding: "10px 15px",
    backgroundColor: "#007BFF",
    color: "#fff",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
};

export default Dashboard;
