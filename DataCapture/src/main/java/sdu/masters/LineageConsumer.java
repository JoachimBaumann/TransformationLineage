package sdu.masters;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class LineageConsumer {
    private final KafkaConsumer<String, String> consumer;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Neo4jLineageService lineageService;
    private volatile boolean running = true;

    public LineageConsumer(String bootstrapServers, String topic, String groupId, Neo4jLineageService lineageService) {
        this.lineageService = lineageService;

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));
    }

    public void pollAndProcess() {
        try {
            while (running) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        LineageRecord lineageRecord = objectMapper.readValue(record.value(), LineageRecord.class);
                        lineageService.recordLineage(lineageRecord);
                        System.out.println("Processed lineage record: " + lineageRecord.transformationId);
                    } catch (Exception e) {
                        System.err.println("Failed to process record: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Kafka polling error: " + e.getMessage());
        } finally {
            consumer.close();
            System.out.println("Kafka consumer closed.");
        }
    }

    public void close() {
        running = false;
    }
}
