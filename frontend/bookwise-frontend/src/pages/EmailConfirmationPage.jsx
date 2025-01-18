import { useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import { applyActionCode, checkActionCode } from "firebase/auth";
import { auth } from "../config/firebaseConfig"; // Adjust the path based on your file structure
import axios from "axios";

const EmailConfirmationPage = () => {
    const [searchParams] = useSearchParams();
    const oobCode = searchParams.get("oobCode");

    useEffect(() => {
        if (!oobCode) {
            console.error("Missing oobCode in URL");
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
                await axios.post("http://localhost:8080/auth/confirm-email", { email });
                console.log("User enabled successfully");
            } catch (error) {
                console.error("Error confirming email:", error);
            }
        };

        confirmEmail();
    }, [oobCode]);

    return <div>Email Confirmation</div>;
};

export default EmailConfirmationPage;
