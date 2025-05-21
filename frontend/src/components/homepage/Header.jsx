import React from "react";
import logo from "../../assets/logo.png";
import "../../css/Homepage.css";

const Header = () => (
    <header className="header">
        <div className="corner-shape"></div>
        <img src={logo} alt="NikCharge Logo" className="logo animate-fade-in" />

        <nav className="nav">
            <a href="/project2/NikCharge/frontend/public" className="nav-link">Home</a>
            <a href="/about" className="nav-link">About</a>
            <a href="#" className="nav-link">Locations</a>
        </nav>

        <button className="login-button">
            <span className="login-text">Login / Register</span>
        </button>
    </header>
);

export default Header;
