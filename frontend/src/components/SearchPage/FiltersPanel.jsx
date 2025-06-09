import React, { useState, useEffect } from "react";
import "../../css/SearchPage/FiltersPanel.css";
import { MapPin, Calendar } from "lucide-react";
import Datetime from "react-datetime";
import "react-datetime/css/react-datetime.css";
import moment from "moment";

const chargerLabelToEnum = {
    "Fast (DC)": "DC_FAST",
    "Standard (AC)": "AC_STANDARD",
    "Ultra-fast (DC)": "DC_ULTRAFAST"
};

const FiltersPanel = ({ userLocation, setUserLocation, selectedChargerTypes, setSelectedChargerTypes, selectedDateTime, setSelectedDateTime }) => {
    const [locationText, setLocationText] = useState("Detecting location...");
    const [customLocation, setCustomLocation] = useState("");

    const toggleChargerType = (label) => {
        setSelectedChargerTypes(prev => {
            const newTypes = prev.includes(label)
                ? prev.filter(t => t !== label)
                : [...prev, label];
            return newTypes;
        });
    };

    const handleLocationSearch = async () => {
        if (!customLocation.trim()) return;

        try {
            const res = await fetch(
                `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(customLocation)}&format=json&limit=1`
            );
            const data = await res.json();

            if (data.length === 0) {
                setLocationText("Location not found.");
                return;
            }

            const lat = parseFloat(data[0].lat);
            const lng = parseFloat(data[0].lon);

            setUserLocation({ lat, lng });
            setLocationText(`Manual: ${customLocation} (${lat.toFixed(4)}, ${lng.toFixed(4)})`);
        } catch (error) {
            console.error("Geocoding error:", error);
            setLocationText("Failed to retrieve location.");
        }
    };

    const resetToGPS = () => {
        if (!navigator.geolocation) {
            setLocationText("Geolocation is not supported by your browser");
            return;
        }

        setLocationText("Detecting location...");
        navigator.geolocation.getCurrentPosition(
            (position) => {
                const { latitude, longitude } = position.coords;
                setUserLocation({ lat: latitude, lng: longitude });
                setLocationText(`GPS: Lat ${latitude.toFixed(4)}, Lng ${longitude.toFixed(4)}`);
            },
            (error) => {
                console.error("Geolocation error:", error);
                let errorMessage = "Location unavailable";
                switch (error.code) {
                    case error.PERMISSION_DENIED:
                        errorMessage = "Please allow location access to see distances";
                        break;
                    case error.POSITION_UNAVAILABLE:
                        errorMessage = "Location information is unavailable";
                        break;
                    case error.TIMEOUT:
                        errorMessage = "Location request timed out";
                        break;
                }
                setLocationText(errorMessage);
            },
            {
                enableHighAccuracy: true,
                timeout: 5000,
                maximumAge: 0
            }
        );
    };

    useEffect(() => {
        resetToGPS();
    }, []);

    const handleDateChange = (newValue) => {
        if (!newValue) {
            setSelectedDateTime(new Date());
            return;
        }

        // If it's a new date selection, set time to midnight
        const date = moment(newValue).toDate();
        if (!selectedDateTime || date.getDate() !== selectedDateTime.getDate()) {
            date.setHours(0, 0, 0, 0);
        }
        
        setSelectedDateTime(date);
    };

    const isValidDate = (current) => {
        const now = moment();
        // Allow today's date and future dates
        return current.isSameOrAfter(now, 'day');
    };

    return (
        <div className="filters-panel">
            <div className="filter-section location-filter">
                <label>Location</label>
                <div className="filter-input">
                    <MapPin size={16} />
                    <input
                        id="location-input"
                        type="text"
                        placeholder="Enter a location (e.g., Aveiro)"
                        value={customLocation}
                        onChange={(e) => setCustomLocation(e.target.value)}
                    />
                    <button onClick={handleLocationSearch} id="search-button">Search</button>
                    <button onClick={resetToGPS} id="gps-button">Current location</button>
                </div>
                <span className="location-status">{locationText}</span>
            </div>

            <div className="filter-section date-filter">
                <label>Date and Time</label>
                <div className="filter-input date-picker-wrapper">
                    <Calendar id="date-icon" size={16} />
                    <Datetime
                        value={selectedDateTime}
                        onChange={handleDateChange}
                        inputProps={{
                            placeholder: "Select date and time",
                            className: "date-picker",
                            id: "date-picker-input"
                        }}
                        isValidDate={isValidDate}
                    />
                    {selectedDateTime && (
                        <button 
                            onClick={() => handleDateChange(null)}
                            className="clear-date-button"
                            title="Reset to current date and time"
                        >
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                <line x1="18" y1="6" x2="6" y2="18"></line>
                                <line x1="6" y1="6" x2="18" y2="18"></line>
                            </svg>
                        </button>
                    )}
                </div>
            </div>

            <div className="filter-section charger-types">
                <label>Charger Type</label>
                <div className="charger-buttons">
                    {Object.keys(chargerLabelToEnum).map(label => (
                        <button
                            key={label}
                            onClick={() => toggleChargerType(label)}
                            className={`charger-btn ${selectedChargerTypes.includes(label) ? 'active' : ''}`}
                        >
                            {label}
                        </button>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default FiltersPanel;
