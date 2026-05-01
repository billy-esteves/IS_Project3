package is.project3.producers;


import com.google.gson.Gson;
import com.github.javafaker.Faker;
import is.project3.models.Purchase;
import org.apache.kafka.clients.producer.*;

import java.util.Properties;
import java.util.Random;

public class PurchaseProducer {

    // Kafka topic where sales events are published
    private static final String TOPIC = "purchases-topic";

    public static void main(String[] args) {

        // Kafka producer configuration
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:29092");     // Address of Kafka brokers (from docker-compose)
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");      // Key serializer (we use String for item)
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");    // Value serializer (JSON string)

        // Create Kafka producer instance
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        Gson gson = new Gson();         // Gson converts Java objects to JSON
        Faker faker = new Faker();      // Faker + Random used to generate fake data
        Random random = new Random();

        String[] items = {"coffee", "tea", "bread", "milk"};

        try {
            while (true) {

                String item = items[random.nextInt(items.length)];
                double price = 0.5 + (5 * random.nextDouble());
                int units = 1 + random.nextInt(10);

                // Create Purchase object (model)
                Purchase purchase = new Purchase(
                        item,
                        price,
                        units,
                        System.currentTimeMillis()
                );
                
                // Convert Sale object → JSON string
                String json = gson.toJson(purchase);

                // Create Kafka record:
                // key = item (important for grouping in Streams)
                // value = JSON
                ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, item, json);

                producer.send(record);

                System.out.println("Sent Purchase: " + json);

                Thread.sleep(1200);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            producer.close();
        }
    }
}