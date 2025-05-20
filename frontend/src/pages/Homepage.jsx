import React from "react";
import "../css/Homepage.css";
import logo from "../assets/logo.png";
import car from "../assets/car.png";

const Homepage = () => {
    return (
        <div className="homepage">
            {/* Header with subtle animation */}
            <header className="header">
                <div className="corner-shape"></div>
                <img src={logo} alt="NikCharge Logo" className="logo animate-fade-in"/>

                <nav className="nav">
                    <a href="#" className="nav-link">Home</a>
                    <a href="/about" className="nav-link">About</a>
                    <a href="#" className="nav-link">Locations</a>
                </nav>

                <button className="login-button">
                    <span className="login-text">Login / Register</span>
                </button>
            </header>

            {/* Main section with animated elements */}
            <main className="main-section">
                <div className="text-section">
                    <h1 className="headline animate-slide-up">
                        Your smart <span className="highlight">electric</span><br/>
                        <span className="highlight">charging network</span>.
                    </h1>
                    <p className="subheading animate-fade-in">
                        Find the nearest charging station for your EV with our smart app.
                    </p>
                    <div className="cta-buttons">
                        <button className="cta-button primary">Get Started</button>
                        <button className="cta-button secondary">Learn More</button>
                    </div>
                </div>
                <div className="image-section">
                    <div className="background-circle"></div>
                    <img src={car} alt="Car at Charging Station" className="car-image animate-float"/>
                </div>
            </main>

            <div className="white-bar">
                <div className="features-preview">
                    <div className="feature-item">
                        <div className="feature-icon">🔌</div>
                        <h3>Fast Charging</h3>
                        <p>Rapid charging solution for all EVs</p>
                    </div>
                    <div className="feature-item">
                        <div className="feature-icon">🗺️</div>
                        <h3>Smart Location</h3>
                        <p>Find stations wherever you go</p>
                    </div>
                    <div className="feature-item">
                        <div className="feature-icon">💰</div>
                        <h3>Cost Effective</h3>
                        <p>Save money with our innovative app</p>
                    </div>
                </div>
            </div>

            {/* Enhanced footer */}
            <footer className="footer">
                <div className="footer-content">
                    <div className="footer-left">
                        <p className="copyright">© 2025 NikCharge</p>
                        <div className="social-icons">
                            <a href="#" className="social-icon">📱</a>
                            <a href="#" className="social-icon">💻</a>
                            <a href="#" className="social-icon">📧</a>
                        </div>
                    </div>

                    <div className="footer-center">
                        <img src={logo} alt="NikCharge Logo" className="footer-logo"/>
                    </div>

                    <div className="footer-right">
                        <p className="university">Universidade de Aveiro</p>
                        <p className="department">Departamento de Electrónica, Telecomunicações e Informática</p>
                        <p className="course">Testes de Qualidade de Software</p>
                        <p className="authors">Ana Rita Silva, Ângela Ribeiro, Carolina Silva, Hugo Castro</p>
                    </div>
                </div>
            </footer>
        </div>
    );
};

export default Homepage;