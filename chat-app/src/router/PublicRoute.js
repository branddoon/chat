import { Navigate } from 'react-router-dom';

/**
 * Renders children when the user is NOT authenticated,
 * otherwise redirects authenticated users to the home page.
 *
 * @param {{ isAuthenticated: boolean, children: React.ReactNode }} props
 */
export const PublicRoute = ({ isAuthenticated, children }) => {
    return !isAuthenticated ? children : <Navigate to="/" replace />;
};
