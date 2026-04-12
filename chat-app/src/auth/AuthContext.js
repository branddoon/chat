import { createContext, useState, useCallback, useContext } from 'react';
import { fetchWithoutToken, fetchWithToken } from '../helpers/fetch';
import { ChatContext } from '../context/chat/ChatContext';
import { types } from '../types/types';

/** React context that exposes authentication state and actions. */
export const AuthContext = createContext();

/** Initial authentication state — user is unknown until token is verified. */
const initialState = {
    uid:      null,
    checking: true,   // true while the token renewal check is in flight
    logged:   false,
    name:     null,
    email:    null,
};

/**
 * Provides authentication state (uid, name, logged, etc.) and actions
 * (login, register, verifyToken, logout) to all descendant components.
 *
 * @param {{ children: React.ReactNode }} props
 */
export const AuthProvider = ({ children }) => {
    const [auth, setAuth] = useState(initialState);
    const { dispatch }    = useContext(ChatContext);

    /**
     * Authenticates a user with email and password.
     * On success, stores the JWT in localStorage and updates auth state.
     *
     * @param {string} email
     * @param {string} password
     * @returns {Promise<true|string>} true on success, error message on failure.
     */
    const login = async (email, password) => {
        const resp = await fetchWithoutToken('login', { email, password }, 'POST');

        if (resp.ok) {
            localStorage.setItem('token', resp.token);
            setAuth({
                uid:      resp.id,
                checking: false,
                logged:   true,
                name:     resp.name,
                email:    resp.email,
            });
            return true;
        }

        return resp.msg;
    };

    /**
     * Registers a new user account.
     * On success, stores the JWT in localStorage and updates auth state.
     *
     * @param {string} name
     * @param {string} email
     * @param {string} password
     * @returns {Promise<true|string>} true on success, error message on failure.
     */
    const register = async (name, email, password) => {
        const resp = await fetchWithoutToken('login/new', { name, email, password }, 'POST');

        if (resp.ok) {
            localStorage.setItem('token', resp.token);
            setAuth({
                uid:      resp.id,
                checking: false,
                logged:   true,
                name:     resp.name,
                email:    resp.email,
            });
            return true;
        }

        return resp.msg;
    };

    /**
     * Verifies the stored JWT by calling the /renew endpoint.
     * Updates auth state based on whether the token is still valid.
     * Memoised with useCallback so it is safe to include in effect deps.
     *
     * @returns {Promise<boolean>} true if token is valid, false otherwise.
     */
    const verifyToken = useCallback(async () => {
        const token = localStorage.getItem('token');

        
        if (!token) {
            setAuth({ uid: null, checking: false, logged: false, name: null, email: null });
            return false;
        }

        const resp = await fetchWithToken('login/renew');

        if (resp.ok) {
            localStorage.setItem('token', resp.token);
            setAuth({
                uid:      resp.id,
                checking: false,
                logged:   true,
                name:     resp.name,
                email:    resp.email,
            });
            return true;
        }

        // Token is invalid or expired.
        setAuth({ uid: null, checking: false, logged: false, name: null, email: null });
        return false;
    }, []);

    /**
     * Logs the current user out by removing the JWT, resetting chat state,
     * and clearing auth state.
     */
    const logout = () => {
        localStorage.removeItem('token');
        dispatch({ type: types.closeSession });
        setAuth({ checking: false, logged: false });
    };

    return (
        <AuthContext.Provider value={{ auth, login, register, verifyToken, logout }}>
            {children}
        </AuthContext.Provider>
    );
};
