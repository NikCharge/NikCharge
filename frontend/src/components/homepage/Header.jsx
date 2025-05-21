import React from "react";
import { Link } from "react-router-dom";
import logo from "../../assets/logo.png";
import "../../css/Homepage.css";

const Header = () => (
    <header className="header">
        <div className="corner-shape"></div>
        <img src={logo} alt="NikCharge Logo" className="logo animate-fade-in" />

        <nav className="nav">
            <Link to="/" className="nav-link">Home</Link>
            <Link to="/about" className="nav-link">About</Link>
            <Link to="#" className="nav-link">Locations</Link>
        </nav>

        <Link to="/signup" className="login-button">
            <span className="login-text">Login / Register</span>
        </Link>
    </header>
);

export default Header;