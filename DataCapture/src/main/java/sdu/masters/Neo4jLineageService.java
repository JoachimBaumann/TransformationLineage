package sdu.masters;



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


    public List<String> traceLineageBackwards(String outputDatasetId) {
        List<String> lineage = new ArrayList<>();

        try (Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run(
                        "MATCH path = (d:Dataset {id: $id})<-[:OUTPUT_TO|INPUT_TO*]-(n) " +
                                "WITH collect(path) AS paths " +
                                "UNWIND paths AS p " +
                                "RETURN p ORDER BY length(p)",
                        Values.parameters("id", outputDatasetId)
                );

                // To prevent duplicates while preserving order
                Set<String> seen = new HashSet<>();

                while (result.hasNext()) {
                    Record record = result.next();
                    Path path = record.get("p").asPath();

                    for (org.neo4j.driver.types.Node node : path.nodes()) {
                        String label = node.labels().iterator().next();
                        String representation;

                        if ("Dataset".equals(label)) {
                            representation = "Dataset: " + node.get("id").asString();
                        } else if ("Transformation".equals(label)) {
                            representation = "Transformation: " + node.get("name").asString() +
                                    " (v" + node.get("version").asString() + ")";
                        } else {
                            continue;
                        }

                        if (seen.add(representation)) {
                            lineage.add(representation);
                        }
                    }
                }

                return null;
            });
        }

        return lineage;
    }






    @Override
    public void close() {
        driver.close();
    }
}
