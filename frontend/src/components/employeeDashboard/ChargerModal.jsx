// components/employeeDashboard/ChargerModal.jsx
import React from 'react';
import '../../css/pages/EmployeeDashboard.css';

const ChargerModal = ({ station, chargers, loading, onClose, onDeleteCharger }) => {
    const getStatusColor = (status) => {
        switch (status) {
            case 'AVAILABLE':
                return '#22c55e';
            case 'IN_USE':
                return '#f59e0b';
            case 'MAINTENANCE':
                return '#ef4444';
            default:
                return '#6b7280';
        }
    };

    const getStatusLabel = (status) => {
        switch (status) {
            case 'AVAILABLE':
                return 'Available';
            case 'IN_USE':
                return 'In Use';
            case 'MAINTENANCE':
                return 'Maintenance';
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
            case 'DC_ULTRA':
                return 'DC Ultra';
            default:
                return type;
        }
    };

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

                    <h3>Chargers ({chargers.length})</h3>

                    {loading ? (
                        <div className="loading-content">
                            <div className="loading-spinner"></div>
                            <p>Loading chargers...</p>
                        </div>
                    ) : chargers.length === 0 ? (
                        <div className="empty-chargers">
                            <p>No chargers found for this station.</p>
                        </div>
                    ) : (
                        <div className="chargers-grid">
                            {chargers.map((charger) => (
                                <div key={charger.id} className="charger-card">
                                    <div className="charger-header">
                                        <div className="charger-id">Charger #{charger.id}</div>
                                        <div
                                            className="charger-status"
                                            style={{
                                                backgroundColor: `${getStatusColor(charger.status)}20`,
                                                color: getStatusColor(charger.status)
                                            }}
                                        >
                                            <div
                                                className="status-dot"
                                                style={{ backgroundColor: getStatusColor(charger.status) }}
                                            ></div>
                                            {getStatusLabel(charger.status)}
                                        </div>
                                    </div>

                                    <div className="charger-details">
                                        <div className="charger-info">
                                            <span className="info-label">Type:</span>
                                            <span className="info-value">{getChargerTypeLabel(charger.chargerType)}</span>
                                        </div>
                                        <div className="charger-info">
                                            <span className="info-label">Price per kWh:</span>
                                            <span className="info-value">€{charger.pricePerKwh}</span>
                                        </div>
                                        {charger.lastMaintenance && (
                                            <div className="charger-info">
                                                <span className="info-label">Last Maintenance:</span>
                                                <span className="info-value">
                                                    {new Date(charger.lastMaintenance).toLocaleDateString()}
                                                </span>
                                            </div>
                                        )}
                                        {charger.maintenanceNote && (
                                            <div className="charger-info">
                                                <span className="info-label">Note:</span>
                                                <span className="info-value">{charger.maintenanceNote}</span>
                                            </div>
                                        )}
                                    </div>

                                    <div className="charger-actions">
                                        <button
                                            className="delete-button"
                                            onClick={() => onDeleteCharger(charger.id)}
                                        >
                                            Delete Charger
                                        </button>
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