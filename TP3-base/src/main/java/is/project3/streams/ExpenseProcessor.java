package is.project3.streams;


import com.google.gson.Gson;
import is.project3.models.ItemStats;
import is.project3.models.Purchase;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;

public class ExpenseProcessor {

    public static void build(StreamsBuilder builder) {

        Gson gson = new Gson();

        KStream<String, String> purchaseStream = builder.stream("Purchases");

        KTable<String, ItemStats> expenseTable =
                purchaseStream
                        .mapValues(value -> gson.fromJson(value, Purchase.class))

                        .selectKey((key, purchase) -> purchase.getItem())

                        .mapValues(purchase ->
                                new ItemStats(
                                        purchase.getItem(),
                                        purchase.getPrice() * purchase.getUnits()
                                )
                        )

                        .groupByKey()
                        .reduce((agg, newVal) ->
                                new ItemStats(
                                        agg.getItem(),
                                        agg.getValue() + newVal.getValue()
                                )
                        );

        expenseTable
                .toStream()
                .mapValues(gson::toJson)
                .to("expenses-per-item");
    }
}