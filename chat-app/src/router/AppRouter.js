import React, { useContext, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthContext } from '../auth/AuthContext';
import { ChatPage }    from '../pages/ChatPage';
import { AuthRouter }  from './AuthRouter';
import { PublicRoute }  from './PublicRoute';
import { PrivateRoute } from './PrivateRoute';

/**
 * Root router that verifies the stored token on mount and then renders
 * either the authenticated chat or the public auth flow.
 * Renders nothing while the token check is in progress.
 */
export const AppRouter = () => {
    const { auth, verifyToken } = useContext(AuthContext);

    // Verify the stored JWT on first render.
    useEffect(() => {
        console.log("verifiying token...")
        verifyToken();
    }, [verifyToken]);

    // Show a blank screen while the auth check is in flight.
    if (auth.checking) return <div />;

    return (
        <Router>
            <Routes>
                {/* Public auth pages (login / register) */}
                <Route
                    path="/auth/*"
                    element={
                        <PublicRoute isAuthenticated={auth.logged}>
                            <AuthRouter />
                        </PublicRoute>
                    }
                />

                {/* Protected chat page */}
                <Route
                    path="/*"
                    element={
                        <PrivateRoute isAuthenticated={auth.logged}>
                            <ChatPage />
                        </PrivateRoute>
                    }
                />

                {/* Fallback redirect */}
                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </Router>
    );
};
