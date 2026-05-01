package is.project3.streams;


import com.google.gson.Gson;
import is.project3.models.AggregateStats;
import is.project3.models.ItemStats;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;

public class AggregateStatsProcessor {

    public static void build(StreamsBuilder builder) {

        Gson gson = new Gson();

        KStream<String, String> revenueStream = builder.stream("revenue-per-item");
        KStream<String, String> expenseStream = builder.stream("expenses-per-item");
        KStream<String, String> profitStream = builder.stream("profit-per-item");

        // TOTAL REVENUE
        revenueStream
                .mapValues((ValueMapper<String, ItemStats>) v -> gson.fromJson(v, ItemStats.class))
                .groupBy((k, v) -> "total")
                .aggregate(
                        AggregateStats::new,
                        (key, value, agg) -> {
                            agg.setTotalRevenue(agg.getTotalRevenue() + value.getValue());
                            return agg;
                        }
                )
                .toStream()
                .mapValues((ValueMapper<AggregateStats, String>) gson::toJson)
                .to("total-revenue");

        // TOTAL EXPENSES
        expenseStream
                .mapValues((ValueMapper<String, ItemStats>) v -> gson.fromJson(v, ItemStats.class))
                .groupBy((k, v) -> "total")
                .aggregate(
                        AggregateStats::new,
                        (key, value, agg) -> {
                            agg.setTotalExpenses(agg.getTotalExpenses() + value.getValue());
                            return agg;
                        }
                )
                .toStream()
                .mapValues((ValueMapper<AggregateStats, String>) gson::toJson)
                .to("total-expenses");

        // TOTAL PROFIT
        profitStream
                .mapValues((ValueMapper<String, ItemStats>) v -> gson.fromJson(v, ItemStats.class))
                .groupBy((k, v) -> "total")
                .aggregate(
                        AggregateStats::new,
                        (key, value, agg) -> {
                            agg.setTotalProfit(agg.getTotalProfit() + value.getValue());
                            return agg;
                        }
                )
                .toStream()
                .mapValues((ValueMapper<AggregateStats, String>) gson::toJson)
                .to("total-profit");
    }
}