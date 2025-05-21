package sdu.masters;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;

import java.util.*;

public class Neo4jLineageService implements AutoCloseable {

    private final Driver driver;

    public Neo4jLineageService(String uri, String user, String password) {
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    public void recordLineage(LineageRecord record) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MERGE (out:Dataset {id: $outputPath})",
                        Map.of("outputPath", record.outputPath));

                tx.run("MERGE (t:Transformation {id: $transformationId}) " +
                                "SET t.name = $name, t.timestamp = $timestamp, t.duration = $duration",
                        Map.of(
                                "transformationId", record.transformationId,
                                "name", record.transformationName,
                                "timestamp", record.timestamp,
                                "duration", record.duration
                        ));

                tx.run("MATCH (t:Transformation {id: $transformationId}), " +
                                "(out:Dataset {id: $outputPath}) " +
                                "MERGE (t)-[:OUTPUT_TO]->(out)",
                        Map.of(
                                "transformationId", record.transformationId,
                                "outputPath", record.outputPath
                        ));

                for (String inputPath : record.inputPaths) {
                    tx.run("MERGE (in:Dataset {id: $inputPath}) " +
                                    "WITH in MATCH (t:Transformation {id: $transformationId}) " +
                                    "MERGE (in)-[:INPUT_TO]->(t)",
                            Map.of(
                                    "inputPath", inputPath,
                                    "transformationId", record.transformationId
                            ));
                }
                return null;
            });
        }
    }

    public String traceLineageBackwards(String outputDatasetId) throws Exception {
        return runAndBuildGraph(
                "MATCH path = (d:Dataset {id: $id})<-[:OUTPUT_TO|INPUT_TO*]-(n) " +
                        "WITH collect(path) AS paths UNWIND paths AS p RETURN p ORDER BY length(p)",
                Map.of("id", outputDatasetId));
    }

    public String traceLineageForward(String inputDatasetId) throws Exception {
        return runAndBuildGraph(
                "MATCH path = (d:Dataset {id: $id})-[:INPUT_TO|OUTPUT_TO*]->(n) " +
                        "WITH collect(path) AS paths UNWIND paths AS p RETURN p ORDER BY length(p)",
                Map.of("id", inputDatasetId));
    }

    public String getAllLineageData() throws Exception {
        try (Session session = driver.session()) {
            List<Path> allPaths = session.readTransaction(tx -> {
                Result result = tx.run("MATCH path = (n)-[r]->(m) RETURN path");
                List<Path> paths = new ArrayList<>();
                while (result.hasNext()) {
                    paths.add(result.next().get("path").asPath());
                }
                return paths;
            });
            return serializeGraph(buildLineageGraph(allPaths));
        }
    }

    private String runAndBuildGraph(String query, Map<String, Object> params) {
        try (Session session = driver.session()) {
            List<Path> paths = session.readTransaction(tx -> {
                Result result = tx.run(query, params);
                List<Path> pList = new ArrayList<>();
                while (result.hasNext()) {
                    pList.add(result.next().get("p").asPath());
                }
                return pList;
            });
            return serializeGraph(buildLineageGraph(paths));
        }
    }

    private Map<String, Object> buildLineageGraph(List<Path> paths) {
        Set<String> nodeIds = new HashSet<>();
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();

        for (Path path : paths) {
            for (Node node : path.nodes()) {
                String label = node.labels().iterator().next();
                String id = String.valueOf(node.id());

                if (nodeIds.add(id)) {
                    Map<String, Object> reactNode = new HashMap<>();
                    reactNode.put("id", id);
                    Map<String, Object> data = new HashMap<>();
                    if ("Dataset".equals(label)) {
                        reactNode.put("type", "dataset");
                        String datasetId = node.get("id").asString();
                        data.put("id", datasetId);
                        data.put("label", "Dataset: " + datasetId);
                    } else if ("Transformation".equals(label)) {
                        reactNode.put("type", "transformation");
                        String name = node.containsKey("name") ? node.get("name").asString() : "";
                        String timestamp = node.containsKey("timestamp") ? node.get("timestamp").asString() : "";
                        long duration = node.containsKey("duration") ? node.get("duration").asLong() : 0;
                        String jobName = node.containsKey("jobName") ? node.get("jobName").asString() : "";

                        data.put("name", name);
                        data.put("timestamp", timestamp);
                        data.put("duration", duration);
                        data.put("jobName", jobName);
                        data.put("label", "Transformation: " + name);
                    }
                    reactNode.put("data", data);
                    reactNode.put("position", Map.of("x", Math.random() * 600, "y", Math.random() * 400));
                    nodes.add(reactNode);
                }
            }

            for (org.neo4j.driver.types.Relationship rel : path.relationships()) {
                String source = String.valueOf(rel.startNodeId());
                String target = String.valueOf(rel.endNodeId());
                String edgeId = "e" + source + "-" + target;

                Map<String, Object> edge = new HashMap<>();
                edge.put("id", edgeId);
                edge.put("source", source);
                edge.put("target", target);
                edge.put("type", "default");
                edge.put("label", rel.type());
                edge.put("animated", false);
                edge.put("markerEnd", Map.of("type", "arrowclosed"));
                edges.add(edge);
            }
        }

        return Map.of("nodes", nodes, "edges", edges);
    }

    private String serializeGraph(Map<String, Object> graph) {
        try {
            return new ObjectMapper().writeValueAsString(graph);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize lineage graph", e);
        }
    }

    @Override
    public void close() {
        driver.close();
    }
}
