package sdu.masters;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CaptureMain {

    private final AppConfig config = new AppConfig();
    private Neo4jLineageService lineageService;

    public static void main(String[] args) {
        System.out.println("Starting Spring Boot application with REST API and Kafka consumer...");
        SpringApplication.run(CaptureMain.class, args);
    }

    @Bean
    public Neo4jLineageService lineageService() {
        this.lineageService = new Neo4jLineageService(
                config.neo4jUri,
                config.neo4jUser,
                config.neo4jPassword
        );
        return this.lineageService;
    }

    @PostConstruct
    public void startKafkaConsumer() {
        new Thread(() -> {
            try {
                LineageConsumer consumer = new LineageConsumer(
                        config.kafkaBootstrapServers,
                        config.kafkaTopic,
                        config.kafkaGroupId,
                        lineageService
                );
                Runtime.getRuntime().addShutdownHook(new Thread(consumer::close));
                consumer.pollAndProcess(); // blocking call in background thread
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
