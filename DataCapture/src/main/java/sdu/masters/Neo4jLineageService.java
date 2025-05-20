package sdu.masters;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
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
                // Create or update output dataset node
                tx.run(
                        "MERGE (out:Dataset {id: $outputPath})",
                        //"SET out.format = $format",
                        Map.of(
                                "outputPath", record.outputPath
                                //"format", record.datasetFormat
                        )
                );

                // Create or update transformation node
                tx.run(
                        "MERGE (t:Transformation {id: $transformationId}) " +
                                "SET t.name = $name, " +
/*                                "    t.version = $version, " +
                                "    t.type = $type, " +*/
                                "    t.timestamp = $timestamp, " +
                                "    t.duration = $duration",
                        Map.of(
                                "transformationId", record.transformationId,
                                "name", record.transformationName,
                                /*"version", record.transformationVersion,
                                "type", record.transformationType,*/
                                "timestamp", record.timestamp,
                                "duration", record.duration
                        )
                );

                // Create relationship from transformation to output
                tx.run(
                        "MATCH (t:Transformation {id: $transformationId}), " +
                                "      (out:Dataset {id: $outputPath}) " +
                                "MERGE (t)-[:OUTPUT_TO]->(out)",
                        Map.of(
                                "transformationId", record.transformationId,
                                "outputPath", record.outputPath
                        )
                );

                // Link each input dataset to the transformation
                for (String inputPath : record.inputPaths) {
                    tx.run(
                            "MERGE (in:Dataset {id: $inputPath}) " +
                                    /*                                    "SET in.format = $format " +*/
                                    "WITH in " +
                                    "MATCH (t:Transformation {id: $transformationId}) " +
                                    "MERGE (in)-[:INPUT_TO]->(t)",
                            Map.of(
                                    "inputPath", inputPath,
                                    //"format", record.datasetFormat,
                                    "transformationId", record.transformationId
                            )
                    );
                }

                return null;
            });
        }
    }


    public String traceLineageBackwards(String outputDatasetId) throws Exception {
        Set<String> nodeIds = new HashSet<>();
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();

        try (Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run(
                        "MATCH path = (d:Dataset {id: $id})<-[:OUTPUT_TO|INPUT_TO*]-(n) " +
                                "WITH collect(path) AS paths " +
                                "UNWIND paths AS p " +
                                "RETURN p ORDER BY length(p)",
                        Values.parameters("id", outputDatasetId)
                );

                while (result.hasNext()) {
                    Record record = result.next();
                    Path path = record.get("p").asPath();

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
                                data.put("label", "Dataset: " + datasetId);
                                data.put("id", datasetId);
                            } else if ("Transformation".equals(label)) {
                                reactNode.put("type", "transformation");
                                String name = node.containsKey("name") ? node.get("name").asString() : "";
                                String timestamp = node.containsKey("timestamp") ? node.get("timestamp").asString() : "";
                                long duration = node.containsKey("duration") ? node.get("duration").asLong() : 0;
                                String jobName = node.containsKey("jobName") ? node.get("jobName").asString() : "";

                                data.put("label", "Transformation: " + name);
                                data.put("name", name);
                                data.put("timestamp", timestamp);
                                data.put("duration", duration);
                                data.put("jobName", jobName);
                            }

                            reactNode.put("data", data);
                            reactNode.put("position", Map.of(
                                    "x", Math.random() * 600,
                                    "y", Math.random() * 400
                            ));

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

                        edge.put("animated", true);
                        edge.put("markerEnd", Map.of("type", "arrowclosed"));

                        edges.add(edge);
                    }
                }

                return null;
            });
        }

        Map<String, Object> graph = Map.of(
                "nodes", nodes,
                "edges", edges
        );

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(graph);
    }

    @Override
    public void close() {
        driver.close();
    }
}