import React from "react";
import "../css/Homepage.css";
import Header from "../components/homepage/Header";
import HeroSection from "../components/homepage/HeroSection";
import FeaturesSection from "../components/homepage/FeaturesSection";
import Footer from "../components/homepage/Footer";

const Homepage = () => (
    <div className="homepage">
        <Header />
        <HeroSection />
        <FeaturesSection />
        <Footer />
    </div>
);

export default Homepage;
