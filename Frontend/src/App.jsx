import React, { useState, useEffect, useMemo } from 'react';
import GraphViewer from './components/GraphViewer';
import TopBar from './components/Topbar';
import Sidebar from './components/Sidebar';
import { layoutGraph } from './utils/layoutGraph';
import './App.css';

function App() {
  const [selectedNode, setSelectedNode] = useState(null);
  const [rawData, setRawData] = useState(null);
  const [traceMode, setTraceMode] = useState(null); // 'backward', 'forward', or null

  // Load full graph on startup
  useEffect(() => {
    fetch('http://joachimbaumann.dk:8080/api/lineage/all')
      .then(res => res.json())
      .then(data => {
        setRawData(data);
        setSelectedNode(null);
      })
      .catch(err => console.error('Failed to fetch lineage graph', err));
  }, []);

  const { nodes, edges } = useMemo(() => {
    if (!rawData) return { nodes: [], edges: [] };
    return layoutGraph(rawData.nodes, rawData.edges, 'TB');
  }, [rawData]);

  // Triggered when a node is clicked
  const handleNodeClick = (node) => {
    if (traceMode === 'backward' || traceMode === 'forward') {
      const direction = traceMode === 'backward' ? 'backwards' : 'forwards';
      const id = node.data?.id || node.id;

      fetch(`http://joachimbaumann.dk:8080/api/lineage/${direction}/${id}`)
        .then(res => res.json())
        .then(data => {
          setRawData(data);
          setSelectedNode(null);
        })
        .catch(err => console.error(`Failed to trace ${direction} from ${id}`, err));

      setTraceMode(null); // reset mode after tracing
    } else {
      setSelectedNode(node);
      console.log('Selected node:', node);
    }
  };

  // Handle sidebar button clicks
  const handleTrace = (mode) => {
    if (mode === 'all') {
      setTraceMode(null);
      fetch('http://joachimbaumann.dk:8080/api/lineage/all')
        .then(res => res.json())
        .then(data => {
          setRawData(data);
          setSelectedNode(null);
        })
        .catch(err => console.error('Failed to fetch full graph', err));
    } else {
      setTraceMode(mode); // 'backward' or 'forward'
    }
  };

  return (
    <div className="app-container">
      <TopBar />
      <div className="main-layout">
        <Sidebar onTrace={handleTrace} />
        <div className="graph-container">
          {rawData ? (
            <GraphViewer
              key={JSON.stringify(nodes.map(n => n.id))} // force remount on node change
              nodes={nodes}
              edges={edges}
              onNodeClick={handleNodeClick}
            />
          ) : (
            <p style={{ color: 'white', padding: '1rem' }}>Loading lineage graph...</p>
          )}

          {selectedNode && (
            <div
              className="node-info-panel"
              style={{
                position: 'fixed',
                right: 20,
                top: 80,
                background: '#111',
                color: 'white',
                padding: '1rem',
                borderRadius: '8px',
                border: '1px solid #555',
                maxWidth: '300px',
                zIndex: 200
              }}
            >
              <h3>Node Info</h3>
              <p><strong>ID:</strong> {selectedNode.data.id || selectedNode.id}</p>

              {selectedNode.type === 'transformation' && (
                <>
                  <p><strong>Name:</strong> {selectedNode.data.name}</p>
                  <p><strong>Timestamp:</strong> {selectedNode.data.timestamp}</p>
                  <p><strong>Duration:</strong> {selectedNode.data.duration} ms</p>
                  <p><strong>Git Version:</strong> {selectedNode.data.gitSha}</p>
                    <p>
      <strong>Inspect Code:</strong>{' '}
      <a
        href={`https://github.com/JoachimBaumann/TransformationLineage/tree/${selectedNode.data.gitSha}/transformations/${selectedNode.data.name}`}
        target="_blank"
        rel="noopener noreferrer"
        style={{ color: '#4ea1d3', textDecoration: 'underline' }}
      >
        View on GitHub
      </a>
    </p>
  </>
              )}

              {selectedNode.type === 'dataset' && (
                <p><strong>Path:</strong> {selectedNode.data.id}</p>
              )}
            </div>
          )}

          {traceMode && (
            <div style={{
              position: 'fixed',
              top: 10,
              left: 250,
              background: '#222',
              color: '#eee',
              padding: '0.5rem 1rem',
              borderRadius: '4px',
              border: '1px solid #444',
              zIndex: 300
            }}>
              Click a node to trace <strong>{traceMode}</strong>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default App;
