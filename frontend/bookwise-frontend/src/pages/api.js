import axios from "axios";
import { jwtDecode } from "jwt-decode";

const api = axios.create({
    baseURL: "http://localhost:8080",
    withCredentials: true, // ✅ Required for authentication
});

// ✅ Attach JWT token to every request
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("jwtToken");

        if (token) {
            const decoded = jwtDecode(token);
            const currentTime = Date.now() / 1000; // Convert milliseconds to seconds

            if (decoded.exp < currentTime) {
                console.warn("🔴 Token expired. Logging out...");
                localStorage.removeItem("jwtToken");
                localStorage.removeItem("userId");
                sessionStorage.clear();
                window.location.href = "/";
                return Promise.reject(new Error("Token expired"));
            }

            config.headers.Authorization = `Bearer ${token}`;
        } else {
            console.warn("⚠️ No JWT token found in localStorage!");
        }

        return config;
    },
    (error) => Promise.reject(error)
);

// ✅ Handle 401 Unauthorized (Invalid Token)
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            console.warn("🔴 Unauthorized! Token may be expired. Logging out...");
            localStorage.removeItem("jwtToken");
            localStorage.removeItem("userId");
            sessionStorage.clear();
            window.location.href = "/";
        }
        return Promise.reject(error);
    }
);

export default api;