/** Base URL for all API requests, set via environment variable. */
const baseUrl = process.env.REACT_APP_API_URL;

/**
 * Makes an HTTP request without an authentication token.
 * Used for public endpoints such as login and registration.
 *
 * @param {string} endPoint - API path (appended to baseUrl).
 * @param {object} [data]   - Request body for non-GET requests.
 * @param {string} [method='GET'] - HTTP method.
 * @returns {Promise<object>} Parsed JSON response.
 */
export const fetchWithoutToken = async (endPoint, data, method = 'GET') => {
    const url = `${baseUrl}/${endPoint}`;

    if (method === 'GET') {
        const resp = await fetch(url);
        return await resp.json();
    }

    const resp = await fetch(url, {
        method,
        headers: { 'Content-type': 'application/json' },
        body: JSON.stringify(data),
    });
    return await resp.json();
};

/**
 * Makes an HTTP request with the JWT token from localStorage.
 * Used for protected endpoints that require authentication.
 *
 * @param {string} endPoint - API path (appended to baseUrl).
 * @param {object} [data]   - Request body for non-GET requests.
 * @param {string} [method='GET'] - HTTP method.
 * @returns {Promise<object>} Parsed JSON response.
 */
export const fetchWithToken = async (endPoint, data, method = 'GET') => {
    const url   = `${baseUrl}/${endPoint}`;
    const token = localStorage.getItem('token') || '';

    if (method === 'GET') {
        const resp = await fetch(url, {
            headers: { 'x-token': token },
        });
        return await resp.json();
    }

    const resp = await fetch(url, {
        method,
        headers: {
            'Content-type': 'application/json',
            'x-token': token,
        },
        body: JSON.stringify(data),
    });
    return await resp.json();
};
