import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/global/Header';
import Footer from '../components/global/Footer';
import '../css/pages/PaymentSuccess.css';
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_APP_API_BASE_URL || "http://localhost:8080";

const PaymentSuccess = () => {
    const navigate = useNavigate();
    const [verificationStatus, setVerificationStatus] = useState('Verifying payment...');
    const [isSuccess, setIsSuccess] = useState(false);

    useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const sessionId = urlParams.get('session_id');

    if (sessionId) {
        const verifyPayment = async () => {
            try {
                const { data } = await axios.get(`/api/payment/verify-session`, {
                    params: { session_id: sessionId }
                });

                setVerificationStatus('Payment successfully verified!');
                setIsSuccess(true);
                // Optionally redirect:
                // navigate('/dashboard', { replace: true });
            } catch (error) {
                const message = error.response?.data?.error || error.message || 'Unknown error';
                setVerificationStatus(`Payment verification failed: ${message}`);
                setIsSuccess(false);
                console.error('Payment verification error:', error);
            }
        };
        verifyPayment();
    } else {
        setVerificationStatus('No session ID found in URL.');
        setIsSuccess(false);
    }
}, [navigate]);

    return (
        <div className="payment-success-page">
            <Header />
            <main className="payment-success-content">
                <h1>{isSuccess ? 'Payment Successful!' : 'Payment Status'}</h1>
                <p>{verificationStatus}</p>
                <button 
                    onClick={() => navigate('/dashboard')}
                    className="go-to-dashboard-button"
                >
                    Go to Dashboard
                </button>
            </main>
            <Footer />
        </div>
    );
};

export default PaymentSuccess; 