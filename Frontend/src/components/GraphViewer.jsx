import React from 'react';
import ReactFlow, { MiniMap, Controls, Background } from 'reactflow';
import 'reactflow/dist/style.css';

import DatasetNode from './DatasetNode';
import TransformationNode from './TransformationNode';

const nodeTypes = {
  dataset: DatasetNode,
  transformation: TransformationNode
};

const GraphViewer = ({ nodes, edges, onNodeClick }) => {
  return (
    <div style={{ width: '100vw', height: '100vh' }}>
      <ReactFlow
        nodes={nodes}
        edges={edges}
        onNodeClick={(event, node) => onNodeClick(node)}
        fitView
        nodeTypes={nodeTypes}
      >
        <MiniMap />
        <Controls />
        <Background />
      </ReactFlow>
    </div>
  );
};

export default GraphViewer;
