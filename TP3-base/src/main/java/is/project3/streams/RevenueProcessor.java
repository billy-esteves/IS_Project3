package is.project3.streams;


import com.google.gson.Gson;
import is.project3.models.ItemStats;
import is.project3.models.Sale;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;

public class RevenueProcessor {

    public static void build(StreamsBuilder builder) {

        Gson gson = new Gson();

        KStream<String, String> salesStream = builder.stream("Sales");

        KTable<String, ItemStats> revenueTable =
                salesStream
                        .mapValues(value -> gson.fromJson(value, Sale.class))

                        // key = item
                        .selectKey((key, sale) -> sale.getItem())

                        // convert to ItemStats
                        .mapValues(sale -> new ItemStats(
                                                sale.getItem(),
                                                sale.getPrice() * sale.getUnits()
                                            )
                        )

                        .groupByKey()
                        .reduce((agg, newVal) -> new ItemStats(
                                                agg.getItem(),
                                                agg.getValue() + newVal.getValue()
                                            )
                        );

        revenueTable
                .toStream()
                .mapValues(gson::toJson)
                .to("revenue-per-item");
    }
}