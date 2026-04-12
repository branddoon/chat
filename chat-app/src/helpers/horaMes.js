import moment from 'moment';

/**
 * Formats a date/timestamp into a readable time and date string.
 *
 * @param {string|Date} date - The date value to format.
 * @returns {string} Formatted string, e.g. "14:30 pm | January 1st".
 */
export const formatDate = (date) => {
    return moment(date).format('HH:mm a | MMMM Do');
};
