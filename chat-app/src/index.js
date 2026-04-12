import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import { ChatApp } from './ChatApp';

/** Mount the React app into the #root DOM element. */
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<ChatApp />);
