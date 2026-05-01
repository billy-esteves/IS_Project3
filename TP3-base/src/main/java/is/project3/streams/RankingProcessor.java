package is.project3.streams;


import com.google.gson.Gson;
import is.project3.models.ItemStats;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;

public class RankingProcessor {

    public static void build(StreamsBuilder builder) {

        Gson gson = new Gson();

        KStream<String, String> profitStream =
                builder.stream("profit-per-item");

        profitStream
                .mapValues((ValueMapper<String, ItemStats>) v -> gson.fromJson(v, ItemStats.class))
                .groupBy((key, value) -> "max")
                .reduce((v1, v2) ->
                        v1.getValue() > v2.getValue() ? v1 : v2
                )
                .toStream()
                .mapValues((ValueMapper<ItemStats, String>) gson::toJson)
                .to("highest-profit");
    }
}