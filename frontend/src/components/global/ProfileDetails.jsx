import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

const RoleSwitcher = ({ user }) => {
    const [showOptions, setShowOptions] = useState(false);
    const [availableRoles, setAvailableRoles] = useState([]);
    const navigate = useNavigate();

    const roleOptionsMap = {
        CLIENT: ["EMPLOYEE", "MANAGER"],
        EMPLOYEE: ["CLIENT", "MANAGER"],
        MANAGER: ["CLIENT", "EMPLOYEE"]
    };

    useEffect(() => {
        if (user?.role) {
            setAvailableRoles(roleOptionsMap[user.role] || []);
        }
    }, [user]);

    const handleRoleChange = async (newRole) => {
        try {
            await axios.put(`/api/clients/changeRole/${user.id}`, {
                newRole: newRole
            });

            // Limpa o armazenamento e força logout
            localStorage.removeItem("client");
            navigate("/login", { replace: true });
        } catch (error) {
            console.error("Erro ao mudar role:", error);
            alert("Erro ao mudar o role. Tente novamente.");
        }
    };

    return (
        <div className="role-switcher">
            <button onClick={() => setShowOptions(!showOptions)} className="change-role-btn">
                Mudar Role
            </button>
            {showOptions && (
                <ul className="role-options-list">
                    {availableRoles.map(role => (
                        <li key={role}>
                            <button onClick={() => handleRoleChange(role)}>{role}</button>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};

export default RoleSwitcher;
