// components/employeeDashboard/DashboardHeader.jsx
import React, { useState } from 'react';
import AvailableChargersModal from './AvailableChargersModal';
import '../../css/pages/EmployeeDashboard.css';

const DashboardHeader = ({
                             stationsCount,
                             availableChargers,
                             inUseChargers,
                             isStationSpecific,
                             stationName,
                             onResetView
                         }) => {
    const [showAvailableChargers, setShowAvailableChargers] = useState(false);

    return (
        <div className="dashboard-header">
            <div className="header-title-section">
                <h1>Employee Dashboard</h1>
                <p>
                    {isStationSpecific
                        ? `Viewing data for ${stationName}`
                        : 'Manage and monitor all charging stations'
                    }
                </p>
                {isStationSpecific && (
                    <button className="reset-view-button" onClick={onResetView}>
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <polyline points="1,4 1,10 7,10"></polyline>
                            <path d="M3.51,15a9,9,0,0,0,2.13,3.09,9,9,0,0,0,13.4,0,9,9,0,0,0,.9-13"></path>
                        </svg>
                        Back to Global View
                    </button>
                )}
            </div>

            <div className="dashboard-stats">
                <div className="stat-item">
                    <div className="stat-icon">
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <path d="M9 11H5a2 2 0 0 0-2 2v3c0 2 1 3 1 3h2.5c0 0 0-1 0-1.5s1-1.5 1-1.5"></path>
                            <path d="M14 13.5c0 0 0 1 0 1.5H16s1-1 1-3v-3a2 2 0 0 0-2-2h-4"></path>
                            <circle cx="12" cy="12" r="3"></circle>
                        </svg>
                    </div>
                    <div className="stat-content">
                        <div className="stat-number">{stationsCount}</div>
                        <div className="stat-label">
                            {isStationSpecific ? 'Selected Station' : 'Total Stations'}
                        </div>
                    </div>
                </div>
                <div 
                    className="stat-item available clickable" 
                    onClick={() => setShowAvailableChargers(true)}
                >
                    <div className="stat-icon">
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <path d="M18.36 6.64a9 9 0 1 1-12.73 0"></path>
                            <line x1="12" y1="2" x2="12" y2="12"></line>
                        </svg>
                    </div>
                    <div className="stat-content">
                        <div className="stat-number">{availableChargers}</div>
                        <div className="stat-label">Available Chargers</div>
                    </div>
                </div>
                <div className="stat-item in-use">
                    <div className="stat-icon">
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <circle cx="12" cy="12" r="10"></circle>
                            <polyline points="12,6 12,12 16,14"></polyline>
                        </svg>
                    </div>
                    <div className="stat-content">
                        <div className="stat-number">{inUseChargers}</div>
                        <div className="stat-label">In Use Chargers</div>
                    </div>
                </div>
            </div>

            {showAvailableChargers && (
                <AvailableChargersModal onClose={() => setShowAvailableChargers(false)} />
            )}
        </div>
    );
};

export default DashboardHeader;