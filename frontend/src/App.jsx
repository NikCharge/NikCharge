import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Homepage from "./pages/Homepage";
import About from "./pages/About";
import SignUpLogin from "./pages/SignUpLogIn.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import Search from "./pages/Search.jsx";
import EmployeeDashboard from "./pages/EmployeeDashboard.jsx";
import ManagerDashboard from "./pages/ManagerDashboard";
import PaymentSuccess from "./pages/PaymentSuccess.jsx";

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<Homepage />} />
                <Route path="/about" element={<About />} />
                <Route path="/signup" element={<SignUpLogin />} />
                <Route path="/dashboard" element={<Dashboard />} />
                <Route path="/search" element={<Search />} />
                <Route path="/employee-dashboard" element={<EmployeeDashboard />} />
                <Route path="/manager-dashboard" element={<ManagerDashboard />} />
                <Route path="/success" element={<PaymentSuccess />} />
                <Route path="/cancel" element={<Dashboard />} />
            </Routes>
        </Router>
    );
}

export default App;