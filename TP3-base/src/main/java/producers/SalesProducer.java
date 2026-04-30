package producers;

import com.google.gson.Gson;
import com.github.javafaker.Faker;
import models.Sale;
import org.apache.kafka.clients.producer.*;

import java.util.Properties;
import java.util.Random;

public class SalesProducer {

    // Kafka topic where sales events are published
    private static final String TOPIC = "sales-topic";

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
        String[] countries = {"Portugal", "Spain", "France", "Germany"};

        try {
            while (true) {

                String item = items[random.nextInt(items.length)];
                double price = 1 + (10 * random.nextDouble());
                int units = 1 + random.nextInt(5);
                String country = countries[random.nextInt(countries.length)];

                // Create Sale object (model)
                Sale sale = new Sale(
                        item,
                        price,
                        units,
                        country,
                        System.currentTimeMillis()
                );
                
                // Convert Sale object → JSON string
                String json = gson.toJson(sale);

                // Create Kafka record:
                // key = item (important for grouping in Streams)
                // value = JSON
                ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, item, json);

                producer.send(record);

                System.out.println("Sent Sale: " + json);

                Thread.sleep(1000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            producer.close();
        }
    }
}