// components/dashboard/StationCard.jsx
import React from 'react';
import { MapPin, Zap, Users } from 'lucide-react';
import '../../css/pages/EmployeeDashboard.css';

const StationCard = ({ station, onClick }) => {
    return (
        <div
            className="station-card"
            onClick={() => onClick(station)}
        >
            <div className="station-card-header">
                <div className="station-icon">
                    <Zap size={24} />
                </div>
                <div className="station-status online">
                    <div className="status-dot"></div>
                    <span>Online</span>
                </div>
            </div>

            <div className="station-info">
                <h3 className="station-name">{station.name}</h3>
                <div className="station-location">
                    <MapPin size={16} />
                    <span>{station.city}</span>
                </div>
                <div className="station-coordinates">
                    <span>Lat: {station.latitude?.toFixed(4)}</span>
                    <span>Lng: {station.longitude?.toFixed(4)}</span>
                </div>
            </div>

            <div className="station-footer">
                <div className="station-metric">
                    <Users size={16} />
                    <span>Available</span>
                </div>
            </div>
        </div>
    );
};

export default StationCard;