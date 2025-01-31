import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import api from "./api";
import styles from "../styles/ChallengesPage.module.css";

const ChallengesPage = () => {
    const [challenges, setChallenges] = useState([]);
    const [userChallenge, setUserChallenge] = useState(null);
    const [progress, setProgress] = useState(0);
    const navigate = useNavigate();
    const userId = localStorage.getItem("userId");

    useEffect(() => {
        fetchChallenges();
    }, []);

    const fetchChallenges = async () => {
        try {
            const response = await api.get("/challenges");
            setChallenges(response.data);
        } catch (error) {
            console.error("Error fetching challenges:", error);
        }
    };

    const joinChallenge = async (challengeId) => {
        try {
            await api.post(`/challenges/${challengeId}/join`, { userId });
            setUserChallenge(challenges.find(ch => ch.id === challengeId));
            setProgress(0);
        } catch (error) {
            console.error("Error joining challenge:", error);
        }
    };

    const updateProgress = async () => {
        if (!userChallenge) return;

        try {
            await api.put(`/challenges/${userChallenge.id}/progress`, {
                userId,
                progress: progress + 1, // Increase progress by 1 book
            });
            setProgress(progress + 1);
        } catch (error) {
            console.error("Error updating progress:", error);
        }
    };

    return (
        <div className={styles.pageWrapper}>
            <header className={styles.header}>
                <h2>Reading Challenges</h2>
                <button onClick={() => navigate("/dashboard")} className={styles.backButton}>
                    Back
                </button>
            </header>

            {userChallenge ? (
                <div className={styles.challengeBox}>
                    <h3>{userChallenge.name}</h3>
                    <p>{userChallenge.description}</p>
                    <p><strong>Progress:</strong> {progress} books read</p>
                    <button onClick={updateProgress} className={styles.button}>
                        Mark Book as Read
                    </button>
                </div>
            ) : (
                <div className={styles.challengeList}>
                    <h3>Available Challenges</h3>
                    {challenges.map((challenge) => (
                        <div key={challenge.id} className={styles.challengeCard}>
                            <h4>{challenge.name}</h4>
                            <p>{challenge.description}</p>
                            <button onClick={() => joinChallenge(challenge.id)} className={styles.button}>
                                Join Challenge
                            </button>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default ChallengesPage;
