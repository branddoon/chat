import { Navigate } from 'react-router-dom';

/**
 * Renders children when the user is authenticated,
 * otherwise redirects to the /auth page.
 *
 * @param {{ isAuthenticated: boolean, children: React.ReactNode }} props
 */
export const PrivateRoute = ({ isAuthenticated, children }) => {
    return isAuthenticated ? children : <Navigate to="/auth" replace />;
};
