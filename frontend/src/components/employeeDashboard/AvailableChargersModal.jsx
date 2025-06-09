import React, { useState, useEffect } from 'react';
import { MapPin, Zap } from 'lucide-react';
import '../../css/pages/EmployeeDashboard.css';
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_APP_API_BASE_URL || "http://localhost:8080";

const AvailableChargersModal = ({ onClose }) => {
    const [availableChargers, setAvailableChargers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                setError(null);

                const response = await axios.get(`/api/chargers/available`);
                setAvailableChargers(response.data);
            } catch (err) {
                console.error('Error fetching available chargers:', err);
                setError(err.response?.data?.message || err.message || 'Failed to fetch available chargers');
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2>Available Chargers</h2>
                    <button className="close-button" onClick={onClose}>
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <line x1="18" y1="6" x2="6" y2="18"></line>
                            <line x1="6" y1="6" x2="18" y2="18"></line>
                        </svg>
                    </button>
                </div>

                <div className="modal-body">
                    {loading ? (
                        <div className="loading-content">
                            <div className="loading-spinner"></div>
                            <p>Loading available chargers...</p>
                        </div>
                    ) : error ? (
                        <div className="error-content">
                            <p className="error-message">{error}</p>
                            <button className="retry-button" onClick={() => window.location.reload()}>
                                Retry
                            </button>
                        </div>
                    ) : availableChargers.length === 0 ? (
                        <div className="empty-chargers">
                            <p>No available chargers found.</p>
                        </div>
                    ) : (
                        <div className="chargers-grid">
                            {availableChargers.map((charger) => {
                                // Access station info directly from charger DTO
                                const stationName = charger.stationName;
                                const stationCity = charger.stationCity;
                                
                                return (
                                    <div key={charger.id} className="charger-card">
                                        <div className="charger-header">
                                            <div className="charger-id">
                                                <Zap size={20} />
                                                <span>Charger #{charger.id}</span>
                                            </div>
                                            <div className="charger-status" style={{ backgroundColor: 'rgba(34, 197, 94, 0.1)', color: '#22c55e' }}>
                                                Available
                                            </div>
                                        </div>
                                        <div className="charger-details">
                                            <div className="charger-info">
                                                <span className="info-label">Type:</span>
                                                <span className="info-value">{charger.chargerType}</span>
                                            </div>
                                            <div className="charger-info">
                                                <span className="info-label">Price:</span>
                                                <span className="info-value">€{charger.pricePerKwh}/kWh</span>
                                            </div>
                                            {/* Display station information */} 
                                            {(stationName || stationCity) && (
                                                <>
                                                    {stationName && (
                                                        <div className="charger-info">
                                                            <span className="info-label">Station:</span>
                                                            <span className="info-value">{stationName}</span>
                                                        </div>
                                                    )}
                                                    {stationCity && (
                                                        <div className="charger-info">
                                                            <span className="info-label">Location:</span>
                                                            <span className="info-value">
                                                                <MapPin size={16} />
                                                                {stationCity}
                                                            </span>
                                                        </div>
                                                    )}
                                                </>
                                            )}
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default AvailableChargersModal; 