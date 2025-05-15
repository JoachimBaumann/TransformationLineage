package sdu.masters;

import java.util.List;

public class LineageExample {
    public static void main(String[] args) {
        try (Neo4jLineageService service = new Neo4jLineageService("bolt://localhost:7687", "neo4j", "ABCD123456abc")) {
            List<String> lineage = service.traceLineageBackwards("1ndOutput.txt");

            System.out.println("Upstream lineage:");
            for (String entry : lineage) {
                System.out.println("  → " + entry);
            }
        }
    }
}