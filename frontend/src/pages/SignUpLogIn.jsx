import React, { useState } from "react";
import Header from "../components/homepage/Header.jsx";
import Footer from "../components/homepage/Footer.jsx";
import SignUp from "../components/SignUpLogIn/SignUp.jsx";
import Login from "../components/SignUpLogIn/Login.jsx"; // ensure this import exists
import "../css/Homepage.css";
import "../css/SignUpLogIn.css";

const SignUpLogin = () => {
    const [isSignUp, setIsSignUp] = useState(true);

    return (
        <div className="homepage">
            <Header />

            <div className={`auth-container ${isSignUp ? "signup-active" : "login-active"}`}>
                {/* Welcome Panel */}
                <div className="auth-welcome-side">
                    <div className="welcome-box">
                        {isSignUp ? (
                            <>
                                <h1 className="welcome-heading">Welcome back!</h1>
                                <p>To keep connected with us, please login with your personal information.</p>
                                <button
                                    className="sign-button"
                                    onClick={() => setIsSignUp(false)}
                                >
                                    SIGN IN
                                </button>
                            </>
                        ) : (
                            <>
                                <h1 className="welcome-heading">Hi, stranger!</h1>
                                <p>Enter your personal details and start your journey with us.</p>
                                <button
                                    className="sign-button"
                                    onClick={() => setIsSignUp(true)}
                                >
                                    SIGN UP
                                </button>
                            </>
                        )}
                    </div>
                </div>

                {/* Form Side */}
                <div className="auth-form-side">
                    {isSignUp ? <SignUp /> : <Login />}
                </div>
            </div>

            <Footer />
        </div>
    );
};

export default SignUpLogin;
