import React, { useState, useMemo } from "react";
import "../../css/SearchPage/StationInfoModal.css";
import axios from "axios";


const API_BASE_URL = import.meta.env.VITE_APP_API_BASE_URL || "http://localhost:8080";

const StationInfoModal = ({ station, onClose, selectedDateTime }) => {
    const [selectedChargerId, setSelectedChargerId] = useState(null);
    const [battery, setBattery] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [success, setSuccess] = useState(false);
    const [error, setError] = useState(null);

    // Get user from localStorage
    const user = JSON.parse(localStorage.getItem("client"));
    const userId = user?.id;

    if (!station) return null;

    const selectedCharger = station.chargers?.find(c => c.id === selectedChargerId);
    const powerKw = {
        AC_STANDARD: 7.4,
        DC_FAST: 50,
        DC_ULTRA_FAST: 150
    }[selectedCharger?.chargerType] || 0;

    const validBattery = Number.isInteger(battery) && battery >= 0 && battery < 100;
    const energyNeeded = useMemo(() => {
        const batt = parseInt(battery, 10);
        if (isNaN(batt)) {
            return "0.00";
        }
        return ((100 - batt) * 0.4).toFixed(2);
    }, [battery]);
    const durationMin = useMemo(() => powerKw ? Math.ceil((parseFloat(energyNeeded) / powerKw) * 60) : 0, [energyNeeded, powerKw]);
    const cost = useMemo(() => {
        if (!selectedCharger) return "0.00";
        const calculatedCost = energyNeeded * selectedCharger.pricePerKwh;
        return Math.max(0.50, calculatedCost).toFixed(2);
    }, [energyNeeded, selectedCharger]);

    // Format local time manually to avoid UTC conversion
    const formatLocalDateTime = (date) => {
        const year = date.getFullYear();
        const month = (date.getMonth() + 1).toString().padStart(2, '0'); // Months are 0-indexed
        const day = date.getDate().toString().padStart(2, '0');
        const hours = date.getHours().toString().padStart(2, '0');
        const minutes = date.getMinutes().toString().padStart(2, '0');
        const seconds = date.getSeconds().toString().padStart(2, '0');
        return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
    };
    
    const startTime = formatLocalDateTime(selectedDateTime || new Date());
    const estimatedEndTime = formatLocalDateTime(new Date((selectedDateTime || new Date()).getTime() + durationMin * 60000));

    const startTimeDisplay = `${(selectedDateTime || new Date()).toLocaleDateString()} ${(selectedDateTime || new Date()).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;
    const estimatedEndTimeDisplay = `${new Date((selectedDateTime || new Date()).getTime() + durationMin * 60000).toLocaleDateString()} ${new Date((selectedDateTime || new Date()).getTime() + durationMin * 60000).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;

    const handleBook = async () => {
    if (!selectedCharger || !validBattery) return;

    if (!userId) {
        setError("You must be logged in to book a charger. Please log in and try again.");
        return;
    }

    const body = {
        clientId: userId,
        chargerId: selectedCharger.id,
        startTime,
        estimatedEndTime,
        batteryLevelStart: battery,
        estimatedKwh: parseFloat(energyNeeded),
        estimatedCost: parseFloat(cost)
    };

    try {
        setIsSubmitting(true);
        setError(null);

        const res = await axios.post(`${API_BASE_URL}/api/reservations`, body, {
            headers: {
                "Content-Type": "application/json"
            }
        });

        if (res.status !== 200 && res.status !== 201) {
            throw new Error(`Unexpected response code: ${res.status}`);
        }

        setSuccess(true);
        setTimeout(() => {
            setSuccess(false);
            onClose();
        }, 1500);
    } catch (err) {
        const message =
            err.response?.data?.error ||
            err.response?.data?.message ||
            err.message ||
            "Could not complete reservation. Please try again.";
        setError(message);
    } finally {
        setIsSubmitting(false);
    }
};


    return (
        <div className="modal-backdrop">
            <div className="modal">
                <button className="modal-close" onClick={onClose}>×</button>
                <h2>{station.name}</h2>
                <p><strong>Distance:</strong> {station.distance}</p>

                <p><strong>Available Chargers:</strong></p>
                <ul className="charger-list">
                    {station.chargers?.filter(c => c.status === "AVAILABLE").map((c) => (
                        <li
                            key={c.id}
                            onClick={() => setSelectedChargerId(c.id)}
                            className={selectedChargerId === c.id ? "selected" : ""}
                        >
                            #{c.id} — {c.chargerType.replace("_", " ")} — {c.pricePerKwh.toFixed(2)}€/kWh
                        </li>
                    ))}
                </ul>

                <label>Starting battery level:</label>
                <div className="input-with-suffix">
                    <input
                        type="number"
                        min={0}
                        max={100}
                        step={1}
                        value={battery}
                        onChange={(e) => setBattery(parseInt(e.target.value, 10))}
                    />
                    <span>%</span>
                </div>

                {selectedCharger && validBattery && (
                    <>
                        <p><strong>Start Time:</strong> {startTimeDisplay}</p>
                        <p><strong>Expected End Time:</strong> {estimatedEndTimeDisplay}</p>
                        <p><strong>Energy needed:</strong> {energyNeeded} kWh</p>
                        <p><strong>Duration:</strong> {durationMin} min</p>
                        <p><strong>Cost:</strong> {cost}€</p>

                        {error && (
                            <p className="error-message">{error}</p>
                        )}

                        {!userId ? (
                            <p className="auth-warning">You must be logged in to book a charger.</p>
                        ) : (
                            <button 
                                className="book-button" 
                                onClick={handleBook} 
                                disabled={isSubmitting}
                            >
                                {isSubmitting ? "Booking..." : success ? "✔ Booked!" : "BOOK"}
                            </button>
                        )}
                    </>
                )}
            </div>
        </div>
    );
};

export default StationInfoModal;
