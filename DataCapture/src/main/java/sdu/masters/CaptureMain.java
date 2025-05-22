package sdu.masters;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CaptureMain {

    private final AppConfig config = new AppConfig();
    private final Neo4jLineageService lineageService;

    public CaptureMain() {
        this.lineageService = new Neo4jLineageService(
                config.neo4jUri,
                config.neo4jUser,
                config.neo4jPassword
        );
    }

    public static void main(String[] args) {
        System.out.println("Starting Spring Boot application with REST API and Kafka consumer...");
        SpringApplication.run(CaptureMain.class, args);
    }

    @Bean
    public Neo4jLineageService lineageService() {
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
                        lineageService // now this will be non-null
                );
                Runtime.getRuntime().addShutdownHook(new Thread(consumer::close));
                consumer.pollAndProcess();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
