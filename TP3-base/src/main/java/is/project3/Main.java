import org.apache.kafka.streams.KafkaStreams;
import is.project3.streams.AnalyticsTopology;
import is.project3.producers.SalesProducer;
import is.project3.producers.PurchaseProducer;
import is.project3.rest.RestServer;
import is.project3.cli.CommandLineApp;

public class Main {

    private static KafkaStreams streams;

    public static void main(String[] args) {

        System.out.println("🚀 Starting Project 3 System...");

        // 1. START KAFKA STREAMS
        startStreams();

        // 2. START PRODUCERS
        startProducers();

        // 3. START REST API (if implemented)
        //startRest();

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

        streams = new AnalyticsTopology().buildStreams();

        streams.start();

        System.out.println("Kafka Streams started.");
    }

    private static void startProducers() {

        System.out.println("Starting Producers...");

        Thread salesThread = new Thread(() -> {
            SalesProducer producer = new SalesProducer();
            producer.startProducing();
        });

        Thread purchaseThread = new Thread(() -> {
            PurchaseProducer producer = new PurchaseProducer();
            producer.startProducing();
        });

        salesThread.start();
        purchaseThread.start();

        System.out.println("Producers started.");
    }
    
    private static void startRest() {
        try {
            System.out.println("Starting REST API...");
            new Thread(() -> RestServer.start()).start();
        } catch (Exception e) {
            System.out.println("REST not started (maybe not implemented yet)");
        }
    }

    private static void startCLI() {
        try {
            System.out.println("Starting CLI...");
            CommandLineApp.run();
        } catch (Exception e) {
            System.out.println("CLI not started (optional)");
        }
    }
}