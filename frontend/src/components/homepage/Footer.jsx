import React from "react";
import logo from "../../assets/logo.png";
import "../../css/Homepage.css";

const Footer = () => (
    <footer className="footer">
        <div className="footer-content">
            <div className="footer-left">
                <p className="copyright">© 2025 NikCharge</p>
                <div className="social-icons">
                    <a href="#" className="social-icon">📱</a>
                    <a href="#" className="social-icon">💻</a>
                    <a href="#" className="social-icon">📧</a>
                </div>
            </div>

            <div className="footer-center">
                <img src={logo} alt="NikCharge Logo" className="footer-logo" />
            </div>

            <div className="footer-right">
                <p className="university">Universidade de Aveiro</p>
                <p className="department">Departamento de Electrónica, Telecomunicações e Informática</p>
                <p className="course">Testes de Qualidade de Software</p>
                <p className="authors">Ana Rita Silva, Ângela Ribeiro, Carolina Silva, Hugo Castro</p>
            </div>
        </div>
    </footer>
);

export default Footer;
