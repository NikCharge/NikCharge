// components/dashboard/StationsGrid.jsx
import React from 'react';
import StationCard from './StationCard';
import LoadingCard from './LoadingCard';
import ErrorCard from './ErrorCard';
import EmptyState from './EmptyState';
import '../../css/pages/EmployeeDashboard.css';

const StationsGrid = ({ stations, loading, error, onStationClick, onRetry }) => {
    return (
        <div className="stations-container">
            {loading && (
                <>
                    <LoadingCard />
                    <LoadingCard />
                    <LoadingCard />
                </>
            )}

            {error && !loading && (
                <ErrorCard message={error} onRetry={onRetry} />
            )}

            {!loading && !error && stations.length === 0 && (
                <EmptyState />
            )}

            {!loading && !error && stations.map((station) => (
                <StationCard
                    key={station.id}
                    station={station}
                    onClick={onStationClick}
                />
            ))}
        </div>
    );
};

export default StationsGrid;
