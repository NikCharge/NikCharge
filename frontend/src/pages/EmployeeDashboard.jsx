// pages/EmployeeDashboard.jsx
import React, { useState, useEffect } from 'react';
import axios from 'axios';
import Header from '../components/global/Header.jsx';
import DashboardHeader from '../components/employeeDashboard/DashboardHeader.jsx';
import StationsGrid from '../components/employeeDashboard/StationsGrid';
import ChargerModal from '../components/employeeDashboard/ChargerModal.jsx';
import MaintenanceNoteModal from '../components/employeeDashboard/MaintenanceNoteModal.jsx';
import Footer from '../components/global/Footer.jsx';
import AvailableChargersModal from '../components/employeeDashboard/AvailableChargersModal.jsx';
import '../css/pages/EmployeeDashboard.css';

const API_BASE_URL = import.meta.env.VITE_APP_API_BASE_URL || "http://localhost:8080";

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

    const [showMaintenanceNoteModal, setShowMaintenanceNoteModal] = useState(false);
    const [chargerIdForMaintenance, setChargerIdForMaintenance] = useState(null);
    const [showAvailableChargers, setShowAvailableChargers] = useState(false);

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
            const response = await axios.get(`/api/stations`);
            setStations(response.data);
        } catch (err) {
            setError(err.message || 'Failed to fetch stations');
            console.error('Error fetching stations:', err);
        } finally {
            setLoading(false);
        }
    };

    const fetchStatistics = async () => {
        try {
            const [availableRes, inUseRes] = await Promise.all([
                axios.get(`/api/chargers/count/available/total`),
                axios.get(`/api/chargers/count/in_use/total`)
            ]);

            setStatistics({
                totalStations: stations.length,
                totalAvailable: availableRes.data,
                totalInUse: inUseRes.data
            });
        } catch (err) {
            console.error('Error fetching statistics:', err);
        }
    };

    const fetchChargers = async (stationId) => {
        try {
            setChargersLoading(true);
            const response = await axios.get(`/api/chargers/station/${stationId}`);
            setChargers(response.data);
        } catch (err) {
            console.error('Error fetching chargers:', err);
            setChargers([]);
        } finally {
            setChargersLoading(false);
        }
    };

    const handleMarkUnderMaintenanceClick = (chargerId) => {
        setChargerIdForMaintenance(chargerId);
        setShowMaintenanceNoteModal(true);
    };

    const handleMaintenanceNoteSubmit = async (chargerId, maintenanceNote) => {
        try {
            await axios.put(`${API_BASE_URL}/api/chargers/${chargerId}/status`, {
                status: 'UNDER_MAINTENANCE',
                maintenanceNote
            });

            if (selectedStation) {
                await fetchChargers(selectedStation.id);
            }
        } catch (err) {
            console.error('Error marking charger under maintenance:', err);
            const errorMessage = err.response?.data?.error || err.message || 'Unknown error';
            alert(`Failed to mark charger under maintenance: ${errorMessage}`);
        }
    };

    const handleMarkAvailable = async (chargerId) => {
        try {
            await axios.put(`${API_BASE_URL}/api/chargers/${chargerId}/status`, {
                status: 'AVAILABLE'
            });

            if (selectedStation) {
                await fetchChargers(selectedStation.id);
            }
        } catch (err) {
            console.error('Error marking charger as available:', err);
            const errorMessage = err.response?.data?.error || err.message || 'Unknown error';
            alert(`Failed to mark charger as available: ${errorMessage}`);
        }
    };

    const handleDeleteCharger = async (chargerId) => {
        if (window.confirm('Are you sure you want to delete this charger?')) {
            try {
                await axios.delete(`${API_BASE_URL}/api/chargers/${chargerId}`);
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