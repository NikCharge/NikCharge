// components/employeeDashboard/DashboardHeader.jsx
import React from 'react';
import '../../css/pages/EmployeeDashboard.css';

const DashboardHeader = ({ stationsCount, availableChargers, inUseChargers }) => {
    return (
        <div className="dashboard-header">
            <h1>Employee Dashboard</h1>
            <p>Manage and monitor all charging stations</p>

            <div className="dashboard-stats">
                <div className="stat-item">
                    <div className="stat-number">{stationsCount}</div>
                    <div className="stat-label">Total Stations</div>
                </div>
                <div className="stat-item">
                    <div className="stat-number">{availableChargers}</div>
                    <div className="stat-label">Available Chargers</div>
                </div>
                <div className="stat-item">
                    <div className="stat-number">{inUseChargers}</div>
                    <div className="stat-label">In Use Chargers</div>
                </div>
            </div>
        </div>
    );
};

export default DashboardHeader;