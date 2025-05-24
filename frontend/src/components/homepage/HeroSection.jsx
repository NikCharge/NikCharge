import React from "react";
import car from "../../assets/car.png";
import "../../css/Homepage.css";

const HeroSection = () => (
    <main className="main-section">
        <div className="text-section">
            <h1 className="headline animate-slide-up">
                Your smart <span className="highlight">electric</span><br />
                <span className="highlight">charging network</span>.
            </h1>
            <p className="subheading animate-fade-in">
                Find the nearest charging station for your EV with our smart app.
            </p>
            <div className="cta-buttons">
                <button className="cta-button primary">Get Started</button>
                <button className="cta-button secondary">Learn More</button>
            </div>
        </div>
        <div className="image-section">
            <div className="background-circle"></div>
            <img src={car} alt="Car at Charging Station" className="car-image animate-float" />
        </div>
    </main>
);

export default HeroSection;
