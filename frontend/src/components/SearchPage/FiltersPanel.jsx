import React, { useState, useEffect } from "react";
import "../../css/SearchPage/FiltersPanel.css";
import { MapPin, Calendar, ChevronDown } from "lucide-react";

const FiltersPanel = ({ setUserLocation }) => {
    const [selectedChargerTypes, setSelectedChargerTypes] = useState(['Fast (DC)', 'Ultra-fast (DC)']);
    const [locationText, setLocationText] = useState("Detecting location...");
    const [currentTime, setCurrentTime] = useState("");

    const toggleChargerType = (type) => {
        setSelectedChargerTypes(prev =>
            prev.includes(type)
                ? prev.filter(t => t !== type)
                : [...prev, type]
        );
    };

    useEffect(() => {
        navigator.geolocation.getCurrentPosition(
            (position) => {
                const { latitude, longitude } = position.coords;
                setLocationText(`Lat: ${latitude.toFixed(4)}, Lng: ${longitude.toFixed(4)}`);
                setUserLocation({ lat: latitude, lng: longitude });
            },
            (error) => {
                console.error("Geolocation error:", error);
                setLocationText("Location unavailable");
            }
        );

        const now = new Date();
        const options = {
            day: "2-digit",
            month: "short",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit"
        };
        setCurrentTime(now.toLocaleString("en-GB", options));
    }, [setUserLocation]);

    return (
        <div className="filters-panel">
            <div className="filter-section location-filter">
                <label>Location (current GPS)</label>
                <div className="filter-input active">
                    <MapPin size={16} />
                    <span>{locationText}</span>
                    <ChevronDown size={16} />
                </div>
            </div>

            <div className="filter-section date-filter">
                <label>Date and time (now)</label>
                <div className="filter-input">
                    <Calendar size={16} />
                    <span>{currentTime}</span>
                    <ChevronDown size={16} />
                </div>
            </div>

            <div className="filter-section charger-types">
                <label>Charger Type</label>
                <div className="charger-buttons">
                    <button
                        onClick={() => toggleChargerType('Fast (DC)')}
                        className={`charger-btn ${selectedChargerTypes.includes('Fast (DC)') ? 'active' : ''}`}
                    >
                        Fast (DC)
                    </button>
                    <button className="charger-btn disabled" disabled>
                        Standard (AC)
                    </button>
                    <button
                        onClick={() => toggleChargerType('Ultra-fast (DC)')}
                        className={`charger-btn ${selectedChargerTypes.includes('Ultra-fast (DC)') ? 'active' : ''}`}
                    >
                        Ultra-fast (DC)
                    </button>
                </div>
            </div>
        </div>
    );
};

export default FiltersPanel;
