import React, { useContext } from 'react';
import { AuthContext } from '../auth/AuthContext';

/**
 * Top bar of the left panel.
 * Displays the current user's name and a logout button.
 */
export const SearchBox = () => {
    const { auth, logout } = useContext(AuthContext);

    return (
        <div className="headind_srch">
            {/* Current user's display name */}
            <div className="recent_heading mt-2">
                <h4>{auth.name}</h4>
            </div>

            {/* Logout action */}
            <div className="srch_bar">
                <div className="stylish-input-group">
                    <button
                        className="btn text-danger"
                        onClick={logout}
                    >
                        Logout
                    </button>
                </div>
            </div>
        </div>
    );
};
