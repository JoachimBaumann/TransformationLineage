package sdu.masters;

public class CaptureMain {
    public static void main(String[] args) {
        AppConfig config = new AppConfig();

        System.out.println("Starting...");
        try (Neo4jLineageService lineageService = new Neo4jLineageService(
                config.neo4jUri,
                config.neo4jUser,
                config.neo4jPassword
        )) {
            LineageConsumer consumer = new LineageConsumer(
                    config.kafkaBootstrapServers,
                    config.kafkaTopic,
                    config.kafkaGroupId,
                    lineageService
            );

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutdown initiated...");
                consumer.close();
            }));

            consumer.pollAndProcess();  // blocking call, keeps service alive

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Finished Starting...");
    }
}
