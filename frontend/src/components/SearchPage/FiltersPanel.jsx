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

const calculateDistance = (lat1, lon1, lat2, lon2) => {
    const toRad = value => (value * Math.PI) / 180;
    const R = 6371;
    const dLat = toRad(lat2 - lat1);
    const dLon = toRad(lon2 - lon1);

    const a = Math.sin(dLat / 2) ** 2 +
        Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
        Math.sin(dLon / 2) ** 2;

    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const d = R * c;

    return d < 1 ? `${Math.round(d * 1000)}m` : `${d.toFixed(1)}km`;
};

const FiltersPanel = ({ userLocation, setUserLocation, selectedChargerTypes, setSelectedChargerTypes, setStations, selectedDateTime, setSelectedDateTime }) => {
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

    const fetchStations = async (lat, lng, datetime = null, chargerTypesOverride = null) => {
        try {
            let stations = [];

            const chargerTypes = chargerTypesOverride || selectedChargerTypes;
            console.log("🚀 Fetching stations with:");
            console.log("Latitude:", lat, "Longitude:", lng);
            console.log("Datetime:", datetime);
            console.log("Charger types:", chargerTypes);

            if (chargerTypes.length > 0 && datetime) {
                const dayOfWeek = dayjs(datetime).day();
                const hour = dayjs(datetime).hour();

                const fetches = chargerTypes.map(async type => {                    
                    const typeParam = chargerLabelToEnum[type];
                    
                    const url = `/api/stations/search?dayOfWeek=${dayOfWeek}&hour=${hour}&chargerType=${typeParam}`;
                    console.log("➡️  Fetching filtered stations from:", url);
                    const res = await fetch(url);
                    return res.json();
                });

                const results = await Promise.all(fetches);
                const allStations = results.flat();
                console.log("📦 Filtered station search results:", allStations);

                const uniqueStations = Array.from(
                    new Map(allStations.map(station => [station.id, station])).values()
                );

                const detailedStations = await Promise.all(
                    uniqueStations.map(async (station) => {
                        const datetimeParam = encodeURIComponent(dayjs(datetime).format("YYYY-MM-DDTHH:mm"));
                        const detailsUrl = `/api/stations/${station.id}/details?datetime=${datetimeParam}`;
                        console.log("🔍 Fetching details from:", detailsUrl);
                        const detailsRes = await fetch(detailsUrl);
                        if (!detailsRes.ok) {
                            console.error("❌ Failed to fetch details for station", station.id);
                            return null;
                        }
                        const details = await detailsRes.json();
                        
                        console.log(`✅ Details for station ${station.id}:`, details);

                        return {
                            ...details,
                            imageUrl: station.imageUrl || null,
                            distance: (userLocation?.lat && userLocation?.lng)
                                ? calculateDistance(
                                    userLocation.lat,
                                    userLocation.lng,
                                    details.latitude,
                                    details.longitude
                                )
                                : "–",
                            availableChargers: details.chargers.length
                        };
                    })
                );

                console.log("🧩 Final detailedStations list:", detailedStations);
                stations = detailedStations;
            } else {
                // fallback: fetch all
                let url = `/api/stations?lat=${lat}&lng=${lng}`;
                if (datetime) {
                    const isoDate = dayjs(datetime).format("YYYY-MM-DDTHH:mm");
                    url += `&datetime=${encodeURIComponent(isoDate)}`;
                }
                console.log("📡 Fallback fetch from:", url);
                const res = await fetch(url);
                const baseStations = await res.json();

                const detailedStations = await Promise.all(
                    baseStations.map(async (station) => {
                        const datetimeParam = datetime
                            ? `?datetime=${encodeURIComponent(dayjs(datetime).format("YYYY-MM-DDTHH:mm"))}`
                            : "";

                        const detailsRes = await fetch(`/api/stations/${station.id}/details${datetimeParam}`);
                        if (!detailsRes.ok) {
                            console.error("❌ Failed to fetch details for station", station.id);
                            return null;
                        }
                        const details = await detailsRes.json();
                        

                        return {
                            ...details,
                            imageUrl: station.imageUrl || null,
                            distance: (userLocation?.lat && userLocation?.lng)
                                ? calculateDistance(
                                    userLocation.lat,
                                    userLocation.lng,
                                    details.latitude,
                                    details.longitude
                                )
                                : "–",
                            availableChargers: Array.isArray(details.chargers) ? details.chargers.length : 0
                        };
                    })
                );

                stations = detailedStations;

            }

            console.log("📊 Setting stations state with:", stations);
            const validStations = stations.filter(Boolean);
            setStations(validStations);

        } catch (err) {
            console.error("❌ Error fetching stations:", err);
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
