package sdu.masters;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        try (Neo4jLineageService lineageService = new Neo4jLineageService("bolt://localhost:7687", "neo4j", "ABCD123456abc")) {

            LineageRecord record3 = new LineageRecord();
            record3.transformationId = "3";
            record3.transformationName = "transformation 3";
            record3.transformationVersion = "1.0";
            record3.transformationType = "Spark";
            record3.timestamp = "2025-05-01T20:00:00Z";
            record3.inputPaths = List.of("2ndOutput.txt");
            record3.outputPath = "3ndOutput.txt";
            record3.datasetFormat = "txt";


            LineageRecord record2 = new LineageRecord();
            record2.transformationId = "2";
            record2.transformationName = "transformation 2";
            record2.transformationVersion = "1.0";
            record2.transformationType = "Spark";
            record2.timestamp = "2025-05-01T20:00:00Z";
            record2.inputPaths = List.of("1ndOutput.txt");
            record2.outputPath = "2ndOutput.txt";
            record2.datasetFormat = "txt";


            LineageRecord record = new LineageRecord();
            record.transformationId = "1";
            record.transformationName = "transformation 1";
            record.transformationVersion = "1.0";
            record.transformationType = "Spark";
            record.timestamp = "2025-05-01T20:00:00Z";
            record.inputPaths = List.of("input.txt", "input2.txt");
            record.outputPath = "1ndOutput.txt";
            record.datasetFormat = "txt";

            lineageService.recordLineage(record3);
        }
    }
}