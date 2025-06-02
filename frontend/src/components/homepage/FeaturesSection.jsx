import React from "react";
import "../../css/pages/Homepage.css";

const FeaturesSection = () => (
    <div className="white-bar">
        <div className="features-preview">
            <div className="feature-item">
                <div className="feature-icon">🔌</div>
                <h3>Fast Charging</h3>
                <p>Rapid charging solution for all EVs</p>
            </div>
            <div className="feature-item">
                <div className="feature-icon">🗺️</div>
                <h3>Smart Location</h3>
                <p>Find stations wherever you go</p>
            </div>
            <div className="feature-item">
                <div className="feature-icon">💰</div>
                <h3>Cost Effective</h3>
                <p>Save money with our innovative app</p>
            </div>
        </div>
    </div>
);

export default FeaturesSection;
