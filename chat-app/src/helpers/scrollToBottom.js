import { animateScroll } from 'react-scroll';

/**
 * Instantly scrolls to the bottom of a scrollable container.
 *
 * @param {string} id - The DOM id of the container element.
 */
export const scrollToBottom = (id) => {
    animateScroll.scrollToBottom({
        containerId: id,
        duration: 0,
    });
};

/**
 * Smoothly animates a scroll to the bottom of a scrollable container.
 *
 * @param {string} id - The DOM id of the container element.
 */
export const scrollToBottomAnimated = (id) => {
    animateScroll.scrollToBottom({
        containerId: id,
        duration: 250,
    });
};
