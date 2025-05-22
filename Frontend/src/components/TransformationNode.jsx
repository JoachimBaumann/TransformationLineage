import React from 'react';
import { Handle, Position } from 'reactflow';

const TransformationNode = ({ data }) => (
  <div className="react-flow__node transformation-node">
    <Handle type="target" position={Position.Top} />
    <div style={{ color: 'black', fontWeight: 'bold' }}>
      Transformation: {data.name}
    </div>
    <Handle type="source" position={Position.Bottom} />
  </div>
);

export default TransformationNode;
