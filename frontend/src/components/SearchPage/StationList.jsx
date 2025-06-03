import React, { useMemo } from "react";
import "../../css/SearchPage/StationList.css";
import { MdEvStation, MdSentimentDissatisfied } from "react-icons/md";

const colors = ["#059669", "#2563eb", "#d97706", "#9333ea", "#dc2626"];

// Retorna uma cor baseada no ID da estação (determinístico)
const getColorFromId = (id) => {
    const hash = Array.from(id).reduce((acc, char) => acc + char.charCodeAt(0), 0);
    return colors[hash % colors.length];
};

// Converte string de distância ("300m", "1.2km") para número de metros
const parseDistance = (distanceStr) => {
    if (!distanceStr || distanceStr === "–") return Infinity;
    return distanceStr.includes("km")
        ? parseFloat(distanceStr) * 1000
        : parseInt(distanceStr.replace("m", ""));
};

const StationList = ({ stations, onStationClick }) => {
    const sortedStations = useMemo(() => {
        return [...stations].sort((a, b) =>
            parseDistance(a.distance) - parseDistance(b.distance)
        );
    }, [stations]);

    if (sortedStations.length === 0) {
        return (
            <div className="station-list empty-message">
                <MdSentimentDissatisfied size={64} color="#6b7280" />
                <h2>No stations nearby</h2>
                <p>
                    We couldn't find any charging stations near your location.
                    Try adjusting the filters or checking again later.
                </p>
            </div>
        );
    }

    return (
        <div className="station-list">
            {sortedStations.map((station) => (
                <button
                    className="station-card"
                    key={station.id}
                    onClick={() => onStationClick && onStationClick(station)}
                >
                    <div
                        className="station-status-tag"
                        style={{ backgroundColor: station.availableChargers > 0 ? "#059669" : "#dc2626" }}
                    >
                        {station.availableChargers > 0 ? "Disponível" : "Indisponível"}
                    </div>

                    {station.imageUrl ? (
                        <img
                            src={station.imageUrl}
                            alt={station.name}
                            onError={(e) => (e.target.style.display = "none")}
                        />
                    ) : (
                        <div className="station-icon-fallback">
                            <MdEvStation
                                size={48}
                                color={station.availableChargers > 0 ? getColorFromId(station.id) : "#dc2626"}
                            />

                        </div>
                    )}

                    <div className="station-info">
                        <span className="station-distance">{station.distance}</span>
                        <h3>{station.name}</h3>
                        <p>{station.availableChargers} available charging stations</p>
                    </div>
                    {station.discount && (
                        <div className="station-discount-tag">
                            -{station.discount}% OFF
                        </div>
                    )}


                </button>
            ))}
        </div>
    );
};

export default StationList;
