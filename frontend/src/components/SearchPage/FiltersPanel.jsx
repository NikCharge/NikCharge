import React, { useState, useEffect } from "react";
import "../../css/SearchPage/FiltersPanel.css";
import { MapPin, Calendar } from "lucide-react";
import Datetime from "react-datetime";
import "react-datetime/css/react-datetime.css";
import moment from "moment";
import dayjs from "dayjs";

const chargerLabelToEnum = {
    "Fast (DC)": "DC_FAST",
    "Standard (AC)": "AC_STANDARD",
    "Ultra-fast (DC)": "DC_ULTRAFAST"
};

const FiltersPanel = ({ setUserLocation, selectedChargerTypes, setSelectedChargerTypes, setStations, selectedDateTime, setSelectedDateTime }) => {
    const [locationText, setLocationText] = useState("Detecting location...");
    const [customLocation, setCustomLocation] = useState("");

    const toggleChargerType = (label) => {
        setSelectedChargerTypes(prev => {
            const newTypes = prev.includes(label)
                ? prev.filter(t => t !== label)
                : [...prev, label];

            navigator.geolocation.getCurrentPosition(
                (position) => {
                    fetchStations(position.coords.latitude, position.coords.longitude, selectedDateTime, newTypes);
                },
                (error) => {
                    console.error("Failed to fetch location after charger toggle:", error);
                }
            );

            return newTypes;
        });
    };

    const fetchStations = async (lat, lng, datetime = null) => {
        try {
            let stations = [];

            // Se houver tipos selecionados, faz chamadas ao /search
            if (selectedChargerTypes.length > 0 && datetime) {
                const dayOfWeek = dayjs(datetime).day(); // 0 (Sunday) to 6 (Saturday)
                const hour = dayjs(datetime).hour();

                const fetches = selectedChargerTypes.map(async type => {
                    const typeParam = encodeURIComponent(type.toUpperCase().replace(/[\s\-()]/g, "_"));
                    const res = await fetch(`/api/stations/search?dayOfWeek=${dayOfWeek}&hour=${hour}&chargerType=${typeParam}`);
                    return res.json();
                });

                const results = await Promise.all(fetches);

                // Junta os resultados e remove duplicados por ID
                const allStations = results.flat();
                const uniqueStations = Array.from(
                    new Map(allStations.map(station => [station.id, station])).values()
                );

                stations = uniqueStations;
            } else {
                // fallback: carregar todas as estações (sem filtragem de disponibilidade)
                let url = `/api/stations?lat=${lat}&lng=${lng}`;
                if (datetime) {
                    const isoDate = dayjs(datetime).format("YYYY-MM-DDTHH:mm");
                    url += `&datetime=${encodeURIComponent(isoDate)}`;
                }
                const res = await fetch(url);
                stations = await res.json();
            }

            setStations(stations);
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

    const handleDateChange = (newValue) => {
        const date = moment(newValue).toDate();
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


    const isValidDate = (current) => {
        return current.isAfter(moment());
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
