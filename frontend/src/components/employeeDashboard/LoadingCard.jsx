// components/dashboard/LoadingCard.jsx
import React from 'react';
import '../../css/pages/EmployeeDashboard.css';

const LoadingCard = () => (
    <div className="station-card loading">
        <div className="loading-content">
            <div className="loading-spinner"></div>
            <span>Loading stations...</span>
        </div>
    </div>
);

export default LoadingCard;