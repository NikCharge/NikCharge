import React, { useState } from "react";
import Header from "../components/homepage/Header.jsx";
import Footer from "../components/homepage/Footer.jsx";
import SignUp from "../components/SignUpLogIn/SignUp.jsx";
import "../css/Homepage.css";
import "../css/SignUpLogIn.css";

const SignUpLogin = () => {
    // State to toggle between sign up and login views
    const [isSignUp, setIsSignUp] = useState(true);

    return (
        <div className="homepage">
            <Header />

            <div className={`auth-container ${isSignUp ? "signup-active" : "login-active"}`}>
                {/* This div will switch sides based on the active mode */}
                <div className="auth-welcome-side">
                    <div className="welcome-box">
                        <h1 className="welcome-heading">
                            Welcome back!
                        </h1>
                        <p>
                            {isSignUp
                                ? "To keep connected with us, please login with your personal information."
                                : "Enter your personal details and start your journey with us."}
                        </p>
                        <button
                            className="sign-button"
                            onClick={() => setIsSignUp(!isSignUp)}
                        >
                            {isSignUp ? "SIGN IN" : "SIGN UP"}
                        </button>
                    </div>
                </div>

                <div className="auth-form-side">
                    {/* Conditionally render Sign Up or Login form based on state */}
                    {isSignUp ? (
                        <SignUp />
                    ) : (
                        <div className="login-placeholder">
                            {/* Login component will be added later */}
                            <h1 className="auth-title">Login</h1>
                            <p className="placeholder-text">Login component will be implemented later.</p>
                        </div>
                    )}
                </div>
            </div>

            <Footer />
        </div>
    );
};

export default SignUpLogin;