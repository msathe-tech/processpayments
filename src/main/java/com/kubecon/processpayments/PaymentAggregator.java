package com.kubecon.processpayments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentAggregator {

    private static final Logger log = LoggerFactory.getLogger(PaymentAggregator.class);

    private final String inputTopic;
    private final String outputTopic;
    private final String storeName;

    public PaymentAggregator(@Value("${kafka.topic.input}") String inputTopic,
                           @Value("${kafka.topic.output}") String outputTopic,
                           @Value("${kafka.store.name}") String storeName) {
        this.inputTopic = inputTopic;
        this.outputTopic = outputTopic;
        this.storeName = storeName;
    }

    @Bean
    public Topology buildTopology(StreamsBuilder builder) {
        // Explicitly define Serdes for key/value types for clarity and to avoid
        // potential issues with default Serdes configuration.
        Materialized<String, Double, KeyValueStore<Bytes, byte[]>> materialized =
                Materialized.<String, Double, KeyValueStore<Bytes, byte[]>>as(storeName)
                        .withKeySerde(Serdes.String()).withValueSerde(Serdes.Double());

        // 1. Stream from the input topic, consuming values as Strings.
        KTable<String, Double> aggregatedTable = builder.stream(inputTopic, Consumed.with(Serdes.String(), Serdes.String()))
                // Print incoming messages for debugging.
                .peek((key, value) -> log.info("Incoming payment - CustomerID: {}, Amount: {}", key, value))
                // 2. Map values from String to Double, handling potential parsing errors.
                .mapValues(value -> {
                    try {
                        return Double.parseDouble(value);
                    } catch (NumberFormatException e) {
                        log.error("Could not parse double from value: '{}'", value, e);
                        return 0.0; // Or handle as a processing error
                    }
                })
                // 3. Group by customer key.
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
                // 4. Aggregate the payments to compute a running total.
                .aggregate(
                        () -> 0.0, /* Initializer for the total */
                        (key, payment, total) -> total + payment, /* Aggregator logic */
                        materialized /* Materialize the result in a state store */
                );

        // 5. Convert the result table to a stream for output.
        aggregatedTable.toStream()
                // 6. Round the aggregated value to two decimal places.
                .mapValues(value -> Math.round(value * 100.0) / 100.0)
                // 7. Print the aggregated value before sending to the output topic for verification.
                .peek((key, value) -> log.info("Aggregated total for CustomerID: {} is now: {}", key, value))
                // 8. Sink the output stream to the 'total.payments' topic.
                .to(outputTopic, Produced.with(Serdes.String(), Serdes.Double()));

        return builder.build();
    }
}
