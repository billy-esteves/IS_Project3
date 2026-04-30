package models;

/**
 * Represents a purchase event made by the system (e.g., supplier stock acquisition).
 *
 * <p>Philosophy:
 * This class models a cost-generating event in the system.
 * Each instance represents an item being bought by the shop from suppliers.</p>
 *
 * <p>It is used as an INPUT model in the Kafka pipeline, similar to Sale,
 * but representing expenses instead of revenue.</p>
 *
 * <p>Kafka Streams uses this class to compute costs, expenses, and profit
 * by comparing it against sales data.</p>
 */

public class Purchase {

    private String item;
    private double price;
    private int units;
    private long timestamp;

    public Purchase() {
        // needed for JSON deserialization
    }

    public Purchase(String item, double price, int units, long timestamp) {
        this.item = item;
        this.price = price;
        this.units = units;
        this.timestamp = timestamp;
    }

    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getUnits() { return units; }
    public void setUnits(int units) { this.units = units; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}