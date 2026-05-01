package is.project3.streams;


import com.google.gson.Gson;
import is.project3.models.ItemStats;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;

public class ProfitProcessor {

    public static void build(StreamsBuilder builder) {

        Gson gson = new Gson();

        KTable<String, String> revenueRaw =
                builder.table("revenue-per-item");

        KTable<String, String> expenseRaw =
                builder.table("expenses-per-item");

        KTable<String, ItemStats> profitTable =
                revenueRaw.join(
                        expenseRaw,
                        (revJson, expJson) -> {

                            ItemStats revenue = gson.fromJson(revJson, ItemStats.class);
                            ItemStats expense = gson.fromJson(expJson, ItemStats.class);

                            return new ItemStats(
                                    revenue.getItem(),
                                    revenue.getValue() - expense.getValue()
                            );
                        }
                );

        profitTable
                .toStream()
                .mapValues((ItemStats v) -> gson.toJson(v))
                .to("profit-per-item");
    }
}