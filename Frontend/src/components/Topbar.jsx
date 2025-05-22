import React from 'react';
import './Topbar.css';

const TopBar = () => (
  <header className="topbar">
    <h1>📊 Transformation Lineage</h1>
    <input type="text" placeholder="Search dataset or transformation..." />
  </header>
);

export default TopBar;
