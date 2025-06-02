import React from "react";
import Header from "../components/global/Header";
import Footer from "../components/global/Footer";
import "../css/pages/About.css";

const About = () => {
    return (
        <div className="homepage">
            {/* Header */}
            <Header />

            {/* About Main Content */}
            <div className="about-main-container">
                <div className="about-content-wrapper">
                    <div className="about-text-content">
                        <h1 className="about-heading">About NikCharge</h1>
                        <p className="about-paragraph">
                            NikCharge is committed to accelerating the electric vehicle revolution
                            by delivering accessible and innovative charging solutions. We're shaping the future of mobility by
                            connecting drivers to an expanding network of high-speed charging stations through intelligent
                            infrastructure and user-friendly technology.
                        </p>
                        <p className="about-paragraph">
                            Whether you're commuting daily, traveling long-distance, or just planning
                            your next stop, NikCharge helps you find fast, reliable, and cost-effective
                            charging stations across the country.
                        </p>
                        <div className="about-buttons">
                            <button className="about-button primary">Find a Station</button>
                            <button className="about-button secondary">Learn More</button>
                        </div>
                    </div>

                    <div className="about-image-container">
                        <div className="about-image">
                            <svg className="bolt-icon" viewBox="0 0 320 512" xmlns="http://www.w3.org/2000/svg">
                                <path fill="currentColor" d="M296 160H180.6l42.6-129.8C227.2 15 215.7 0 200 0H56C44 0 33.8 8.9 32.2 20.8l-32 240C-1.7 275.2 9.5 288 24 288h118.7L96.6 482.5c-3.6 15.2 8 29.5 23.3 29.5 8.4 0 16.4-4.4 20.8-12l176-304c9.3-15.9-2.2-36-20.7-36z"/>
                            </svg>
                        </div>
                    </div>
                </div>
            </div>

            {/* Why Choose Section */}
            <div className="why-choose-section">
                <h2 className="section-heading">Why Choose NikCharge</h2>
                <div className="features-container">
                    <div className="feature-card">
                        <div className="feature-icon-container">
                            <svg className="feature-icon" viewBox="0 0 320 512" xmlns="http://www.w3.org/2000/svg">
                                <path fill="currentColor" d="M296 160H180.6l42.6-129.8C227.2 15 215.7 0 200 0H56C44 0 33.8 8.9 32.2 20.8l-32 240C-1.7 275.2 9.5 288 24 288h118.7L96.6 482.5c-3.6 15.2 8 29.5 23.3 29.5 8.4 0 16.4-4.4 20.8-12l176-304c9.3-15.9-2.2-36-20.7-36z"/>
                            </svg>
                        </div>
                        <h3 className="feature-title">Fast Charging</h3>
                        <p className="feature-description">Get back on the road quickly with our high-speed charging stations</p>
                    </div>

                    <div className="feature-card">
                        <div className="feature-icon-container">
                            <svg className="feature-icon" viewBox="0 0 512 512" xmlns="http://www.w3.org/2000/svg">
                                <path fill="currentColor" d="M499.99 176h-59.87l-16.64-41.6C406.38 91.63 365.57 64 319.5 64h-127c-46.06 0-86.88 27.63-103.99 70.4L71.87 176H12.01C4.2 176-1.53 183.34.37 190.91l6 24C7.7 220.25 12.5 224 18.01 224h20.07C24.65 235.73 16 252.78 16 272v48c0 16.12 6.16 30.67 16 41.93V416c0 17.67 14.33 32 32 32h32c17.67 0 32-14.33 32-32v-32h256v32c0 17.67 14.33 32 32 32h32c17.67 0 32-14.33 32-32v-54.07c9.84-11.25 16-25.8 16-41.93v-48c0-19.22-8.65-36.27-22.07-48H494c5.51 0 10.31-3.75 11.64-9.09l6-24c1.89-7.57-3.84-14.91-11.65-14.91z"/>
                            </svg>
                        </div>
                        <h3 className="feature-title">Nationwide Coverage</h3>
                        <p className="feature-description">Access charging stations across the country for seamless travel</p>
                    </div>

                    <div className="feature-card">
                        <div className="feature-icon-container">
                            <svg className="feature-icon" viewBox="0 0 576 512" xmlns="http://www.w3.org/2000/svg">
                                <path fill="currentColor" d="M546.2 9.7c-5.6-12.5-21.6-13-28.3-1.2C486.9 62.4 431.4 96 368 96h-80C182 96 96 182 96 288c0 7 .8 13.7 1.5 20.5C161.3 262.8 253.4 224 384 224c8.8 0 16 7.2 16 16s-7.2 16-16 16C132.6 256 26 410.1 2.4 468c-6.6 16.3 1.2 34.9 17.5 41.6 16.4 6.8 35-1.1 41.8-17.3 1.5-3.6 20.9-47.9 71.9-90.6 32.4 43.9 94 85.8 174.9 77.2C465.5 467.5 576 326.7 576 154.3c0-50.2-10.8-102.2-29.8-144.6z"/>
                            </svg>
                        </div>
                        <h3 className="feature-title">Eco-Friendly</h3>
                        <p className="feature-description">Powered by renewable energy sources for a greener future</p>
                    </div>
                </div>
            </div>

            {/* Footer */}
            <Footer />
        </div>
    );
};

export default About;