import React, { useEffect, useState } from "react";
import "../css/pages/Search.css";
import Header from "../components/global/Header.jsx";
import Footer from "../components/global/Footer.jsx";
import FiltersPanel from "../components/SearchPage/FiltersPanel.jsx";
import MapDisplay from "../components/SearchPage/MapDisplay.jsx";

const Search = () => {
    const [stations, setStations] = useState([]);
    const [userLocation, setUserLocation] = useState(null);

    useEffect(() => {
        fetch("http://localhost:8080/api/stations")
            .then(res => res.json())
            .then(data => setStations(data))
            .catch(err => console.error("Failed to fetch stations", err));
    }, []);

    return (
        <div className="search">
            <Header />
            <main className="search-main">
                <div className="search-header">
                    <h1>Search for stations</h1>
                    <p>{stations.length} station{stations.length !== 1 && "s"} found</p>
                </div>
                <FiltersPanel setUserLocation={setUserLocation} />
                <MapDisplay stations={stations} userLocation={userLocation} />
            </main>
            <Footer />
        </div>
    );
};

export default Search;
