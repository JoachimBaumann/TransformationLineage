package sdu.masters;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;

import java.util.List;

public class Main4test {

    public static void main(String[] args) {
        try (Neo4jLineageService lineageService = new Neo4jLineageService("bolt://joachimbaumann.dk:7687", "neo4j", "ABCD123456abc")) {

            LineageRecord record1 = new LineageRecord();
            record1.transformationId = "1";
            record1.transformationName = "transformation 1";
            record1.timestamp = "2025-05-01T20:10:00Z";
            record1.duration = 3268;
            record1.inputPaths = List.of("input1.txt", "input2.txt");
            record1.outputPath = "output1.txt";
            record1.gitSha = "a56ab";

            LineageRecord record2 = new LineageRecord();
            record2.transformationId = "2";
            record2.transformationName = "transformation 2";
            record2.timestamp = "2025-05-01T20:20:00Z";
            record2.duration = 2543;
            record2.inputPaths = List.of("input3.txt");
            record2.outputPath = "output2.txt";
            record2.gitSha = "a56ab";

            LineageRecord record3 = new LineageRecord();
            record3.transformationId = "3";
            record3.transformationName = "transformation 3";
            record3.timestamp = "2025-05-01T20:30:00Z";
            record3.duration = 4923;
            record3.inputPaths = List.of("input4.txt", "input5.txt");
            record3.outputPath = "output3.txt";
            record3.gitSha = "a56ab";

            LineageRecord record4 = new LineageRecord();
            record4.transformationId = "4";
            record4.transformationName = "transformation 4";
            record4.timestamp = "2025-05-01T20:40:00Z";
            record4.duration = 1036;
            record4.inputPaths = List.of("output1.txt");
            record4.outputPath = "output4.txt";
            record4.gitSha = "a56ab";

            LineageRecord record5 = new LineageRecord();
            record5.transformationId = "5";
            record5.transformationName = "transformation 5";
            record5.timestamp = "2025-05-01T20:50:00Z";
            record5.duration = 2125;
            record5.inputPaths = List.of("output2.txt", "output3.txt");
            record5.outputPath = "output5.txt";
            record5.gitSha = "a56ab";

            LineageRecord record6 = new LineageRecord();
            record6.transformationId = "6";
            record6.transformationName = "transformation 6";
            record6.timestamp = "2025-05-01T21:00:00Z";
            record6.duration = 3590;
            record6.inputPaths = List.of("output2.txt");
            record6.outputPath = "output6.txt";
            record6.gitSha = "a56ab";

            LineageRecord record7 = new LineageRecord();
            record7.transformationId = "7";
            record7.transformationName = "transformation 7";
            record7.timestamp = "2025-05-01T21:10:00Z";
            record7.duration = 1116;
            record7.inputPaths = List.of("output4.txt");
            record7.outputPath = "output7.txt";
            record7.gitSha = "a56ab";

            LineageRecord record8 = new LineageRecord();
            record8.transformationId = "8";
            record8.transformationName = "transformation 8";
            record8.timestamp = "2025-05-01T21:20:00Z";
            record8.duration = 3159;
            record8.inputPaths = List.of("output5.txt", "output6.txt");
            record8.outputPath = "output8.txt";
            record8.gitSha = "a56ab";

            LineageRecord record9 = new LineageRecord();
            record9.transformationId = "9";
            record9.transformationName = "transformation 9";
            record9.timestamp = "2025-05-01T21:30:00Z";
            record9.duration = 2901;
            record9.inputPaths = List.of("output6.txt");
            record9.outputPath = "output9.txt";
            record9.gitSha = "a56ab";

            LineageRecord record10 = new LineageRecord();
            record10.transformationId = "10";
            record10.transformationName = "transformation 10";
            record10.timestamp = "2025-05-01T21:40:00Z";
            record10.duration = 1925;
            record10.inputPaths = List.of("output7.txt", "output8.txt", "output9.txt");
            record10.outputPath = "output10.txt";
            record10.gitSha = "a56ab";

            // Record them in order
            lineageService.recordLineage(record1);
     /*       lineageService.recordLineage(record2);
            lineageService.recordLineage(record3);
            lineageService.recordLineage(record4);
            lineageService.recordLineage(record5);
            lineageService.recordLineage(record6);
            lineageService.recordLineage(record7);
            lineageService.recordLineage(record8);
            lineageService.recordLineage(record9);
            lineageService.recordLineage(record10);

      */


            //   String json = lineageService.traceLineageBackwards("output5.txt");
            //   System.out.println(json);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}
