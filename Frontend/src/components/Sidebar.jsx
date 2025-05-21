import React from 'react';
import './Sidebar.css';

const Sidebar = ({ onTrace }) => (
  <aside className="sidebar">
    <button onClick={() => onTrace('all')}>Show All</button>
    <button onClick={() => onTrace('backward')}>Trace Backward</button>
    <button onClick={() => onTrace('forward')}>Trace Forward</button>
  </aside>
);

export default Sidebar;
