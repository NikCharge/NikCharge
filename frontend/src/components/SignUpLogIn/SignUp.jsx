import React, { useState } from "react";
import axios from "axios";
import {
    FaUser,
    FaEnvelope,
    FaLock,
    FaCarBattery,
    FaRoad,
    FaEye,
    FaEyeSlash,
} from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import "../../css/SignUpLogIn.css";

const SignUp = () => {
    const [formData, setFormData] = useState({
        name: "",
        email: "",
        password: "",
        batteryCapacity: "",
        fullRange: "",
    });

    const [showPassword, setShowPassword] = useState(false);
    const [message, setMessage] = useState(null);
    const [messageType, setMessageType] = useState(""); // "success" or "error"
    const navigate = useNavigate();

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        const form = e.target;
        if (!form.checkValidity()) {
            form.reportValidity();
            return;
        }

        if (formData.password.length < 8) {
            setMessage("Password must be at least 8 characters.");
            return;
        }

        try {
            const payload = {
                name: formData.name.trim(),
                email: formData.email.trim(),
                password: formData.password,
                batteryCapacityKwh: parseFloat(formData.batteryCapacity),
                fullRangeKm: parseFloat(formData.fullRange),
            };

            const response = await axios.post("/api/clients/signup", payload);
            setMessage("Account created successfully!");
            setMessageType("success");
            console.log(response.data);
        } catch (error) {
            console.error("Signup error:", error);
            let errorMsg = "Failed to create account. Please try again.";

            if (error.response && error.response.data) {
                const data = error.response.data;

                if (typeof data === "string") {
                    errorMsg = data;
                } else if (typeof data === "object") {
                    errorMsg = Object.entries(data)
                        .map(([field, msg]) => `${field}: ${msg}`)
                        .join(" | ");
                }
            }

            setMessage(errorMsg);
            setMessageType("error");
        }
    };

    return (
        <div className="auth-form-container">
            <h1 className="auth-title">Create Account</h1>

            <form onSubmit={handleSubmit} className="auth-form">
                <div className="form-input-wrapper icon-wrapper">
                    <FaUser className="input-icon" />
                    <input
                        type="text"
                        name="name"
                        value={formData.name}
                        onChange={handleChange}
                        placeholder="Name"
                        className="form-input leaf-input"
                        required
                    />
                </div>

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
                        placeholder="Password (min. 8 characters)"
                        className="form-input leaf-input"
                        required
                        minLength={8}
                    />
                    <span
                        className="password-toggle-icon"
                        onClick={() => setShowPassword(!showPassword)}
                    >
                        {showPassword ? <FaEyeSlash /> : <FaEye />}
                    </span>
                </div>

                <h3 className="vehicle-info-title">Vehicle Information</h3>

                <div className="form-row">
                    <div className="form-input-wrapper icon-wrapper half-width wide-battery">
                        <FaCarBattery className="input-icon" />
                        <input
                            type="number"
                            name="batteryCapacity"
                            value={formData.batteryCapacity}
                            onChange={handleChange}
                            placeholder="Battery capacity (kWh)"
                            className="form-input leaf-input"
                            required
                        />
                    </div>

                    <div className="form-input-wrapper icon-wrapper half-width">
                        <FaRoad className="input-icon" />
                        <input
                            type="number"
                            name="fullRange"
                            value={formData.fullRange}
                            onChange={handleChange}
                            placeholder="Full range (km)"
                            className="form-input leaf-input"
                            required
                        />
                    </div>
                </div>

                <button type="submit" className="sign-button signup-btn">
                    SIGN UP
                </button>

                {message && (
                    <p
                        className={`signup-message ${messageType === "success" ? "success-message" : "error-message"
                            }`}
                    >
                        {message}
                    </p>
                )}
            </form>
        </div>
    );
};

export default SignUp;
