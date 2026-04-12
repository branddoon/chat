import React from 'react';
import { SearchBox } from './SearchBox';
import { Sidebar }   from './Sidebar';

/**
 * Left panel of the chat layout.
 * Contains the user header / logout button and the scrollable contact list.
 */
export const InboxPeople = () => {
    return (
        <div className="inbox_people">

            {/* Header: current user name + logout button */}
            <SearchBox />

            {/* Scrollable list of all other users */}
            <Sidebar />

        </div>
    );
};
