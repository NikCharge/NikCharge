import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Homepage from "./pages/Homepage";
import About from "./pages/About";
import SignUpLogin from "./pages/SignUpLogIn.jsx";

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<Homepage />} />
                <Route path="/about" element={<About />} />
                <Route path="/signup" element={<SignUpLogin />} />
            </Routes>
        </Router>
    );
}

export default App;