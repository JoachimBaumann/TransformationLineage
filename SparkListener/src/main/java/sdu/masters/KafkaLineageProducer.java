package sdu.masters;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class KafkaLineageProducer {
    private final Producer<String, String> producer;
    private final String topic;

    // Constructor
    public KafkaLineageProducer(String bootstrapServers, String topic) {
        this.topic = topic;

        System.out.println("🟡 Initializing KafkaProducer with bootstrap servers: " + bootstrapServers);


        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        this.producer = new KafkaProducer<>(props);
        System.out.println("🟢 KafkaProducer initialized");

    }

    // Send a JSON string to Kafka
    public void sendEvent(String lineageEventJson) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, lineageEventJson);
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                System.err.println("❌ Error sending LineageEvent: " + exception.getMessage());
            } else {
                System.out.println("✅ Message sent to topic=" + metadata.topic() +
                        ", partition=" + metadata.partition() +
                        ", offset=" + metadata.offset());
            }
        });
    }


    // Close the producer
    public void close() {
        producer.flush();
        producer.close();
    }
}
