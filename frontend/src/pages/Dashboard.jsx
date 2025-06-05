import React, { useEffect, useState } from "react";
import "../css/pages/Dashboard.css";
import Header from "../components/global/Header.jsx";
import Footer from "../components/global/Footer.jsx";

const Dashboard = () => {
    const [reservations, setReservations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

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
    }, []); // Empty dependency array means this effect runs once on mount

    const handleCancelReservation = async (reservationId) => {
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
        }
    };

    const activeReservations = reservations.filter(res => res.status === "ACTIVE");
    const completedReservations = reservations.filter(res => res.status === "COMPLETED");

    const renderReservationItem = (reservation) => (
        <li key={reservation.id} className="reservation-item">
            <p><strong>Station:</strong> {reservation.charger?.station?.name || 'N/A'}</p>
            <p><strong>Location:</strong> {reservation.charger?.station ? `${reservation.charger.station.address}, ${reservation.charger.station.city}` : 'N/A'}</p>
            <p><strong>Charger:</strong> {reservation.charger ? `#${reservation.charger.id} (${reservation.charger.chargerType.replace("_", " ")})` : 'N/A'}</p>
            <p><strong>Start Time:</strong> {new Date(reservation.startTime).toLocaleDateString()} {new Date(reservation.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</p>
            <p><strong>Expected End Time:</strong> {new Date(reservation.estimatedEndTime).toLocaleDateString()} {new Date(reservation.estimatedEndTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</p>
            <p><strong>Cost:</strong> {reservation.estimatedCost ? reservation.estimatedCost.toFixed(2) + '€' : 'N/A'}</p>
            {reservation.status === "ACTIVE" && (
                <button 
                    className="cancel-button"
                    onClick={() => handleCancelReservation(reservation.id)}
                >
                    Cancel Reservation
                </button>
            )}
        </li>
    );

    return (
        <div className="dashboard">
            <Header />
            <main className="dashboard-content">
                {loading && <p>Loading reservations...</p>}
                {error && <p className="error-message">{error}</p>}

                {!loading && !error && (
                    <>
                        <section className="reservations-section">
                            <h2>Active Reservations</h2>
                            {activeReservations.length === 0 ? (
                                <p>You have no active reservations.</p>
                            ) : (
                                <ul className="reservations-list grid-view">
                                    {activeReservations.map(renderReservationItem)}
                                </ul>
                            )}
                        </section>

                        <section className="reservations-section">
                            <h2>Completed Reservations</h2>
                            {completedReservations.length === 0 ? (
                                <p>You have no completed reservations.</p>
                            ) : (
                                <ul className="reservations-list grid-view">
                                    {completedReservations.map(renderReservationItem)}
                                </ul>
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
