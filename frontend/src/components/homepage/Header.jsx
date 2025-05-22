import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import logo from "../../assets/logo.png";
import "../../css/Homepage.css";

const Header = () => {
    const [client, setClient] = useState(null);

    useEffect(() => {
        const savedClient = localStorage.getItem("client");
        if (savedClient) {
            setClient(JSON.parse(savedClient));
        }
    }, []);

    return (
        <header className="header">
            <div className="corner-shape"></div>
            <img src={logo} alt="NikCharge Logo" className="logo animate-fade-in" />

            <nav className="nav">
                <Link to="/" className="nav-link">Home</Link>
                <Link to="/about" className="nav-link">About</Link>
                <Link to="/search" className="nav-link">Search</Link>
                {client && <Link to="/dashboard" className="nav-link">Dashboard</Link>}
            </nav>

            {client ? (
                <div className="login-button" style={{ backgroundColor: "#0e6355" }}>
        <span className="login-text">
            {client.name || client.email} <span style={{ fontSize: "0.8em" }}>⌄</span>
        </span>
                </div>
            ) : (
                <Link to="/signup" className="login-button">
                    <span className="login-text">Login / Register</span>
                </Link>
            )}
        </header>
    );
};

export default Header;
