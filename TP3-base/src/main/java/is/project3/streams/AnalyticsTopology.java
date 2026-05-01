package is.project3.streams;

import org.apache.kafka.streams.kstream.Produced;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;

import is.project3.config.KafkaConfig;
import is.project3.util.TopicNames;

import java.time.Duration;

public class AnalyticsTopology {

    private static final Gson gson = new Gson();

    public static Topology build() {

        StreamsBuilder builder = new StreamsBuilder();

        // INPUT STREAMS
        KStream<String, String> sales = builder.stream("sales-topic");
        KStream<String, String> purchases = builder.stream("purchases-topic");

        // REVENUE PER ITEM
        KTable<String, Double> revenuePerItem =
                sales
                        .mapValues(value -> {
                            JsonObject json = gson.fromJson(value, JsonObject.class);
                            return json.get("price").getAsDouble()
                                    * json.get("units").getAsInt();
                        })
                        .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
                        .reduce(Double::sum);

        revenuePerItem.toStream()
                .mapValues(v -> json("revenue", v))
                .to("revenue-per-item", Produced.with(Serdes.String(), Serdes.String()));

        // EXPENSES PER ITEM
        KTable<String, Double> expensePerItem =
                purchases
                        .mapValues(value -> {
                            JsonObject json = gson.fromJson(value, JsonObject.class);
                            return json.get("price").getAsDouble()
                                    * json.get("units").getAsInt();
                        })
                        .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
                        .reduce(Double::sum);

        expensePerItem.toStream()
                .mapValues(v -> json("expense", v))
                .to("Proj3TotalExpensesOutputStreamsTopic", Produced.with(Serdes.String(), Serdes.String()));

        // PROFIT PER ITEM (JOIN)
        // profit = revenue - expense
        KTable<String, Double> profitPerItem =
                revenuePerItem.join(
                        expensePerItem,
                        (rev, exp) -> rev - exp
                );

        profitPerItem.toStream()
                .mapValues(v -> json("profit", v))
                .to("Proj3TotalProfitOutputStreamsTopic", Produced.with(Serdes.String(), Serdes.String()));

        // TOTALS (ALL ITEMS COMBINED)
        KTable<String, Double> totalRevenue =
        revenuePerItem
                .toStream()
                .groupBy((k, v) -> "total", Grouped.with(Serdes.String(), Serdes.Double()))
                .reduce(Double::sum);

        KTable<String, Double> totalExpenses =
        expensePerItem
                .toStream()
                .groupBy((k, v) -> "total", Grouped.with(Serdes.String(), Serdes.Double()))
                .reduce(Double::sum);

        KTable<String, Double> totalProfit =
                totalRevenue.join(totalExpenses, (r, e) -> r - e);

        totalRevenue.toStream()
                .mapValues(v -> json("totalRevenue", v))
                .to("total-revenue", Produced.with(Serdes.String(), Serdes.String()));

        totalExpenses.toStream()
                .mapValues(v -> json("totalExpenses", v))
                .to("Proj3TotalExpensesOutputStreamsTopic", Produced.with(Serdes.String(), Serdes.String()));

        totalProfit.toStream()
                .mapValues(v -> json("totalProfit", v))
                .to("Proj3TotalProfitOutputStreamsTopic", Produced.with(Serdes.String(), Serdes.String()));

        // WINDOWED REVENUE (1 HOUR)
        revenuePerItem
                .toStream()
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
                .windowedBy(TimeWindows.of(Duration.ofHours(1)))
                .reduce(Double::sum)
                .toStream()
                .mapValues(v -> json("windowedRevenue", v))
                .to(
                        "Proj3TotalRevenueWindowedOutputStreamsTopic",
                        Produced.with(
                        WindowedSerdes.timeWindowedSerdeFrom(String.class),
                        Serdes.String()
                        )
                );

        // HIGHEST PROFIT ITEM
        profitPerItem
                .toStream()
                .groupBy((k, v) -> "global")
                .reduce((a, b) -> a > b ? a : b)
                .toStream()
                .mapValues(v -> json("highestProfit", v))
                .to("Proj3HighestProfitOutputStreamsTopic", Produced.with(Serdes.String(), Serdes.String()));

        return builder.build();
    }

    // JSON helper
    private static String json(String type, double value) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", type);
        obj.addProperty("value", value);
        return gson.toJson(obj);
    }
}