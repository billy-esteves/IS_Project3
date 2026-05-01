import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.StreamsConfig;

import is.project3.streams.AnalyticsTopology;
import is.project3.producers.SalesProducer;
import is.project3.producers.PurchaseProducer;
import is.project3.rest.RestServer;
import is.project3.cli.CommandLineApp;

import java.util.Properties;

public class Main {

    private static KafkaStreams streams;

    public static void main(String[] args) {

        System.out.println("Starting System...");

        // 1. START KAFKA STREAMS
        startStreams();

        // 2. START PRODUCERS
        startProducers();

        // TODO: implement
        // 3. START REST API (if implemented)
        //startRest();

        // TODO: implement
        // 4. START CLI (optional)
        //startCLI();

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            if (streams != null) streams.close();
        }));
    }

    private static void startStreams() {
        System.out.println("Starting Kafka Streams...");

        Topology topology = AnalyticsTopology.build();

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "project3-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, 
                org.apache.kafka.common.serialization.Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, 
                org.apache.kafka.common.serialization.Serdes.String().getClass());

        streams = new KafkaStreams(topology, props);

        streams.start();

        System.out.println("Kafka Streams started.");
    }

    private static void startProducers() {

        System.out.println("Starting Producers...");

        Thread salesThread = new Thread(() -> {
            SalesProducer.main(null);
        });

        Thread purchaseThread = new Thread(() -> {
            PurchaseProducer.main(null);
        });

        salesThread.start();
        purchaseThread.start();

        System.out.println("Producers started.");
    }
    
    // TODO: implement
    /*
    private static void startRest() {
        try {
            System.out.println("Starting REST API...");
            new Thread(() -> RestServer.start()).start();
        } catch (Exception e) {
            System.out.println("REST not started (maybe not implemented yet)");
        }
    }
    */

    // TODO: implement
    /*
    private static void startCLI() {
        try {
            System.out.println("Starting CLI...");
            CommandLineApp.run();
        } catch (Exception e) {
            System.out.println("CLI not started (optional)");
        }
    }
    */
}