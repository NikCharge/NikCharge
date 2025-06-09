import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/global/Header';
import Footer from '../components/global/Footer';
import '../css/pages/PaymentSuccess.css';

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
                    const response = await fetch(`http://localhost:8080/api/payment/verify-session?session_id=${sessionId}`);
                    const data = await response.json();

                    if (response.ok) {
                        setVerificationStatus('Payment successfully verified!');
                        setIsSuccess(true);
                        // Optionally, fetch updated reservation list or navigate
                        // navigate('/dashboard', { replace: true });
                    } else {
                        setVerificationStatus(`Payment verification failed: ${data.error || response.statusText}`);
                        setIsSuccess(false);
                    }
                } catch (error) {
                    setVerificationStatus('An error occurred during payment verification.');
                    setIsSuccess(false);
                    console.error('Payment verification error:', error);
                }
            };
            verifyPayment();
        } else {
            setVerificationStatus('No session ID found in URL.');
            setIsSuccess(false);
        }
    }, [navigate]); // Added navigate to dependency array

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