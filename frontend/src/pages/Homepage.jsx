import React from "react";
import "../css/pages/Homepage.css";
import Header from "../components/global/Header.jsx";
import HeroSection from "../components/homepage/HeroSection";
import FeaturesSection from "../components/homepage/FeaturesSection";
import Footer from "../components/global/Footer.jsx";

const Homepage = () => (
    <div className="homepage">
        <Header />
        <HeroSection />
        <FeaturesSection />
        <Footer />
    </div>
);

export default Homepage;
