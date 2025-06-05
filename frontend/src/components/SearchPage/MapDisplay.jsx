import React, { useEffect } from "react";
import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import "../../css/SearchPage/MapDisplay.css";
import L from "leaflet";
import { MdMyLocation } from "react-icons/md";

// Default Leaflet marker
L.Icon.Default.mergeOptions({
    iconRetinaUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
    iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
    shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
});

// Optional: custom blue icon for user
const userIcon = new L.Icon({
    iconUrl: "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-blue.png",
    shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png",
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
});

const stationIcon = new L.Icon({
    iconUrl: "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png",
    shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png",
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
});

const RecenterMap = ({ lat, lng }) => {
    const map = useMap();
    useEffect(() => {
        map.setView([lat, lng]);
    }, [lat, lng, map]);
    return null;
};

const MapDisplay = ({ stations, userLocation, onStationClick }) => {
    return (
        <div className="map-container">
            <MapContainer
                center={[40.633, -8.660]}
                zoom={15}
                scrollWheelZoom={true}
                className="leaflet-map"
            >
                <TileLayer
                    attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                />
                {userLocation && (
                    <>
                        <RecenterMap lat={userLocation.lat} lng={userLocation.lng} />
                        <Marker position={[userLocation.lat, userLocation.lng]} icon={userIcon}>
                            <Popup>
                                You are here<br />
                                Lat: {userLocation.lat.toFixed(4)}<br />
                                Lng: {userLocation.lng.toFixed(4)}
                            </Popup>
                        </Marker>
                    </>
                )}
                {stations.map((station) => (
                    <Marker
                        key={station.id}
                        position={[station.latitude, station.longitude]}
                        icon={stationIcon}
                        eventHandlers={{
                            click: () => onStationClick(station)
                        }}
                    >
                        <Popup>
                            <strong>{station.name}</strong><br />
                            {station.address}, {station.city}<br />
                            {station.availableChargers} available chargers
                        </Popup>
                    </Marker>
                ))}
            </MapContainer>
        </div>
    );
};

export default MapDisplay;
