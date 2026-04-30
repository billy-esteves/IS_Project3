package models;

/**
 * Represents an aggregated statistical result computed by Kafka Streams.
 *
 * <p>Philosophy:
 * Unlike Sale and Purchase, this class does NOT represent a real-world event.
 * Instead, it represents a derived metric calculated from multiple input events.</p>
 *
 * <p>It is used as an OUTPUT model in the Kafka pipeline, meaning it is produced
 * by stream processing operations such as reduce(), aggregate(), or join().</p>
 *
 * <p>This class follows a "result container" philosophy: it is generic,
 * lightweight, and designed to represent different computed metrics such as
 * revenue, expenses, or profit per item.</p>
 */

public class ItemStats {

    private String item;
    private double value;

    public ItemStats() {
        // needed for JSON deserialization (Kafka Connect / Gson)
    }

    public ItemStats(String item, double value) {
        this.item = item;
        this.value = value;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}