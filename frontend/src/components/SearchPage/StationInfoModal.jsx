import React, { useState, useMemo } from "react";
import "../../css/SearchPage/StationInfoModal.css";

const StationInfoModal = ({ station, onClose }) => {
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
    const energyNeeded = useMemo(() => ((100 - battery) * 0.4).toFixed(2), [battery]);
    const durationMin = useMemo(() => powerKw ? Math.ceil((energyNeeded / powerKw) * 60) : 0, [energyNeeded, powerKw]);
    const cost = useMemo(() => selectedCharger ? (energyNeeded * selectedCharger.pricePerKwh).toFixed(2) : "0.00", [energyNeeded, selectedCharger]);

    const now = new Date();
    const startTime = now.toISOString();
    const estimatedEndTime = new Date(now.getTime() + durationMin * 60000).toISOString();
    const startTimeDisplay = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    const endTimeDisplay = new Date(now.getTime() + durationMin * 60000).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    const today = now.toLocaleDateString("en-GB", { day: '2-digit', month: 'short', year: 'numeric' });

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
            const res = await fetch("http://localhost:8080/api/reservations", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(body)
            });

            if (!res.ok) {
                let detailedErrorMessage = `Request failed with status ${res.status}.`;
                try {
                    const errorData = await res.json();
                    detailedErrorMessage = errorData.error || errorData.message || JSON.stringify(errorData);
                } catch (jsonError) {
                    try {
                        const textError = await res.text();
                        if (textError) {
                            detailedErrorMessage = `Server returned: ${textError}`;
                        } else {
                            detailedErrorMessage = `Request failed with status ${res.status}, empty response body.`;
                        }
                    } catch (textReadError) {
                        detailedErrorMessage = `Could not read error response from server (Status: ${res.status}).`;
                    }
                }
                setError(detailedErrorMessage);
                setIsSubmitting(false);
                return; // Stop processing on error
            }

            setSuccess(true);
            setTimeout(() => {
                setSuccess(false);
                onClose();
            }, 1500);
        } catch (err) {
            setError(err.message || "Could not complete reservation. Please try again.");
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
                        <p><strong>Date:</strong> {today}</p>
                        <p><strong>Start time:</strong> {startTimeDisplay}</p>
                        <p><strong>Expected end time:</strong> {endTimeDisplay}</p>
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
