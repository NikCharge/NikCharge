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

const Search = () => {
    const [stations, setStations] = useState([]);
    const [userLocation, setUserLocation] = useState(null);
    const [viewMode, setViewMode] = useState("map");
    const [selectedChargerTypes, setSelectedChargerTypes] = useState([]);
    const [selectedStation, setSelectedStation] = useState(null);
    const [selectedDateTime, setSelectedDateTime] = useState(null);

    useEffect(() => {
    const fetchData = async () => {
        try {
            const [baseRes, discountRes] = await Promise.all([
                fetch("http://localhost:8080/api/stations"),
                fetch("http://localhost:8080/api/discounts")
            ]);

            const baseStations = await baseRes.json();
            const allDiscounts = await discountRes.json();

            // Filtrar apenas descontos ativos
            const activeDiscounts = allDiscounts.filter(d => d.active);

            const detailedStations = await Promise.all(
                baseStations.map(async (station) => {
                    const datetimeParam = selectedDateTime
                        ? `?datetime=${encodeURIComponent(dayjs(selectedDateTime).format("YYYY-MM-DDTHH:mm"))}`
                        : "";

                    const detailsRes = await fetch(`http://localhost:8080/api/stations/${station.id}/details${datetimeParam}`);
                    const details = await detailsRes.json();

                    // Match com descontos ativos
                    const stationDiscounts = activeDiscounts.filter(
                        d => d.station.id === station.id
                    );

                    // Pegar o maior desconto da estação (se houver)
                    const maxDiscount = stationDiscounts.length > 0
                        ? Math.max(...stationDiscounts.map(d => d.discountPercent))
                        : null;

                    let distance = "–";
                    if (userLocation?.lat && userLocation?.lng) {
                        distance = calculateDistance(
                            userLocation.lat,
                            userLocation.lng,
                            details.latitude,
                            details.longitude
                        );
                    }

                    return {
                        ...details,
                        imageUrl: station.imageUrl || null,
                        distance,
                        availableChargers: details.chargers.length,
                        discount: maxDiscount // novo campo
                    };
                })
            );

            setStations(detailedStations);
        } catch (error) {
            console.error("Erro ao buscar estações ou descontos:", error);
        }
    };

    if (userLocation?.lat && userLocation?.lng) {
        fetchData();
    }
}, [userLocation, selectedDateTime]);


    const filteredStations = useMemo(() => {
        const typeMap = {
            "DC_FAST": "Fast (DC)",
            "DC_ULTRA_FAST": "Ultra-fast (DC)",
            "AC_STANDARD": "Standard (AC)"
        };

        // Se nenhum filtro foi selecionado, retorna todas as estações
        if (selectedChargerTypes.length === 0) return stations;

        // Caso contrário, aplica o filtro por tipo
        return stations.filter(station =>
            Array.isArray(station.chargers) &&
            station.chargers.some(charger =>
                selectedChargerTypes.includes(typeMap[charger.chargerType])
            )
        );

    }, [stations, selectedChargerTypes]);

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
                    setStations={setStations}
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
