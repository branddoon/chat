/** Base URL for all API requests, set via environment variable. */
const baseUrl = process.env.REACT_APP_API_URL;

/**
 * Makes an HTTP request without an authentication token.
 * Used for public endpoints such as login and registration.
 * credentials: 'include' is required so the browser stores the HttpOnly cookie
 * that the server sets in the login/register response.
 *
 * @param {string} endPoint - API path (appended to baseUrl).
 * @param {object} [data]   - Request body for non-GET requests.
 * @param {string} [method='GET'] - HTTP method.
 * @returns {Promise<object>} Parsed JSON response.
 */
export const fetchWithoutToken = async (endPoint, data, method = 'GET') => {
    const url = `${baseUrl}/${endPoint}`;

    const resp = method === 'GET'
        ? await fetch(url, { credentials: 'include' })
        : await fetch(url, {
              method,
              headers: { 'Content-type': 'application/json' },
              credentials: 'include',
              body: JSON.stringify(data),
          });

    try {
        return await resp.json();
    } catch {
        return { ok: false };
    }
};

/**
 * Makes an HTTP request for protected endpoints.
 * The JWT is transmitted via the HttpOnly cookie set at login — the browser
 * attaches it automatically thanks to credentials: 'include'. No token is
 * read from or written to localStorage.
 *
 * @param {string} endPoint - API path (appended to baseUrl).
 * @param {object} [data]   - Request body for non-GET requests.
 * @param {string} [method='GET'] - HTTP method.
 * @returns {Promise<object>} Parsed JSON response.
 */
export const fetchWithToken = async (endPoint, data, method = 'GET') => {
    const url = `${baseUrl}/${endPoint}`;

    const resp = method === 'GET'
        ? await fetch(url, { credentials: 'include' })
        : await fetch(url, {
              method,
              headers: { 'Content-type': 'application/json' },
              credentials: 'include',
              body: JSON.stringify(data),
          });

    // Spring Security can respond with an empty body or HTML on 401/403,
    // both of which cause resp.json() to throw. Return { ok: false } so
    // callers (e.g. verifyToken) handle it as an unauthenticated response
    // instead of crashing.
    try {
        console.log("Complete resp object from server...")
        console.log(resp)
        return await resp.json();
    } catch {
        return { ok: false };
    }
};
