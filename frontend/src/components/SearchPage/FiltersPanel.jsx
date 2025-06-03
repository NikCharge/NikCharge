import React, { useState, useEffect } from "react";
import "../../css/SearchPage/FiltersPanel.css";
import { MapPin, Calendar, ChevronDown } from "lucide-react";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";

const FiltersPanel = ({ setUserLocation, selectedChargerTypes, setSelectedChargerTypes, setFilteredStations }) => {
    const [locationText, setLocationText] = useState("Detecting location...");
    const [customLocation, setCustomLocation] = useState("");
    const [selectedDateTime, setSelectedDateTime] = useState(null);

    const toggleChargerType = (type) => {
        setSelectedChargerTypes(prev =>
            prev.includes(type)
                ? prev.filter(t => t !== type)
                : [...prev, type]
        );
    };

    const fetchStations = async (lat, lng, datetime = null) => {
        try {
            let url = `/api/stations?lat=${lat}&lng=${lng}`;
            if (datetime) {
                const isoDate = datetime.toISOString().slice(0, 16); // yyyy-MM-ddTHH:mm
                url += `&datetime=${encodeURIComponent(isoDate)}`;
            }
            const res = await fetch(url);
            const stations = await res.json();
            setFilteredStations(stations);
        } catch (err) {
            console.error("Error fetching stations:", err);
        }
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
            fetchStations(lat, lng, selectedDateTime);
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
                fetchStations(latitude, longitude, null);
                setSelectedDateTime(null);
            },
            (error) => {
                console.error("Geolocation error:", error);
                setLocationText("Location unavailable");
            }
        );
    };

    useEffect(() => {
        resetToGPS();
    }, []);

    const handleDateChange = (date) => {
        setSelectedDateTime(date);
        navigator.geolocation.getCurrentPosition(
            (position) => {
                fetchStations(position.coords.latitude, position.coords.longitude, date);
            },
            (error) => {
                console.error("Failed to fetch location for time filtering:", error);
            }
        );
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
                    <DatePicker
                        popperPlacement="bottom-start"
                        popperClassName="custom-datepicker-popper"
                        selected={selectedDateTime}
                        onChange={handleDateChange}
                        showTimeSelect
                        timeFormat="HH:mm"
                        timeIntervals={15}
                        timeCaption="Time"
                        dateFormat="yyyy-MM-dd HH:mm"
                        placeholderText="Select date and time"
                        className="date-picker"
                        minDate={new Date()}
                        id="date-picker-input"
                    />
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
