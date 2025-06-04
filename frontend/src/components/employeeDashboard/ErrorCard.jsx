// components/dashboard/ErrorCard.jsx
import React from 'react';
import '../../css/pages/EmployeeDashboard.css';

const ErrorCard = ({ message, onRetry }) => (
    <div className="station-card error">
        <div className="error-content">
            <span className="error-message">{message}</span>
            <button className="retry-button" onClick={onRetry}>
                Retry
            </button>
        </div>
    </div>
);

export default ErrorCard;