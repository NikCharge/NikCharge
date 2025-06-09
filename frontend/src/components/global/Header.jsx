import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import axios from "axios";
import logo from "../../assets/logo.png";
import "../../css/global/Header.css";

const Header = () => {
    const [client, setClient] = useState(null);
    const [showDropdown, setShowDropdown] = useState(false);
    const [editing, setEditing] = useState(false);
    const [editData, setEditData] = useState({});
    const [message, setMessage] = useState("");
    const navigate = useNavigate();
    const [showRoleOptions, setShowRoleOptions] = useState(false);


    useEffect(() => {
        const savedClient = localStorage.getItem("client");
        if (savedClient) {
            const parsedClient = JSON.parse(savedClient);
            setClient(parsedClient);
            setEditData(parsedClient);
        }
    }, []);

    const handleRoleChange = async (newRole) => {
        try {
            await axios.put(`/api/clients/changeRole/${client.id}`, {
                newRole
            });

            // Após mudar role, faz logout automaticamente
            handleLogout();
        } catch (error) {
            console.error("Role update failed:", error);
            setMessage("Failed to change role.");
        }
    };


    const handleLogout = () => {
        localStorage.removeItem("client");
        localStorage.removeItem("role");
        setClient(null);
        navigate("/");
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setEditData({ ...editData, [name]: value });
    };

    const handleSave = async () => {
        try {
            const response = await axios.put(
                `/api/clients/${client.email}`,
                {
                    name: editData.name,
                    email: client.email,
                    batteryCapacityKwh: parseFloat(editData.batteryCapacityKwh),
                    fullRangeKm: parseFloat(editData.fullRangeKm),
                    role:client.role,
                }
            );

            localStorage.setItem("client", JSON.stringify(response.data));
            setClient(response.data);
            setEditData(response.data);
            setEditing(false);
            setMessage("Profile updated!");
        } catch (error) {
            console.error("Update failed:", error);
            setMessage("Update failed.");
        }
    };

    return (
        <header className="header">
            <img src={logo} alt="NikCharge Logo" className="logo" />

            <nav className="nav">
                <Link to="/" className="nav-link">Home</Link>
                <Link to="/about" className="nav-link">About</Link>
                <Link to="/search" className="nav-link">Search</Link>
                {client && (
                    <>
                        {client.role === "CLIENT" && (
                            <Link to="/dashboard" className="nav-link">Dashboard</Link>
                        )}
                        {client.role === "EMPLOYEE" && (
                            <Link to="/employee-dashboard" className="nav-link">Employee Dashboard</Link>
                        )}
                        {client.role === "MANAGER" && (
                            <Link to="/manager-dashboard" className="nav-link">Manager Dashboard</Link>
                        )}
                    </>
                )}

            </nav>


            {client ? (
                <div className="login-button-wrapper">
                    <div
                        className="login-button"
                        onClick={() => {
                            setShowDropdown(!showDropdown);
                            setMessage("");
                        }}
                    >
                        <span className="login-text">
                            {client.name || client.email} ⌄
                        </span>
                    </div>

                    {showDropdown && (
                        <div className="user-dropdown">
                            <h3>Profile Details</h3>
                            {editing ? (
                                <div className="edit-form">
                                    <div className="edit-field">
                                        <label>Name:</label>
                                        <input
                                            name="name"
                                            value={editData.name}
                                            onChange={handleChange}
                                            className="edit-input"
                                        />
                                    </div>
                                    <div className="edit-field">
                                        <label>Email:</label>
                                        <input
                                            name="email"
                                            value={editData.email}
                                            readOnly
                                            disabled
                                            className="edit-input disabled"
                                        />
                                    </div>
                                    <div className="edit-field">
                                        <label>Battery (kWh):</label>
                                        <input
                                            name="batteryCapacityKwh"
                                            value={editData.batteryCapacityKwh}
                                            onChange={handleChange}
                                            type="number"
                                            className="edit-input"
                                        />
                                    </div>
                                    <div className="edit-field">
                                        <label>Range (km):</label>
                                        <input
                                            name="fullRangeKm"
                                            value={editData.fullRangeKm}
                                            onChange={handleChange}
                                            type="number"
                                            className="edit-input"
                                        />
                                    </div>

                                    <button className="edit-btn" onClick={handleSave}>Save</button>
                                    <button className="logout-btn" onClick={() => setEditing(false)}>Cancel</button>
                                </div>
                            ) : (
                                <>
                                    <p>{client.name}</p>
                                    <p className="email">{client.email}</p>
                                    <div className="car-stats">
                                        <span><b>Battery:</b> {client.batteryCapacityKwh ?? "–"} kWh</span>
                                        <span><b>Range:</b> {client.fullRangeKm ?? "–"} km</span>
                                    </div>
                                    <button className="edit-btn" onClick={() => setEditing(true)}>Edit Profile</button>
                                    <div className="role-switch-section">
                                    <button
                                        className="edit-btn"
                                        onClick={() => setShowRoleOptions(!showRoleOptions)}
                                    >
                                        Change Role
                                    </button>

                                    {showRoleOptions && (
                                        <div className="role-options">
                                            {["CLIENT", "EMPLOYEE", "MANAGER"]
                                                .filter((r) => r !== client.role)
                                                .map((roleOption) => (
                                                    <button
                                                        key={roleOption}
                                                        className="edit-btn role-option"
                                                        onClick={() => handleRoleChange(roleOption)}
                                                    >
                                                        Switch to {roleOption}
                                                    </button>
                                                ))}
                                        </div>
                                    )}
                                </div>

                                    <button className="logout-btn" onClick={handleLogout}>Log out</button>
                                    {message && <p className="message">{message}</p>}
                                </>
                            )}
                        </div>
                    )}
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
