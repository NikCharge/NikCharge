import React, { useState, useEffect } from 'react';
import '../../css/pages/EmployeeDashboard.css'; // Reuse dashboard styles for modal base

const MaintenanceNoteModal = ({ show, onClose, onSubmit, chargerId }) => {
    const [maintenanceNote, setMaintenanceNote] = useState('');

    // Clear the note when the modal is closed
    useEffect(() => {
        if (!show) {
            setMaintenanceNote('');
        }
    }, [show]);

    const handleSubmit = () => {
        onSubmit(chargerId, maintenanceNote);
        onClose(); // Close modal after submitting
    };

    if (!show) {
        return null;
    }

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2>Add Maintenance Note for Charger #{chargerId}</h2>
                    <button className="close-button" onClick={onClose}>
                         <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <line x1="18" y1="6" x2="6" y2="18"></line>
                            <line x1="6" y1="6" x2="18" y2="18"></line>
                        </svg>
                    </button>
                </div>

                <div className="modal-body">
                    <div className="form-group">
                        <label htmlFor="maintenanceNote">Maintenance Note:</label>
                        <textarea
                            id="maintenanceNote"
                            value={maintenanceNote}
                            onChange={(e) => setMaintenanceNote(e.target.value)}
                            rows="4"
                            className="form-control"
                            placeholder="Enter details about the maintenance issue..."
                        ></textarea>
                    </div>
                </div>

                <div className="modal-footer">
                    <button className="button secondary" onClick={onClose}>Cancel</button>
                    <button className="button primary" onClick={handleSubmit}>Submit</button>
                </div>
            </div>
        </div>
    );
};

export default MaintenanceNoteModal; 