import React, { useState, useMemo } from 'react';
import GraphViewer from './components/GraphViewer';
import TopBar from './components/TopBar';
import Sidebar from './components/Sidebar';
import { nodes as rawNodes, edges as rawEdges } from './data/mockGraphData';
import { layoutGraph } from './utils/layoutGraph';
import './App.css';

function App() {
  const [selectedNode, setSelectedNode] = useState(null);

  const { nodes, edges } = useMemo(() => {
    return layoutGraph(rawNodes, rawEdges, 'TB');
  }, []);

  const handleNodeClick = (node) => {
    setSelectedNode(node);
    console.log('Selected node:', node);
  };

  return (
    <div className="app-container">
      <TopBar />
      <div className="main-layout">
        <Sidebar />
        <div className="graph-container">
          <GraphViewer nodes={nodes} edges={edges} onNodeClick={handleNodeClick} />
{selectedNode && (
  <div
    className="node-info-panel"
    style={{
      position: 'fixed',
      right: 20,
      top: 80, // not to overlap the topbar
      background: '#111',
      color: 'white',
      padding: '1rem',
      borderRadius: '8px',
      border: '1px solid #555',
      maxWidth: '300px',
      zIndex: 200 // above sidebar + canvas
    }}
  >
    <h3>Node Info</h3>
    <p><strong>ID:</strong> {selectedNode.data.id || selectedNode.id}</p>

    {selectedNode.type === 'transformation' && (
      <>
        <p><strong>Name:</strong> {selectedNode.data.name}</p>
        <p><strong>Timestamp:</strong> {selectedNode.data.timestamp}</p>
        <p><strong>Duration:</strong> {selectedNode.data.duration} ms</p>
      </>
    )}

    {selectedNode.type === 'dataset' && (
      <p><strong>Path:</strong> {selectedNode.data.id}</p>
    )}
  </div>
)}

        </div>
      </div>
    </div>
  );
}

export default App;
