// pages/EmployeeDashboard.jsx
import React, { useState, useEffect } from 'react';
import Header from '../components/global/Header.jsx';
import DashboardHeader from '../components/employeeDashboard/DashboardHeader.jsx';
import StationsGrid from '../components/employeeDashboard/StationsGrid';
import ChargerModal from '../components/employeeDashboard/ChargerModal.jsx';
import MaintenanceNoteModal from '../components/employeeDashboard/MaintenanceNoteModal.jsx';
import Footer from '../components/global/Footer.jsx';
import '../css/pages/EmployeeDashboard.css';

const EmployeeDashboard = () => {
    const [stations, setStations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [statistics, setStatistics] = useState({
        totalStations: 0,
        totalAvailable: 0,
        totalInUse: 0
    });
    const [selectedStation, setSelectedStation] = useState(null);
    const [showChargerModal, setShowChargerModal] = useState(false);
    const [chargers, setChargers] = useState([]);
    const [chargersLoading, setChargersLoading] = useState(false);

    // New state for maintenance note modal
    const [showMaintenanceNoteModal, setShowMaintenanceNoteModal] = useState(false);
    const [chargerIdForMaintenance, setChargerIdForMaintenance] = useState(null);

    // Filter stations based on search term
    const filteredStations = stations.filter(station => {
        const searchLower = searchTerm.toLowerCase();
        return (
            station.name.toLowerCase().includes(searchLower) ||
            station.city.toLowerCase().includes(searchLower)
        );
    });

    const fetchStations = async () => {
        try {
            setLoading(true);
            setError(null);

            const response = await fetch('http://localhost:8080/api/stations');

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            setStations(data);
        } catch (err) {
            setError(err.message || 'Failed to fetch stations');
            console.error('Error fetching stations:', err);
        } finally {
            setLoading(false);
        }
    };

    const fetchStatistics = async () => {
        try {
            const [availableResponse, inUseResponse] = await Promise.all([
                fetch('http://localhost:8080/api/chargers/count/available/total'),
                fetch('http://localhost:8080/api/chargers/count/in_use/total')
            ]);

            if (!availableResponse.ok || !inUseResponse.ok) {
                throw new Error('Failed to fetch statistics');
            }

            const availableCount = await availableResponse.json();
            const inUseCount = await inUseResponse.json();

            setStatistics({
                totalStations: stations.length,
                totalAvailable: availableCount,
                totalInUse: inUseCount
            });
        } catch (err) {
            console.error('Error fetching statistics:', err);
        }
    };

    const fetchChargers = async (stationId) => {
        try {
            setChargersLoading(true);
            const response = await fetch(`http://localhost:8080/api/chargers/station/${stationId}`);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            setChargers(data);
        } catch (err) {
            console.error('Error fetching chargers:', err);
            setChargers([]);
        } finally {
            setChargersLoading(false);
        }
    };

    // This function will now just open the maintenance note modal
    const handleMarkUnderMaintenanceClick = (chargerId) => {
        setChargerIdForMaintenance(chargerId);
        setShowMaintenanceNoteModal(true);
    };

    // This function will handle the API call after submitting the note
    const handleMaintenanceNoteSubmit = async (chargerId, maintenanceNote) => {
        try {
            const response = await fetch(`http://localhost:8080/api/chargers/${chargerId}/status`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ status: 'UNDER_MAINTENANCE', maintenanceNote: maintenanceNote })
            });

            if (!response.ok) {
                 // Attempt to read error message from response body
                const errorBody = await response.json();
                const errorMessage = errorBody.error || `HTTP error! status: ${response.status}`;
                throw new Error(errorMessage);
            }

            // Refresh chargers list for the current station
            if (selectedStation) {
                await fetchChargers(selectedStation.id);
            }
            // No need to refresh overall statistics as under maintenance chargers are still counted in total

        } catch (err) {
            console.error('Error marking charger under maintenance:', err);
            alert(`Failed to mark charger under maintenance: ${err.message || 'Unknown error'}`);
        }
    };

    const handleCloseChargerModal = () => {
        setShowChargerModal(false);
        setSelectedStation(null);
        setChargers([]);
    };

    const handleCloseMaintenanceNoteModal = () => {
        setShowMaintenanceNoteModal(false);
        setChargerIdForMaintenance(null);
    };

    useEffect(() => {
        fetchStations();
    }, []);

    useEffect(() => {
        if (stations.length > 0) {
            fetchStatistics();
        }
    }, [stations]);

    const handleStationClick = async (station) => {
        setSelectedStation(station);
        setShowChargerModal(true);
        await fetchChargers(station.id);
    };

    const handleRetry = () => {
        fetchStations();
    };

    // The delete function is kept but not used in ChargerModal anymore
    const handleDeleteCharger = async (chargerId) => {
        if (window.confirm('Are you sure you want to delete this charger?')) {
            try {
                const response = await fetch(`http://localhost:8080/api/chargers/${chargerId}`, {
                    method: 'DELETE'
                });

                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }

                // Refresh chargers list and statistics
                if (selectedStation) {
                    await fetchChargers(selectedStation.id);
                }
                await fetchStatistics();
            } catch (err) {
                console.error('Error deleting charger:', err);
                alert('Failed to delete charger. Please try again.');
            }
        }
    };

    return (
        <div className="employee-dashboard-page">
            <Header />
            <div className="dashboard-content">
                <DashboardHeader
                    stationsCount={statistics.totalStations}
                    availableChargers={statistics.totalAvailable}
                    inUseChargers={statistics.totalInUse}
                />
                <div className="search-container">
                    <input
                        type="text"
                        className="search-bar"
                        placeholder="Search stations by name or city..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                <StationsGrid
                    stations={filteredStations}
                    loading={loading}
                    error={error}
                    onStationClick={handleStationClick}
                    onRetry={handleRetry}
                />
            </div>
            <Footer />

            {showChargerModal && (
                <ChargerModal
                    station={selectedStation}
                    chargers={chargers}
                    loading={chargersLoading}
                    onClose={handleCloseChargerModal}
                    onMarkUnderMaintenance={handleMarkUnderMaintenanceClick}
                />
            )}

            {showMaintenanceNoteModal && (
                <MaintenanceNoteModal
                    show={showMaintenanceNoteModal}
                    onClose={handleCloseMaintenanceNoteModal}
                    onSubmit={handleMaintenanceNoteSubmit}
                    chargerId={chargerIdForMaintenance}
                />
            )}
        </div>
    );
};

export default EmployeeDashboard;