package is.project3.streams;

import com.google.gson.Gson;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;

import is.project3.models.AggregateStats;
import is.project3.models.ItemStats;

public class AggregateStatsProcessor {

    public static void build(StreamsBuilder builder) {

        Gson gson = new Gson();

        KStream<String, String> revenueStream = builder.stream("revenue-per-item");
        KStream<String, String> expenseStream = builder.stream("expenses-per-item");
        KStream<String, String> profitStream = builder.stream("profit-per-item");

        // -------------------------
        // TOTAL REVENUE
        // -------------------------
        KTable<String, AggregateStats> revenueAgg =
                revenueStream
                        .mapValues((ValueMapper<String, ItemStats>) v ->
                                gson.fromJson(v, ItemStats.class)
                        )
                        .groupBy(
                                (key, value) -> "total",
                                Grouped.with(Serdes.String(), Serdes.serdeFrom(
                                        (topic, data) -> gson.toJson(data).getBytes(),
                                        (topic, bytes) -> gson.fromJson(new String(bytes), ItemStats.class)
                                ))
                        )
                        .aggregate(
                                AggregateStats::new,
                                (key, value, agg) -> {
                                    agg.setTotalRevenue(agg.getTotalRevenue() + value.getValue());
                                    return agg;
                                }
                        );

        revenueAgg
                .toStream()
                .mapValues(v -> gson.toJson(v))
                .to("total-revenue");

        // -------------------------
        // TOTAL EXPENSES
        // -------------------------
        KTable<String, AggregateStats> expenseAgg =
                expenseStream
                        .mapValues((ValueMapper<String, ItemStats>) v ->
                                gson.fromJson(v, ItemStats.class)
                        )
                        .groupBy(
                                (key, value) -> "total"
                        )
                        .aggregate(
                                AggregateStats::new,
                                (key, value, agg) -> {
                                    agg.setTotalExpenses(agg.getTotalExpenses() + value.getValue());
                                    return agg;
                                }
                        );

        expenseAgg
                .toStream()
                .mapValues(v -> gson.toJson(v))
                .to("total-expenses");

        // -------------------------
        // TOTAL PROFIT
        // -------------------------
        KTable<String, AggregateStats> profitAgg =
                profitStream
                        .mapValues((ValueMapper<String, ItemStats>) v ->
                                gson.fromJson(v, ItemStats.class)
                        )
                        .groupBy(
                                (key, value) -> "total"
                        )
                        .aggregate(
                                AggregateStats::new,
                                (key, value, agg) -> {
                                    agg.setTotalProfit(agg.getTotalProfit() + value.getValue());
                                    return agg;
                                }
                        );

        profitAgg
                .toStream()
                .mapValues(v -> gson.toJson(v))
                .to("total-profit");
    }
}