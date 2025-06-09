import React, { useEffect, useState } from "react";
import "../css/pages/Dashboard.css";
import Header from "../components/global/Header.jsx";
import Footer from "../components/global/Footer.jsx";

const Dashboard = () => {
    const [reservations, setReservations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [cancellingId, setCancellingId] = useState(null);
    const [completingId, setCompletingId] = useState(null);

    const fetchReservations = async () => {
        const user = JSON.parse(localStorage.getItem("client"));
        const userId = user?.id;

        if (!userId) {
            setError("User not logged in.");
            setLoading(false);
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/api/reservations/client/${userId}`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data = await response.json();
            setReservations(data);
        } catch (error) {
            setError("Failed to fetch reservations.");
            console.error("Fetching reservations failed:", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchReservations();
    }, []);

    const handleCancelReservation = async (reservationId) => {
        setCancellingId(reservationId);
        try {
            const response = await fetch(`http://localhost:8080/api/reservations/${reservationId}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Failed to cancel reservation');
            }

            // Refresh the reservations list
            await fetchReservations();
        } catch (error) {
            setError(error.message);
            console.error("Cancelling reservation failed:", error);
        } finally {
            setCancellingId(null);
        }
    };

    const handleCompleteReservation = async (reservationId) => {
        setCompletingId(reservationId);
        try {
            const response = await fetch(`http://localhost:8080/api/reservations/${reservationId}/complete`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Failed to mark reservation as completed');
            }

            // Refresh the reservations list
            await fetchReservations();
        } catch (error) {
            setError(error.message);
            console.error("Marking reservation as completed failed:", error);
        } finally {
            setCompletingId(null);
        }
    };

    const activeReservations = reservations.filter(res => res.status === "ACTIVE");
    const completedReservations = reservations.filter(res => res.status === "COMPLETED");

    const renderReservationItem = (reservation) => (
        <div key={reservation.id} className="reservation-card">
            <div className="card-header">
                <div className="station-info">
                    <h3 className="station-name">{reservation.charger?.station?.name || 'N/A'}</h3>
                    <div className="location-info">
                        <svg className="location-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                            <circle cx="12" cy="10" r="3"/>
                        </svg>
                        <span>{reservation.charger?.station ? `${reservation.charger.station.address}, ${reservation.charger.station.city}` : 'N/A'}</span>
                    </div>
                </div>
                <div className={`status-badge ${reservation.status.toLowerCase()}`}>
                    {reservation.status === "COMPLETED" ? (
                        <svg className="status-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                            <polyline points="22,4 12,14.01 9,11.01"/>
                        </svg>
                    ) : (
                        <svg className="status-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <polygon points="13,2 3,14 12,14 11,22 21,10 12,10 13,2"/>
                        </svg>
                    )}
                    {reservation.status}
                </div>
            </div>

            <div className="card-body">
                <div className="charger-info">
                    <svg className="charger-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <polygon points="13,2 3,14 12,14 11,22 21,10 12,10 13,2"/>
                    </svg>
                    <span>Charger #{reservation.charger?.id || 'N/A'} ({reservation.charger?.chargerType?.replace("_", " ") || 'N/A'})</span>
                </div>

                <div className="time-info">
                    <div className="time-item">
                        <svg className="time-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <circle cx="12" cy="12" r="10"/>
                            <polyline points="12,6 12,12 16,14"/>
                        </svg>
                        <div className="time-details">
                            <span className="time-label">
                                {reservation.status === "COMPLETED" ? "Started" : "Start Time"}
                            </span>
                            <span className="time-value">
                                {new Date(reservation.startTime).toLocaleDateString()} {new Date(reservation.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                            </span>
                        </div>
                    </div>
                    <div className="time-item">
                        <svg className="time-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                            <line x1="16" y1="2" x2="16" y2="6"/>
                            <line x1="8" y1="2" x2="8" y2="6"/>
                            <line x1="3" y1="10" x2="21" y2="10"/>
                        </svg>
                        <div className="time-details">
                            <span className="time-label">
                                {reservation.status === "COMPLETED" ? "Ended" : "Expected End"}
                            </span>
                            <span className="time-value">
                                {new Date(reservation.estimatedEndTime).toLocaleDateString()} {new Date(reservation.estimatedEndTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                            </span>
                        </div>
                    </div>
                </div>

                <div className="cost-info">
                    <svg className="cost-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <circle cx="12" cy="12" r="10"/>
                        <path d="M16 8h-6a2 2 0 1 0 0 4h4a2 2 0 1 1 0 4H8"/>
                        <path d="M12 18V6"/>
                    </svg>
                    <span className="cost-value">{reservation.estimatedCost ? reservation.estimatedCost.toFixed(2) + '€' : 'N/A'}</span>
                </div>
            </div>

            {reservation.status === "ACTIVE" && (
                <div className="card-actions">
                    <button
                        className="complete-button"
                        onClick={() => handleCompleteReservation(reservation.id)}
                        disabled={completingId === reservation.id}
                    >
                        {completingId === reservation.id ? (
                            <>
                                <svg className="spinner" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                    <path d="M21 12a9 9 0 11-6.219-8.56"/>
                                </svg>
                                Completing...
                            </>
                        ) : (
                            <>
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                                    <polyline points="22,4 12,14.01 9,11.01"/>
                                </svg>
                                Complete
                            </>
                        )}
                    </button>
                    <button
                        className="cancel-button"
                        onClick={() => handleCancelReservation(reservation.id)}
                        disabled={cancellingId === reservation.id}
                    >
                        {cancellingId === reservation.id ? (
                            <>
                                <svg className="spinner" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                    <path d="M21 12a9 9 0 11-6.219-8.56"/>
                                </svg>
                                Cancelling...
                            </>
                        ) : (
                            <>
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                    <circle cx="12" cy="12" r="10"/>
                                    <line x1="15" y1="9" x2="9" y2="15"/>
                                    <line x1="9" y1="9" x2="15" y2="15"/>
                                </svg>
                                Cancel
                            </>
                        )}
                    </button>
                </div>
            )}
        </div>
    );

    return (
        <div className="dashboard">
            <Header />
            <main className="dashboard-content">
                <div className="dashboard-header">
                    <h1>Your Dashboard</h1>
                    <p className="dashboard-subtitle">Manage your charging reservations</p>
                </div>

                {loading && (
                    <div className="loading-state">
                        <svg className="spinner" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <path d="M21 12a9 9 0 11-6.219-8.56"/>
                        </svg>
                        <p>Loading reservations...</p>
                    </div>
                )}

                {error && (
                    <div className="error-state">
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <circle cx="12" cy="12" r="10"/>
                            <line x1="15" y1="9" x2="9" y2="15"/>
                            <line x1="9" y1="9" x2="15" y2="15"/>
                        </svg>
                        <p>{error}</p>
                    </div>
                )}

                {!loading && !error && (
                    <>
                        <section className="reservations-section">
                            <div className="section-header">
                                <h2>Active Reservations</h2>
                                <span className="count-badge">{activeReservations.length}</span>
                            </div>
                            {activeReservations.length === 0 ? (
                                <div className="empty-state">
                                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                        <polygon points="13,2 3,14 12,14 11,22 21,10 12,10 13,2"/>
                                    </svg>
                                    <p>You have no active reservations.</p>
                                    <span>Book a charging session to get started!</span>
                                </div>
                            ) : (
                                <div className="reservations-grid">
                                    {activeReservations.map(renderReservationItem)}
                                </div>
                            )}
                        </section>

                        <section className="reservations-section">
                            <div className="section-header">
                                <h2>Completed Reservations</h2>
                                <span className="count-badge">{completedReservations.length}</span>
                            </div>
                            {completedReservations.length === 0 ? (
                                <div className="empty-state">
                                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                                        <polyline points="22,4 12,14.01 9,11.01"/>
                                    </svg>
                                    <p>You have no completed reservations.</p>
                                    <span>Your charging history will appear here.</span>
                                </div>
                            ) : (
                                <div className="reservations-grid">
                                    {completedReservations.map(renderReservationItem)}
                                </div>
                            )}
                        </section>
                    </>
                )}
            </main>
            <Footer />
        </div>
    );
};

export default Dashboard;