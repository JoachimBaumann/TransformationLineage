package sdu.masters;


public class Main {

    public static void main(String[] args) {
        KafkaLineageProducer producer = new KafkaLineageProducer("joachimbaumann.dk:9092", "LineageEvent");

        System.out.println("test");
        producer.sendEvent("Hello World");
        producer.close();  // Flush + close
    }

}