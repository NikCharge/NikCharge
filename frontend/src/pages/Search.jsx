import React, { useEffect, useState, useMemo } from "react";
import "../css/pages/Search.css";
import Header from "../components/global/Header.jsx";
import Footer from "../components/global/Footer.jsx";
import FiltersPanel from "../components/SearchPage/FiltersPanel.jsx";
import MapDisplay from "../components/SearchPage/MapDisplay.jsx";
import StationList from "../components/SearchPage/StationList.jsx";
import { MdMap, MdList } from "react-icons/md";

// Distância entre dois pontos geográficos
const calculateDistance = (lat1, lon1, lat2, lon2) => {
  const toRad = (value) => (value * Math.PI) / 180;
  const R = 6371;
  const dLat = toRad(lat2 - lat1);
  const dLon = toRad(lon2 - lon1);

  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) ** 2;

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  const d = R * c;

  return d < 1 ? `${Math.round(d * 1000)}m` : `${d.toFixed(1)}km`;
};

const Search = () => {
  const [stations, setStations] = useState([]);
  const [userLocation, setUserLocation] = useState(null);
  const [viewMode, setViewMode] = useState("map");
  const [selectedChargerTypes, setSelectedChargerTypes] = useState([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        // 1. Buscar todos os descontos
        const discountsRes = await fetch("http://localhost:8080/api/discounts");
        const allDiscounts = await discountsRes.json();

        const now = new Date();
        const currentDay = now.getDay();
        const currentHour = now.getHours();

        // Filtrar apenas os descontos válidos agora
        const validDiscounts = allDiscounts.filter(
          (d) =>
            d.active &&
            d.dayOfWeek === currentDay &&
            d.startHour <= currentHour &&
            d.endHour >= currentHour
        );

        // 2. Buscar estações base
        const baseRes = await fetch("http://localhost:8080/api/stations");
        const baseStations = await baseRes.json();

        // 3. Buscar detalhes e combinar com desconto e distância
        const detailedStations = await Promise.all(
          baseStations.map(async (station) => {
            const detailsRes = await fetch(
              `http://localhost:8080/api/stations/${station.id}/details`
            );
            const details = await detailsRes.json();

            let distance = "–";
            if (userLocation?.lat && userLocation?.lng) {
              distance = calculateDistance(
                userLocation.lat,
                userLocation.lng,
                details.latitude,
                details.longitude
              );
            }

            // Verificar descontos aplicáveis a esta estação
            const stationDiscounts = validDiscounts.filter(
              (d) => d.station.id === station.id
            );

            // Verificar se algum tipo de carregador na estação tem desconto
            const chargerTypesWithDiscount = new Set(
              stationDiscounts.map((d) => d.chargerType)
            );

            const matchingDiscounts = details.chargers
              .filter((c) => chargerTypesWithDiscount.has(c.chargerType))
              .map((c) => {
                const d = stationDiscounts.find(
                  (disc) => disc.chargerType === c.chargerType
                );
                return d?.discountPercent || null;
              })
              .filter((val) => val !== null);

            const maxDiscount =
              matchingDiscounts.length > 0
                ? Math.max(...matchingDiscounts)
                : null;

            return {
              ...details,
              imageUrl: station.imageUrl || null,
              distance,
              discount: maxDiscount,
              availableChargers: details.chargers.filter(
                (c) => c.status === "AVAILABLE"
              ).length,
            };
          })
        );

        setStations(detailedStations);
      } catch (error) {
        console.error("Erro ao buscar estações:", error);
      }
    };

    if (userLocation?.lat && userLocation?.lng) {
      fetchData();
    }
  }, [userLocation]);

  const filteredStations = useMemo(() => {
    const typeMap = {
      DC_FAST: "Fast (DC)",
      DC_ULTRA_FAST: "Ultra-fast (DC)",
      AC_STANDARD: "Standard (AC)",
    };

    if (selectedChargerTypes.length === 0) return stations;

    return stations.filter((station) =>
      station.chargers.some((charger) =>
        selectedChargerTypes.includes(typeMap[charger.chargerType])
      )
    );
  }, [stations, selectedChargerTypes]);

  return (
    <div className="search">
      <Header />
      <main className="search-main">
        <div className="search-header">
          <h1>Search for stations</h1>
          <p>
            {stations.length} station{stations.length !== 1 && "s"} found
          </p>

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
          selectedChargerTypes={selectedChargerTypes}
          setSelectedChargerTypes={setSelectedChargerTypes}
        />

        {viewMode === "map" ? (
          <MapDisplay stations={filteredStations} userLocation={userLocation} />
        ) : (
          <StationList
            stations={filteredStations}
            onStationClick={(station) => console.log(station)}
          />
        )}
      </main>
      <Footer />
    </div>
  );
};

export default Search;
