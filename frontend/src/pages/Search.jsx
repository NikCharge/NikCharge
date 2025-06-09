import React, { useEffect, useState, useMemo } from "react";
import "../css/pages/Search.css";
import Header from "../components/global/Header.jsx";
import Footer from "../components/global/Footer.jsx";
import FiltersPanel from "../components/SearchPage/FiltersPanel.jsx";
import MapDisplay from "../components/SearchPage/MapDisplay.jsx";
import StationList from "../components/SearchPage/StationList.jsx";
import StationInfoModal from "../components/SearchPage/StationInfoModal.jsx";
import { MdMap, MdList } from "react-icons/md";
import dayjs from "dayjs";

// Distância entre dois pontos geográficos
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

const chargerLabelToEnum = {
    "Fast (DC)": "DC_FAST",
    "Standard (AC)": "AC_STANDARD",
    "Ultra-fast (DC)": "DC_ULTRAFAST"
};

const Search = () => {
    const [stations, setStations] = useState([]);
    const [userLocation, setUserLocation] = useState(null);
    const [viewMode, setViewMode] = useState("map");
    const [selectedChargerTypes, setSelectedChargerTypes] = useState([]);
    const [selectedStation, setSelectedStation] = useState(null);
    const [selectedDateTime, setSelectedDateTime] = useState(() => new Date());

    useEffect(() => {
        const fetchStations = async () => {
            if (!userLocation) return;

            try {
                let stations = [];
                const datetimeParam = selectedDateTime
                    ? `?datetime=${encodeURIComponent(dayjs(selectedDateTime).format("YYYY-MM-DDTHH:mm"))}`
                    : "";

                if (selectedChargerTypes.length > 0) {
                    const dayOfWeek = dayjs(selectedDateTime).day();
                    const hour = dayjs(selectedDateTime).hour();

                    const fetches = selectedChargerTypes.map(async type => {
                        const typeParam = chargerLabelToEnum[type];
                        const url = `/api/stations/search?dayOfWeek=${dayOfWeek}&hour=${hour}&chargerType=${typeParam}`;
                        const res = await fetch(url, { 
                            cache: "no-store",
                            headers: {
                                'Cache-Control': 'no-cache'
                            }
                        });
                        return res.json();
                    });

                    const results = await Promise.all(fetches);
                    const allStations = results.flat();

                    const uniqueStations = Array.from(
                        new Map(allStations.map(station => [station.id, station])).values()
                    );

                    const detailedStations = await Promise.all(
                        uniqueStations.map(async (station) => {
                            const detailsUrl = `/api/stations/${station.id}/details${datetimeParam}`;
                            const detailsRes = await fetch(detailsUrl, { 
                                cache: "no-store",
                                headers: {
                                    'Cache-Control': 'no-cache'
                                }
                            });
                            if (!detailsRes.ok) return null;
                            const details = await detailsRes.json();

                            return {
                                ...details,
                                imageUrl: station.imageUrl || null,
                                distance: calculateDistance(
                                    userLocation.lat,
                                    userLocation.lng,
                                    details.latitude,
                                    details.longitude
                                ),
                                availableChargers: details.chargers.length
                            };
                        })
                    );

                    stations = detailedStations;
                } else {
                    // Fetch all stations if no charger type filter
                    const url = `/api/stations?lat=${userLocation.lat}&lng=${userLocation.lng}${datetimeParam}`;
                    const res = await fetch(url, { 
                        cache: "no-store",
                        headers: {
                            'Cache-Control': 'no-cache'
                        }
                    });
                    const baseStations = await res.json();

                    const detailedStations = await Promise.all(
                        baseStations.map(async (station) => {
                            const detailsRes = await fetch(`/api/stations/${station.id}/details${datetimeParam}`, { 
                                cache: "no-store",
                                headers: {
                                    'Cache-Control': 'no-cache'
                                }
                            });
                            if (!detailsRes.ok) return null;
                            const details = await detailsRes.json();

                            return {
                                ...details,
                                imageUrl: station.imageUrl || null,
                                distance: calculateDistance(
                                    userLocation.lat,
                                    userLocation.lng,
                                    details.latitude,
                                    details.longitude
                                ),
                                availableChargers: Array.isArray(details.chargers) ? details.chargers.length : 0
                            };
                        })
                    );

                    stations = detailedStations;
                }

                const validStations = stations.filter(Boolean);
                setStations(validStations);
            } catch (error) {
                console.error("Error fetching stations:", error);
            }
        };

        fetchStations();
    }, [userLocation, selectedDateTime, selectedChargerTypes]);

    const filteredStations = useMemo(() => {
        return stations;
    }, [stations]);

    const handleStationClick = (station) => {
        setSelectedStation(station);
    };

    return (
        <div className="search">
            <Header />
            <main className="search-main">
                <div className="search-header">
                    <h1>Search for stations</h1>
                    <p>{stations.length} station{stations.length !== 1 && "s"} found</p>

                    <div className="view-toggle">
                        <button
                            className={viewMode === "map" ? "active" : ""}
                            onClick={() => setViewMode("map")}
                        >
                            <MdMap size={20} />
                            <span>Map</span>
                        </button>
                        <button
                            className={viewMode === "list" ? "active" : ""}
                            onClick={() => setViewMode("list")}
                        >
                            <MdList size={20} />
                            <span>List</span>
                        </button>
                    </div>
                </div>

                <FiltersPanel
                    setUserLocation={setUserLocation}
                    userLocation={userLocation}
                    selectedChargerTypes={selectedChargerTypes}
                    setSelectedChargerTypes={setSelectedChargerTypes}
                    selectedDateTime={selectedDateTime}
                    setSelectedDateTime={setSelectedDateTime}
                />

                {viewMode === "map" ? (
                    <MapDisplay
                        stations={filteredStations}
                        userLocation={userLocation}
                        onStationClick={handleStationClick}
                    />
                ) : (
                    <StationList
                        stations={filteredStations}
                        onStationClick={handleStationClick}
                    />
                )}

                {selectedStation && (
                    <StationInfoModal
                        station={selectedStation}
                        onClose={() => setSelectedStation(null)}
                        selectedDateTime={selectedDateTime}
                    />
                )}
            </main>
            <Footer />
        </div>
    );
};

export default Search;
