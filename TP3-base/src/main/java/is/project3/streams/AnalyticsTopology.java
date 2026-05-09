package is.project3.streams;
 
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.state.KeyValueStore;
 
import java.time.Duration;
 
public class AnalyticsTopology {

    private static final boolean debug = true; // Set to true to enable debug logs
 
    private static final Gson gson = new Gson();
 
    public static Topology build() {
        StreamsBuilder builder = new StreamsBuilder();
 
        // INPUT STREAMS
        KStream<String, String> sales = builder.stream("sales-topic", Consumed.with(Serdes.String(), Serdes.String()));
        KStream<String, String> purchases = builder.stream("purchases-topic", Consumed.with(Serdes.String(), Serdes.String()));
 
        // ============= REVENUE PER ITEM =============
        KTable<String, Double> revenuePerItem = 
                sales.mapValues(value -> {
                    JsonObject json = gson.fromJson(value, JsonObject.class);
                    return json.get("price").getAsDouble() * json.get("units").getAsInt();
                })
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
                .reduce(
                        Double::sum,
                        Materialized.as("revenue-per-item-store")
                );

        if (debug) {
            System.out.println("Debug: revenuePerItem KTable created");
            System.out.println("Debug: revenuePerItem KTable content:");
            revenuePerItem.toStream().foreach((key, value) -> System.out.println("[]Item: " + key + ", Revenue: " + value));
        }
 
        revenuePerItem.toStream()
                .mapValues(v -> toJson("revenuePerItem", v))
                .to("output-revenue-per-item", Produced.with(Serdes.String(), Serdes.String()));
 
        // ============= EXPENSES PER ITEM =============
        KTable<String, Double> expensesPerItem = purchases
                .mapValues(value -> {
                    JsonObject json = gson.fromJson(value, JsonObject.class);
                    return json.get("price").getAsDouble() * json.get("units").getAsInt();
                })
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
                .reduce(
                        Double::sum,
                        Materialized.as("expenses-per-item-store")
                );

        if (debug) {
            System.out.println("Debug: expensesPerItem KTable created");
            System.out.println("Debug: expensesPerItem KTable content:");
            expensesPerItem.toStream().foreach((key, value) -> System.out.println("[]Item: " + key + ", Expenses: " + value));
        }
 
        expensesPerItem.toStream()
                .mapValues(v -> toJson("expensesPerItem", v))
                .to("output-expenses-per-item", Produced.with(Serdes.String(), Serdes.String()));
 
        // ============= PROFIT PER ITEM =============
        KTable<String, Double> profitPerItem = revenuePerItem.join(
                expensesPerItem,
                (revenue, expense) -> revenue - expense
        );

        profitPerItem.toStream()
                .mapValues(v -> toJson("profitPerItem", v))
                .to("output-profit-per-item", Produced.with(Serdes.String(), Serdes.String()));
 
        // ============= TOTAL REVENUE =============
        KTable<String, Double> totalRevenue = revenuePerItem
                .toStream()
                .groupBy((k, v) -> "total", Grouped.with(Serdes.String(), Serdes.Double()))
                .reduce(
                        Double::sum,
                        Materialized.as("total-revenue-store")

                );

        if (debug) {
            System.out.println("Debug: totalRevenue KTable created");
            System.out.println("Debug: totalRevenue KTable content:");
            totalRevenue.toStream().foreach((key, value) -> System.out.println("[]Key: " + key + ", Total Revenue: " + value));
        }

        totalRevenue.toStream()
                .mapValues(v -> toJson("totalRevenue", v))
                .to("output-total-revenue", Produced.with(Serdes.String(), Serdes.String()));
    
        // ============= AVERAGE AMOUNT SPENT PER PURCHASE =============
        KTable<String, double[]> purchaseAverages = purchases
                .mapValues(value -> {
                    JsonObject json = gson.fromJson(value, JsonObject.class);
                    return json.get("price").getAsDouble() * json.get("units").getAsInt();
                })
                .groupBy((k, v) -> "all", Grouped.with(Serdes.String(), Serdes.Double()))
                .aggregate(
                        () -> new double[]{0, 0},  // {total, count}
                        (k, spent, stats) -> {
                            stats[0] += spent;
                            stats[1] += 1;
                            return stats;
                        },
                        Materialized.<String, double[], KeyValueStore<Bytes, byte[]>>as("purchase-averages-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(doubleArraySerdes())
                );
 
        purchaseAverages.toStream()
                .mapValues(stats -> toJson("averageAmountPerPurchase", stats[0] / stats[1]))
                .to("output-purchase-averages", Produced.with(Serdes.String(), Serdes.String()));

        if (debug) {
            System.out.println("Debug: purchaseAverages KTable created");
            System.out.println("Debug: purchaseAverages KTable content:");
            purchaseAverages.toStream().foreach((key, value) -> System.out.println("[]Key: " + key + ", Total Spent: " + value[0] + ", Count: " + value[1]));
        }

        /**
        // ============= ITEM WITH HIGHEST PROFIT =============
        KTable<String, ItemProfit> maxProfitTracker = profitPerItem
                .toStream()
                .map((item, profit) -> new KeyValue<>("max", new ItemProfit(item, profit)))
                .groupByKey(Grouped.with(Serdes.String(), itemProfitSerdes()))
                .reduce(
                        (ip1, ip2) -> ip1.profit >= ip2.profit ? ip1 : ip2,
                        Materialized.as("max-profit-tracker")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(itemProfitSerdes())
                );
 
        maxProfitTracker.toStream()
                .mapValues(ip -> toJson("highestProfitItem", ip.item + " (" + String.format("%.2f", ip.profit) + ")"))
                .to("output-highest-profit-item", Produced.with(Serdes.String(), Serdes.String()));
 
        // ============= TOTAL REVENUE IN LAST HOUR =============
        revenuePerItem
                .toStream()
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
                .windowedBy(TimeWindows.of(Duration.ofHours(1)))
                .reduce(
                        Double::sum,
                        Materialized.as("revenue-1hour-window")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Double())
                )
                .toStream()
                .mapValues(v -> toJson("revenueLastHour", v))
                .to("output-revenue-1hour", 
                    Produced.with(
                        WindowedSerdes.timeWindowedSerdeFrom(String.class),
                        Serdes.String()
                    )
                );
 
        // ============= COUNTRY WITH HIGHEST SALES PER ITEM =============
        KTable<String, CountrySales> highestSalesCountry = sales
                .map((item, value) -> {
                    JsonObject json = gson.fromJson(value, JsonObject.class);
                    String country = json.get("country").getAsString();
                    double revenue = json.get("price").getAsDouble() * json.get("units").getAsInt();
                    return new KeyValue<>(item, new CountrySales(country, revenue));
                })
                .groupByKey(Grouped.with(Serdes.String(), countrySalesSerdes()))
                .reduce(
                        (cs1, cs2) -> cs1.sales >= cs2.sales ? cs1 : cs2,
                        Materialized.as("highest-sales-by-country")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(countrySalesSerdes())
                );
 
        highestSalesCountry.toStream()
                .mapValues(cs -> toJson("topCountry", cs.country + " (" + String.format("%.2f", cs.sales) + ")"))
                .to("output-highest-sales-country", Produced.with(Serdes.String(), Serdes.String()));
        **/
        return builder.build();
     
    }

    // ============= Helper Classes =============
    static class ItemProfit {
        public String item;
        public double profit;
 
        public ItemProfit(String item, double profit) {
            this.item = item;
            this.profit = profit;
        }
 
        public ItemProfit() {} // For deserialization
    }
 
    static class CountrySales {
        public String country;
        public double sales;
 
        public CountrySales(String country, double sales) {
            this.country = country;
            this.sales = sales;
        }
 
        public CountrySales() {} // For deserialization
    }
 
    // ============= Custom Serdes =============
    private static Serde<ItemProfit> itemProfitSerdes() {
        return Serdes.serdeFrom(
                new Serializer<ItemProfit>() {
                    private final Gson gson = new Gson();
                    @Override
                    public byte[] serialize(String topic, ItemProfit data) {
                        return gson.toJson(data).getBytes();
                    }
                },
                new Deserializer<ItemProfit>() {
                    private final Gson gson = new Gson();
                    @Override
                    public ItemProfit deserialize(String topic, byte[] data) {
                        return gson.fromJson(new String(data), ItemProfit.class);
                    }
                }
        );
    }
 
    private static Serde<CountrySales> countrySalesSerdes() {
        return Serdes.serdeFrom(
                new Serializer<CountrySales>() {
                    private final Gson gson = new Gson();
                    @Override
                    public byte[] serialize(String topic, CountrySales data) {
                        return gson.toJson(data).getBytes();
                    }
                },
                new Deserializer<CountrySales>() {
                    private final Gson gson = new Gson();
                    @Override
                    public CountrySales deserialize(String topic, byte[] data) {
                        return gson.fromJson(new String(data), CountrySales.class);
                    }
                }
        );
    }
 
    private static Serde<double[]> doubleArraySerdes() {
        return Serdes.serdeFrom(
                new Serializer<double[]>() {
                    private final Gson gson = new Gson();
                    @Override
                    public byte[] serialize(String topic, double[] data) {
                        return gson.toJson(data).getBytes();
                    }
                },
                new Deserializer<double[]>() {
                    private final Gson gson = new Gson();
                    @Override
                    public double[] deserialize(String topic, byte[] data) {
                        return gson.fromJson(new String(data), double[].class);
                    }
                }
        );
    }
 
    // ============= JSON Helper =============
    private static String toJson(String key, Object value) {
        JsonObject obj = new JsonObject();
        obj.addProperty(key, value.toString());
        return obj.toString();
    }
}
