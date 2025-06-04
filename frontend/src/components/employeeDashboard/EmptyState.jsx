// components/dashboard/EmptyState.jsx
import React from 'react';
import { Zap } from 'lucide-react';
import '../../css/pages/EmployeeDashboard.css';

const EmptyState = () => (
    <div className="empty-state">
        <Zap size={48} />
        <h3>No stations found</h3>
        <p>There are no charging stations available at the moment.</p>
    </div>
);

export default EmptyState;