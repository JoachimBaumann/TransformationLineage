package sdu.masters;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;

import java.util.List;

public class Main4test {

    public static void main(String[] args) {
        try (Neo4jLineageService lineageService = new Neo4jLineageService("bolt://localhost:7687", "neo4j", "ABCD123456abc")) {

            LineageRecord record3 = new LineageRecord();
            record3.transformationId = "3";
            record3.transformationName = "transformation 3";
            record3.timestamp = "2025-05-01T20:00:00Z";
            record3.duration = 3000;
            record3.inputPaths = List.of("2ndOutput.txt");
            record3.outputPath = "3rdOutput.txt";

            LineageRecord record2 = new LineageRecord();
            record2.transformationId = "2";
            record2.transformationName = "transformation 2";
            record2.timestamp = "2025-05-01T20:00:00Z";
            record2.duration = 2500;
            record2.inputPaths = List.of("1stOutput.txt");
            record2.outputPath = "2ndOutput.txt";

            LineageRecord record1 = new LineageRecord();
            record1.transformationId = "1";
            record1.transformationName = "transformation 1";
            record1.timestamp = "2025-05-01T20:00:00Z";
            record1.duration = 2000;
            record1.inputPaths = List.of("input.txt", "input2.txt");
            record1.outputPath = "1stOutput.txt";

            // Record them in order
            //  lineageService.recordLineage(record1);
            //   lineageService.recordLineage(record2);
               lineageService.recordLineage(record3);
            String json = lineageService.traceLineageBackwards("3rdOutput.txt");
            System.out.println(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}
