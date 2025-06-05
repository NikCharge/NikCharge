// components/employeeDashboard/ChargerModal.jsx
import React, { useState, useEffect } from 'react';
import '../../css/pages/EmployeeDashboard.css';

const ChargerModal = ({ station, chargers, loading, onClose, onMarkUnderMaintenance, onMarkAvailable }) => {
    const [filterStatus, setFilterStatus] = useState('All'); // State for filter

    const getStatusColor = (status) => {
        switch (status) {
            case 'AVAILABLE':
                return 'rgba(34, 197, 94, 0.1)'; // Use a lighter background color with alpha
            case 'IN_USE':
                return 'rgba(245, 158, 11, 0.1)';
            case 'UNDER_MAINTENANCE':
                return 'rgba(239, 68, 68, 0.1)';
            default:
                return 'rgba(107, 114, 128, 0.1)';
        }
    };

     const getStatusTextColor = (status) => {
        switch (status) {
            case 'AVAILABLE':
                return '#22c55e'; // Darker green text
            case 'IN_USE':
                return '#f59e0b'; // Darker orange text
            case 'UNDER_MAINTENANCE':
                return '#ef4444'; // Darker red text
            default:
                return '#6b7280'; // Darker gray text
        }
    };

    const getStatusLabel = (status) => {
        switch (status) {
            case 'AVAILABLE':
                return 'Available';
            case 'IN_USE':
                return 'In Use';
            case 'UNDER_MAINTENANCE':
                return 'Under Maintenance';
            default:
                return status;
        }
    };

    const getChargerTypeLabel = (type) => {
        switch (type) {
            case 'AC_STANDARD':
                return 'AC Standard';
            case 'AC_FAST':
                return 'AC Fast';
            case 'DC_FAST':
                return 'DC Fast';
            case 'DC_ULTRA_FAST':
                return 'DC Ultra Fast';
            default:
                return type;
        }
    };

    // Filter chargers based on selected status
    const filteredChargers = filterStatus === 'All'
        ? chargers
        : chargers.filter(charger => charger.status === filterStatus);

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2>{station?.name}</h2>
                    <button className="close-button" onClick={onClose}>
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <line x1="18" y1="6" x2="6" y2="18"></line>
                            <line x1="6" y1="6" x2="18" y2="18"></line>
                        </svg>
                    </button>
                </div>

                <div className="modal-body">
                    <div className="station-details">
                        <p><strong>Location:</strong> {station?.city}</p>
                        <p><strong>Coordinates:</strong> {station?.latitude}, {station?.longitude}</p>
                    </div>

                    <div className="chargers-section-header">
                        <h3>Chargers ({filteredChargers.length})</h3>
                        <div className="filter-dropdown">
                            <label htmlFor="status-filter">Filter by status:</label>
                            <select
                                id="status-filter"
                                value={filterStatus}
                                onChange={(e) => setFilterStatus(e.target.value)}
                            >
                                <option value="All">All Statuses</option>
                                <option value="AVAILABLE">Available</option>
                                <option value="IN_USE">In Use</option>
                                <option value="UNDER_MAINTENANCE">Under Maintenance</option>
                                {/* Add other status options if needed */}
                            </select>
                        </div>
                    </div>

                    {loading ? (
                        <div className="loading-content">
                            <div className="loading-spinner"></div>
                            <p>Loading chargers...</p>
                        </div>
                    ) : chargers.length === 0 ? (
                        <div className="empty-chargers">
                            <p>No chargers found for this station.</p>
                        </div>
                    ) : filteredChargers.length === 0 ? (
                        <div className="empty-chargers">
                            <p>No chargers found with status '{getStatusLabel(filterStatus)}'.</p>
                        </div>
                    ) : (
                        <div className="chargers-grid">
                            {filteredChargers.map((charger) => (
                                <div key={charger.id} className="charger-card">
                                    <div className="charger-header">
                                        <div className="charger-id">
                                            <span>Charger #{charger.id}</span>
                                        </div>
                                        <div
                                            className="charger-status"
                                            style={{
                                                backgroundColor: getStatusColor(charger.status),
                                                color: getStatusTextColor(charger.status) // Use contrasting color for text
                                            }}
                                        >
                                            {getStatusLabel(charger.status)}
                                        </div>
                                    </div>
                                    <div className="charger-details">
                                        <div className="charger-info">
                                            <span className="info-label">Type:</span>
                                            <span className="info-value">{getChargerTypeLabel(charger.chargerType)}</span>
                                        </div>
                                        <div className="charger-info">
                                            <span className="info-label">Price:</span>
                                            <span className="info-value">€{charger.pricePerKwh}/kWh</span>
                                        </div>
                                    </div>
                                    <div className="charger-actions">
                                        {/* Mark Under Maintenance button */}
                                        {charger.status !== 'UNDER_MAINTENANCE' && (
                                            <button
                                                className="maintenance-button"
                                                onClick={() => onMarkUnderMaintenance(charger.id)}
                                            >
                                                Mark Under Maintenance
                                            </button>
                                        )}
                                        {/* Mark Available button */}
                                        {charger.status === 'UNDER_MAINTENANCE' && (
                                            <button
                                                className="available-button"
                                                onClick={() => onMarkAvailable(charger.id)}
                                            >
                                                Mark as Available
                                            </button>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ChargerModal;