package sdu.masters;

public class AppConfig {
    public final String kafkaBootstrapServers = System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "joachimbaumann.dk:9092");
    public final String kafkaTopic = System.getenv().getOrDefault("KAFKA_TOPIC", "LineageEvent");
    public final String kafkaGroupId = System.getenv().getOrDefault("KAFKA_GROUP_ID", "lineage-consumer-group");

    public final String neo4jUri = System.getenv().getOrDefault("NEO4J_URI", "bolt://joachimbaumann.dk:7687");
    public final String neo4jUser = System.getenv().getOrDefault("NEO4J_USER", "neo4j");
    public final String neo4jPassword = System.getenv().getOrDefault("NEO4J_PASSWORD", "ABCD123456abc");
}
