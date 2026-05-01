package is.project3.streams;

import com.google.gson.Gson;
import is.project3.models.ItemStats;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;

import java.time.Duration;

public class WindowedStatsProcessor {

    public static void build(StreamsBuilder builder) {

        Gson gson = new Gson();

        KStream<String, String> revenueStream =
                builder.stream("revenue-per-item");

        revenueStream
                .mapValues((ValueMapper<String, ItemStats>) v -> gson.fromJson(v, ItemStats.class))
                .groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofHours(1)))
                .aggregate(
                        () -> 0.0,
                        (key, value, agg) -> agg + value.getValue()
                )
                .toStream()
                .mapValues((ValueMapper<Object, String>) gson::toJson)
                .to("windowed-revenue");
    }
}