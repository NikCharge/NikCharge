import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../components/global/Header";
import Footer from "../components/global/Footer";
import { BarChart3, TrendingUp, Zap, DollarSign, Users, Calendar } from "lucide-react";
import "../css/pages/ManagerDashboard.css";



const dummyData = [
    { stationName: "Aveiro Central", sessions: 120, energyKwh: 3400, revenue: 5100, efficiency: 92, status: "active" },
    { stationName: "Porto Norte", sessions: 85, energyKwh: 2300, revenue: 3900, efficiency: 87, status: "active" },
    { stationName: "Lisboa Sul", sessions: 95, energyKwh: 2800, revenue: 4700, efficiency: 89, status: "maintenance" },
    { stationName: "Coimbra Centro", sessions: 67, energyKwh: 1900, revenue: 3200, efficiency: 91, status: "active" },
];

const ManagerDashboard = () => {
    const navigate = useNavigate();
    const [currentTime, setCurrentTime] = useState("");
    const [selectedPeriod, setSelectedPeriod] = useState("This Week");

    useEffect(() => {
        const role = localStorage.getItem("role");
        if (role !== "MANAGER") {
            navigate("/");
        }

        const now = new Date();
        const options = {
            day: "2-digit",
            month: "short",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit"
        };
        setCurrentTime(now.toLocaleString("en-GB", options));
    }, [navigate]);

    const totals = dummyData.reduce((acc, station) => ({
        sessions: acc.sessions + station.sessions,
        energy: acc.energy + station.energyKwh,
        revenue: acc.revenue + station.revenue
    }), { sessions: 0, energy: 0, revenue: 0 });

    const averageEfficiency = Math.round(
        dummyData.reduce((acc, station) => acc + station.efficiency, 0) / dummyData.length
    );

    return (
        <div className="dashboard">
            <Header />
            <main className="dashboard-main">
                <div className="dashboard-header">
                    <div className="header-content">
                        <h1>Station Management Dashboard</h1>
                        <p>Monitor performance and revenue across all charging stations</p>
                    </div>

                    <div className="dashboard-controls">
                        <div className="time-display">
                            <Calendar size={16} />
                            <span>{currentTime}</span>
                        </div>
                        <select
                            value={selectedPeriod}
                            onChange={(e) => setSelectedPeriod(e.target.value)}
                            className="period-select"
                        >
                            <option>This Week</option>
                            <option>This Month</option>
                            <option>Last 30 Days</option>
                            <option>This Quarter</option>
                        </select>
                    </div>
                </div>

                <div className="summary-cards">
                    <div className="summary-card">
                        <Users size={18} className="card-icon sessions" />
                        <div className="card-content">
                            <h3>Total Sessions</h3>
                            <p className="card-value">{totals.sessions}</p>
                            <span className="card-change positive">+12%</span>
                        </div>
                    </div>
                    <div className="summary-card">
                        <Zap size={18} className="card-icon energy" />
                        <div className="card-content">
                            <h3>Energy Delivered</h3>
                            <p className="card-value">{totals.energy.toLocaleString()} kWh</p>
                            <span className="card-change positive">+8%</span>
                        </div>
                    </div>
                    <div className="summary-card">
                        <DollarSign size={18} className="card-icon revenue" />
                        <div className="card-content">
                            <h3>Total Revenue</h3>
                            <p className="card-value">€{totals.revenue.toLocaleString()}</p>
                            <span className="card-change positive">+15%</span>
                        </div>
                    </div>
                    <div className="summary-card">
                        <TrendingUp size={18} className="card-icon efficiency" />
                        <div className="card-content">
                            <h3>Avg Efficiency</h3>
                            <p className="card-value">{averageEfficiency}%</p>
                            <span className="card-change neutral">+2%</span>
                        </div>
                    </div>
                </div>

                <div className="stations-section">
                    <div className="section-header">
                        <h2>Station Performance</h2>
                        <button className="view-btn">
                            <BarChart3 size={16} /> Detailed
                        </button>
                    </div>

                    <div className="stations-table">
                        <div className="table-header">
                            <div className="header-cell station-name">Station</div>
                            <div className="header-cell">Status</div>
                            <div className="header-cell">Sessions</div>
                            <div className="header-cell">Energy</div>
                            <div className="header-cell">Revenue</div>
                            <div className="header-cell">Eff.</div>
                            <div className="header-cell">Actions</div>
                        </div>

                        {dummyData.map((station, idx) => (
                            <div key={idx} className="table-row">
                                <div className="table-cell station-name">{station.stationName}</div>
                                <div className="table-cell">
                                    <span className={`status-badge ${station.status}`}>{station.status}</span>
                                </div>
                                <div className="table-cell metric-compact">
                                    <Users size={12} /> {station.sessions}
                                </div>
                                <div className="table-cell metric-compact">
                                    <Zap size={12} /> {station.energyKwh}
                                </div>
                                <div className="table-cell metric-compact">
                                    <DollarSign size={12} /> €{station.revenue}
                                </div>
                                <div className="table-cell efficiency-compact">{station.efficiency}%</div>
                                <div className="table-cell actions">
                                    <button className="action-btn compact primary">Details</button>
                                    <button className="action-btn compact secondary">Manage</button>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                <div className="discounts-section">
                    <div className="section-header">
                        <h2>Apply Time-Based Discounts</h2>
                        <button className="view-btn">
                            Configure
                        </button>
                    </div>

                    <div className="discounts-table">
                        <div className="discounts-header">
                            <div className="discounts-cell">Station</div>
                            <div className="discounts-cell">Default Price (€/kWh)</div>
                            <div className="discounts-cell">Time Slot</div>
                            <div className="discounts-cell">Discount (%)</div>
                            <div className="discounts-cell">Final Price</div>
                        </div>

                        {dummyData.map((station, index) => (
                            <div key={index} className="discounts-row">
                                <div className="discounts-cell">{station.stationName}</div>
                                <div className="discounts-cell">€0.30</div>
                                <div className="discounts-cell">14:00 – 17:00</div>
                                <div className="discounts-cell">20%</div>
                                <div className="discounts-cell">€0.24</div>
                            </div>
                        ))}
                    </div>
                </div>

            </main>
            <Footer />
        </div>
    );
};

export default ManagerDashboard;
