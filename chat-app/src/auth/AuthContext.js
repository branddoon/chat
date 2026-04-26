import { createContext, useState, useCallback, useContext } from 'react';
import { fetchWithoutToken, fetchWithToken } from '../helpers/fetch';
import { ChatContext } from '../context/chat/ChatContext';
import { types } from '../types/types';

/** React context that exposes authentication state and actions. */
export const AuthContext = createContext();

/**
 * Initial authentication state — user is unknown until token is verified.
 * `token` is kept in memory solely for the Socket.IO handshake; it is never
 * written to localStorage or sessionStorage.
 */
const initialState = {
    uid:      null,
    checking: true,   // true while the token renewal check is in flight
    logged:   false,
    name:     null,
    email:    null,
    token:    null,   // in-memory only — used for Socket.IO auth, not HTTP requests
};

/**
 * Provides authentication state (uid, name, logged, etc.) and actions
 * (login, register, verifyToken, logout) to all descendant components.
 *
 * JWT security model:
 *  - HTTP requests: the JWT travels as an HttpOnly cookie (set by the server).
 *    The browser attaches it automatically; JavaScript cannot read it.
 *  - Socket.IO: because the WS server runs on a different port, cookies do not
 *    travel there. The token is kept in this context's in-memory state and
 *    passed as a query param only for the initial socket handshake.
 *
 * @param {{ children: React.ReactNode }} props
 */
export const AuthProvider = ({ children }) => {
    const [auth, setAuth] = useState(initialState);
    const { dispatch }    = useContext(ChatContext);

    /**
     * Authenticates a user with email and password.
     * The server sets an HttpOnly cookie in the response; no token is stored
     * in localStorage. The token is also returned in the body and held in
     * memory for the Socket.IO handshake.
     *
     * @param {string} email
     * @param {string} password
     * @returns {Promise<true|string>} true on success, error message on failure.
     */
    const login = async (email, password) => {
        const resp = await fetchWithoutToken('login', { email, password }, 'POST');

        if (resp.ok) {
            setAuth({
                uid:      resp.id,
                checking: false,
                logged:   true,
                name:     resp.name,
                email:    resp.email,
                token:    resp.token,
            });
            return true;
        }

        return resp.msg;
    };

    /**
     * Registers a new user account.
     * The server sets an HttpOnly cookie in the response; the token is held
     * only in memory for the Socket.IO handshake.
     *
     * @param {string} name
     * @param {string} email
     * @param {string} password
     * @returns {Promise<true|string>} true on success, error message on failure.
     */
    const register = async (name, email, password) => {
        const resp = await fetchWithoutToken('login/new', { name, email, password }, 'POST');

        if (resp.ok) {
            setAuth({
                uid:      resp.id,
                checking: false,
                logged:   true,
                name:     resp.name,
                email:    resp.email,
                token:    resp.token,
            });
            return true;
        }

        return resp.msg;
    };

    /**
     * Verifies the session by calling the /renew endpoint.
     * The HttpOnly cookie is sent automatically by the browser; no token is
     * read from localStorage. If the cookie is absent or invalid the server
     * returns a non-ok response and the user is treated as logged out.
     * Memoised with useCallback so it is safe to include in effect deps.
     *
     * @returns {Promise<boolean>} true if the session is valid, false otherwise.
     */
    const verifyToken = useCallback(async () => {
        const resp = await fetchWithToken('login/renew');
        console.log("Response from server...");
        console.log(resp);
        if (resp.ok) {
            setAuth({
                uid:      resp.id,
                checking: false,
                logged:   true,
                name:     resp.name,
                email:    resp.email,
                token:    resp.token,
            });
            console.log("Auth object...")
            console.log(auth)
            return true;
        }

        // Cookie absent, expired, or invalid.
        setAuth({ uid: null, checking: false, logged: false, name: null, email: null, token: null });
        return false;
    }, []);

    /**
     * Logs the current user out.
     * Calls the server's logout endpoint so it can expire the HttpOnly cookie
     * (client-side JS cannot delete an HttpOnly cookie directly), then resets
     * all local auth and chat state.
     */
    const logout = async () => {
        await fetchWithToken('login/logout', {}, 'POST');
        dispatch({ type: types.closeSession });
        setAuth({ checking: false, logged: false, uid: null, name: null, email: null, token: null });
    };

    return (
        <AuthContext.Provider value={{ auth, login, register, verifyToken, logout }}>
            {children}
        </AuthContext.Provider>
    );
};
