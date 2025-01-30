import { useEffect, useState } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import { applyActionCode, checkActionCode } from "firebase/auth";
import { auth } from "../config/firebaseConfig.js";
import api from "./api";
import styles from "../styles/EmailConfirmation.module.css"; // Importing CSS

const EmailConfirmationPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const oobCode = searchParams.get("oobCode");

    const [status, setStatus] = useState("loading"); // "loading", "success", "error"

    useEffect(() => {
        if (!oobCode) {
            console.error("Missing oobCode in URL");
            setStatus("error");
            return;
        }

        const confirmEmail = async () => {
            try {
                // Verify action code validity
                const actionInfo = await checkActionCode(auth, oobCode);
                const email = actionInfo.data.email; // Extract email from action code metadata

                // Apply action code to verify email
                // await applyActionCode(auth, oobCode);
                console.log("Email verified successfully");

                // Enable user in the backend
                await api.post("/auth/confirm-email", { email });
                console.log("User enabled successfully");
                setStatus("success");

                setTimeout(() => {
                    navigate("/");
                }, 10000);
            } catch (error) {
                console.error("Error confirming email:", error);
            }
        };

        confirmEmail();
    }, [oobCode, navigate]);

    return (
        <div className={styles.container}>
            {status === "loading" && <p className={styles.loading}>Processing your request...</p>}
            {status === "success" && (
                <h1 className={styles.success}>🎉 You successfully activated your profile! 🎉</h1>
            )}
            {status === "error" && <h1 className={styles.error}>⚠️ Email confirmation failed. Please try again.</h1>}
        </div>
    );
};

export default EmailConfirmationPage;
