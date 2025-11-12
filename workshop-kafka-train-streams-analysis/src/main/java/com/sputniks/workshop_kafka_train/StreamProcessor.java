package com.sputniks.workshop_kafka_train;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

import java.time.Duration;


/**
 * This StreamProcessor class defines the Kafka Streams topology that processes incoming train location data in real time, 
 * calculates the average speed per train every 10 seconds, and sends this information to another topic.
 */
@Component
public class StreamProcessor {

    /**
     * This is a simple accumulator used to:
     * - Track how many speed samples we have seen (count)
     * - Accumulate the total sum of speeds (sum)
     * - Compute the average when needed (sum / count)
     * Kafka Streams requires stateful aggregation functions to be serializable and public, hence this is a public static class.
     */
    public static class AverageAggregator {
        public int count = 0;
        public long sum = 0;

        public AverageAggregator add(int speed) {
            count++;
            sum += speed;
            return this;
        }

        public double computeAverage() {
            if (count == 0) return 0.0;
            return (double) sum / count;
        }
    }

    @Autowired
    public void buildTopology(StreamsBuilder builder) {
        /* JsonSerde is used to serialize/deserialize our custom objects to/from JSON. */
        JsonSerde<TrainPosition> positionSerde = new JsonSerde<>(TrainPosition.class);
        JsonSerde<TrainAverageSpeed> avgSpeedSerde = new JsonSerde<>(TrainAverageSpeed.class);
        JsonSerde<AverageAggregator> aggregatorSerde = new JsonSerde<>(AverageAggregator.class);

        /* Stream from the input topic: the key is trainId */
        KStream<String, TrainPosition> inputStream = builder.stream(
            "train-locations",
            Consumed.with(Serdes.String(), positionSerde)
        );

        /* 
         * Group by key (trainId), create 10-second tumbling windows, and aggregate
         * - Group by key: each train's messages are grouped by their ID (trainId).
         * - Apply a 10-second tumbling window: time is split into 10-second non-overlapping intervals.
         * - Aggregate:
         *     - AverageAggregator::new: creates a fresh accumulator per window.
         *     - .add(value.speedKph()): adds the speed to the accumulator.
         *     - Materialized.with(...): stores state in memory (backed by RocksDB or in-memory store).
         * - mapValues(...): once the aggregation is done, compute the average from the accumulator.
         * At this point, you have a KTable<Windowed<String>, Double> containing average speeds per train ID every 2 seconds.
        */
        KTable<Windowed<String>, Double> averageSpeedTable = inputStream
            .groupByKey(Grouped.with(Serdes.String(), positionSerde))
            .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(2)))
            .aggregate(
                AverageAggregator::new,
                (key, value, aggregate) -> aggregate.add(value.speedKph()),
                Materialized.with(Serdes.String(), aggregatorSerde)
            )
            .mapValues(AverageAggregator::computeAverage);

        /*
         * KTable is a changelog of averages per window.
         * Convert the KTable back to a KStream to write to the output topic 
        */
        KStream<String, Double> averageSpeedStream = averageSpeedTable.toStream()
            .map((windowedKey, value) -> new KeyValue<>(windowedKey.key(), value));

        /*
         * Each record now becomes a TrainAverageSpeed object.
         * This is the domain object that wraps trainId and average speed, meant for downstream consumers.
         */
        KStream<String, TrainAverageSpeed> outputStream = averageSpeedStream
            .mapValues((readOnlyKey, value) -> new TrainAverageSpeed(readOnlyKey, value));

        /* Write to the output topic: the key is trainId and the value is averageSpeed */
        outputStream.to("train-speed-averages", Produced.with(Serdes.String(), avgSpeedSerde));
    }
}