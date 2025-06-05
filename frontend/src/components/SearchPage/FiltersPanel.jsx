import React, { useState, useEffect } from "react";
import "../../css/SearchPage/FiltersPanel.css";
import { MapPin, Calendar, ChevronDown } from "lucide-react";

const FiltersPanel = ({ setUserLocation, selectedChargerTypes, setSelectedChargerTypes }) => {
    const [locationText, setLocationText] = useState("Detecting location...");
    const [currentTime, setCurrentTime] = useState("");
    const [customLocation, setCustomLocation] = useState("");

    const toggleChargerType = (type) => {
        setSelectedChargerTypes(prev =>
            prev.includes(type)
                ? prev.filter(t => t !== type)
                : [...prev, type]
        );
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
        navigator.geolocation.getCurrentPosition(
            (position) => {
                const { latitude, longitude } = position.coords;
                setUserLocation({ lat: latitude, lng: longitude });
                setLocationText(`GPS: Lat ${latitude.toFixed(4)}, Lng ${longitude.toFixed(4)}`);
            },
            (error) => {
                console.error("Geolocation error:", error);
                setLocationText("Location unavailable");
            }
        );
    };

    useEffect(() => {
        resetToGPS();

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
                <label>Location</label>
                <div className="filter-input">
                    <MapPin size={16} />
                    <input id="location-input"
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
                    <button
                        onClick={() => toggleChargerType('Standard (AC)')}
                        className={`charger-btn ${selectedChargerTypes.includes('Standard (AC)') ? 'active' : ''}`}
                    >
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
