import React from "react";
import "../css/Dashboard.css";
import Header from "../components/global/Header.jsx";
import Footer from "../components/global/Footer.jsx";

const Dashboard = () => {
    return (
        <div className="dashboard">
            <Header />
            <main className="dashboard-content">
                <div className="dashboard-card">
                    <h1>Dashboard</h1>
                    <p>Welcome to your dashboard. Start managing your stations or trips here.</p>
                </div>
            </main>
            <Footer />
        </div>
    );
};

export default Dashboard;
