import graph from './neo4jGraph.json';

const uniqueEdgesMap = new Map();
graph.edges.forEach(edge => {
  const key = `${edge.source}->${edge.target}`;
  if (!uniqueEdgesMap.has(key)) {
    uniqueEdgesMap.set(key, edge);
  }
});

export const nodes = graph.nodes;
export const edges = Array.from(uniqueEdgesMap.values());
