import React from 'react';
import { Handle, Position } from 'reactflow';

const DatasetNode = ({ data }) => (
  <div className="react-flow__node dataset-node">
    <Handle type="target" position={Position.Top} />
    <div style={{ color: 'black', fontWeight: 'bold' }}>
      Dataset: {data.id}
    </div>
    <Handle type="source" position={Position.Bottom} />
  </div>
);

export default DatasetNode;
