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

    // New state for available chargers modal
    const [showAvailableChargers, setShowAvailableChargers] = useState(false);

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

    const handleMarkAvailable = async (chargerId) => {
        try {
            const response = await fetch(`http://localhost:8080/api/chargers/${chargerId}/status`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ status: 'AVAILABLE' })
            });

            if (!response.ok) {
                const errorBody = await response.json();
                const errorMessage = errorBody.error || `HTTP error! status: ${response.status}`;
                throw new Error(errorMessage);
            }

            // Refresh chargers list for the current station
            if (selectedStation) {
                await fetchChargers(selectedStation.id);
            }

        } catch (err) {
            console.error('Error marking charger as available:', err);
            alert(`Failed to mark charger as available: ${err.message || 'Unknown error'}`);
        }
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
                 <div className="dashboard-stats">
                <div className="stat-item">
                    <div className="stat-icon">
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <path d="M9 11H5a2 2 0 0 0-2 2v3c0 2 1 3 1 3h2.5c0 0 0-1 0-1.5s1-1.5 1-1.5"></path>
                            <path d="M14 13.5c0 0 0 1 0 1.5H16s1-1 1-3v-3a2 2 0 0 0-2-2h-4"></path>
                            <circle cx="12" cy="12" r="3"></circle>
                        </svg>
                    </div>
                    <div className="stat-content">
                        <div className="stat-number">{statistics.totalStations}</div>
                        <div className="stat-label">
                           Total Stations
                        </div>
                    </div>
                </div>
                <div
                    className="stat-item available clickable"
                    onClick={() => setShowAvailableChargers(true)}
                >
                    <div className="stat-icon">
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <path d="M18.36 6.64a9 9 0 1 1-12.73 0"></path>
                            <line x1="12" y1="2" x2="12" y2="12"></line>
                        </svg>
                    </div>
                    <div className="stat-content">
                        <div className="stat-number">{statistics.totalAvailable}</div>
                        <div className="stat-label">Available Chargers</div>
                    </div>
                </div>
                <div className="stat-item in-use">
                    <div className="stat-icon">
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <circle cx="12" cy="12" r="10"></circle>
                            <polyline points="12,6 12,12 16,14"></polyline>
                        </svg>
                    </div>
                    <div className="stat-content">
                        <div className="stat-number">{statistics.totalInUse}</div>
                        <div className="stat-label">In Use Chargers</div>
                    </div>
                </div>
            </div>
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

            {showAvailableChargers && (
                <AvailableChargersModal onClose={() => setShowAvailableChargers(false)} />
            )}

            {showChargerModal && (
                <ChargerModal
                    station={selectedStation}
                    chargers={chargers}
                    loading={chargersLoading}
                    onClose={handleCloseChargerModal}
                    onMarkUnderMaintenance={handleMarkUnderMaintenanceClick}
                    onMarkAvailable={handleMarkAvailable}
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