// components/employeeDashboard/SearchBar.jsx
import React from 'react';

const SearchBar = ({ searchTerm, onSearchChange, totalStations, filteredCount }) => {
    return (
        <div className="search-section">
            <div className="search-container">
                <div className="search-input-wrapper">
                    <svg className="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <circle cx="11" cy="11" r="8"></circle>
                        <path d="m21 21-4.35-4.35"></path>
                    </svg>
                    <input
                        type="text"
                        placeholder="Search stations by name or city..."
                        value={searchTerm}
                        onChange={(e) => onSearchChange(e.target.value)}
                        className="search-input"
                    />
                    {searchTerm && (
                        <button
                            className="clear-search"
                            onClick={() => onSearchChange('')}
                        >
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                <line x1="18" y1="6" x2="6" y2="18"></line>
                                <line x1="6" y1="6" x2="18" y2="18"></line>
                            </svg>
                        </button>
                    )}
                </div>

                <div className="search-results-info">
                    {searchTerm ? (
                        <span className="results-count">
                            {filteredCount} of {totalStations} stations found
                        </span>
                    ) : (
                        <span className="results-count">
                            Showing all {totalStations} stations
                        </span>
                    )}
                </div>
            </div>
        </div>
    );
};

export default SearchBar;