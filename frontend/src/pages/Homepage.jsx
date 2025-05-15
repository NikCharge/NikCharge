import React from "react";
import "../css/Homepage.css";
import logo from "../assets/logo.png";
import car from "../assets/car.png";

const Homepage = () => {
    return (
        <div className="homepage">
            {/* ✅ HEADER */}
            <header className="header">
                <img src={logo} alt="NikCharge Logo" className="logo" />

                <nav className="nav">
                    <a href="#">Home</a>
                    <a href="#">About</a>
                    <a href="#">Search</a>
                </nav>

                <button className="login-button">Login / Register</button>
            </header>

            {/* ✅ MAIN SECTION */}
            <main className="main-section">
                <div className="text-section">
                    <h1>
                        Your smart <span className="highlight">electric</span><br />
                        <span className="highlight">charging network</span>.
                    </h1>
                </div>
                <div className="image-section">
                    <img src={car} alt="Car at Charging Station" className="car-image" />
                </div>
            </main>

            {/* ✅ FOOTER */}
            <footer className="footer">
                <p>Copyright @ 2025</p>
                <div className="footer-info">
                    <img src={logo} alt="NikCharge Logo" className="footer-logo" />
                    <p>
                        Universidade de Aveiro<br />
                        Departamento de Electrónica, Telecomunicações e Informática<br />
                        Testes de Qualidade de Software<br />
                        Ana Rita Silva, Ângela Ribeiro, Carolina Silva, Hugo Castro
                    </p>
                </div>
            </footer>
        </div>
    );
};

export default Homepage;
