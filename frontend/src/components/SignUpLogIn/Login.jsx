import React, { useState } from "react";
import axios from "axios";
import { FaEnvelope, FaLock, FaEye, FaEyeSlash } from "react-icons/fa";
import "../../css/SignUpLogIn.css";

const Login = () => {
    const [formData, setFormData] = useState({ email: "", password: "" });
    const [showPassword, setShowPassword] = useState(false);
    const [message, setMessage] = useState("");
    const [messageType, setMessageType] = useState("");

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post("http://localhost:8080/api/clients/login", formData, {
                withCredentials: true,
            });

            const clientData = {
                email: response.data.email,
                name: response.data.name,
            };

            localStorage.setItem("client", JSON.stringify(clientData));
            setMessage("Login successful!");
            setMessageType("success");

            console.log("Login success:", clientData);

            // ✅ This forces the app to fully reload and update the Header
            window.location.reload();

        } catch (error) {
            console.error("Login failed:", error);
            let errorMsg = "Login failed. Please try again.";
            if (error.response?.data?.error) {
                errorMsg = typeof error.response.data.error === "string"
                    ? error.response.data.error
                    : Object.values(error.response.data.error).join(" | ");
            }
            setMessage(errorMsg);
            setMessageType("error");
        }
    };

    return (
        <div className="auth-form-container">
            <h1 className="auth-title">Log in</h1>
            <form onSubmit={handleSubmit} className="auth-form">
                <div className="form-input-wrapper icon-wrapper">
                    <FaEnvelope className="input-icon" />
                    <input
                        type="email"
                        name="email"
                        value={formData.email}
                        onChange={handleChange}
                        placeholder="Email"
                        className="form-input leaf-input"
                        required
                    />
                </div>

                <div className="form-input-wrapper icon-wrapper password-wrapper">
                    <FaLock className="input-icon" />
                    <input
                        type={showPassword ? "text" : "password"}
                        name="password"
                        value={formData.password}
                        onChange={handleChange}
                        placeholder="Password"
                        className="form-input leaf-input"
                        required
                    />
                    <span className="password-toggle-icon" onClick={() => setShowPassword(!showPassword)}>
                        {showPassword ? <FaEyeSlash /> : <FaEye />}
                    </span>
                </div>

                <button type="submit" className="sign-button signup-btn">SIGN IN</button>

                {message && (
                    <p className={`signup-message ${messageType === "success" ? "success-message" : "error-message"}`}>
                        {message}
                    </p>
                )}
            </form>
        </div>
    );
};

export default Login;
