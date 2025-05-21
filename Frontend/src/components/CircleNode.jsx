// src/components/CircleNode.jsx
import React from 'react';
import { Handle, Position } from 'reactflow';

const CircleNode = ({ data }) => {
  return (
    <div style={{
      border: '2px solid #555',
      borderRadius: '50%',
      width: 100,
      height: 100,
      textAlign: 'center',
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      background: 'white'
    }}>
      <Handle type="target" position={Position.Top} />
      <div>{data.label}</div>
      <Handle type="source" position={Position.Bottom} />
    </div>
  );
};

export default CircleNode;
