package ru.red.productservice.stream;

import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.red.order.avro.OrderCancelled;
import ru.red.order.avro.OrderManipulationKey;
import ru.red.order.avro.OrderPlaced;
import ru.red.productservice.service.ProductsStateService;

import java.util.Map;

@Component
public class OrderProcessor {

    private static final Serde<OrderManipulationKey> ODER_MANIPULATION_KEY_SERDE = new SpecificAvroSerde<>();
    private static final Serde<OrderPlaced> ORDER_PLACED_SERDE = new SpecificAvroSerde<>();
    private static final Serde<OrderCancelled> ORDER_CANCELLED_SERDE = new SpecificAvroSerde<>();

    private static final Serde<GenericRecord> GENERIC_VALUE_SERDE = new GenericAvroSerde();

    private static final String TOPIC_NAME = "order-manipulation";

    private final ProductsStateService stateService;


    @Autowired
    public OrderProcessor(@Qualifier("serde-config") Map<String, String> serdeConfig,
                          ProductsStateService stateService) {
        ODER_MANIPULATION_KEY_SERDE.configure(serdeConfig, true);
        ORDER_PLACED_SERDE.configure(serdeConfig, false);
        ORDER_CANCELLED_SERDE.configure(serdeConfig, false);

        GENERIC_VALUE_SERDE.configure(serdeConfig, false);

        this.stateService = stateService;
    }

    @Autowired
    void buildPipeline(StreamsBuilder builder) {
        KStream<OrderManipulationKey, Bytes> bytesStreamWithoutTombstones = builder.stream(TOPIC_NAME,
                        Consumed.with(ODER_MANIPULATION_KEY_SERDE, Serdes.Bytes()))
                .filterNot(((key, value) -> value == null)); // We don't care about tombstones

        // Order Placed processing
        bytesStreamWithoutTombstones.filter((key, bytes) -> GENERIC_VALUE_SERDE.deserializer()
                        .deserialize(TOPIC_NAME, bytes.get())
                        .getSchema().getFullName().equals(OrderPlaced.getClassSchema().getFullName()))
                .mapValues(bytes -> ORDER_PLACED_SERDE.deserializer().deserialize(TOPIC_NAME, bytes.get()))
                .split(Named.as("order-validation-split"))
                .branch(((key, value) -> {
                    // If any quantity of position in order is greater than in stock
                    try {
                        return value.getItems()
                                .entrySet()
                                .stream()
                                .anyMatch(entry -> stateService.getByNameWithoutComments(entry.getKey().toString())
                                        .getQuantity() < entry.getValue().getUnits());
                    } catch (NullPointerException ignored) { // Or product doesn't exist
                        return true;
                    }
                }), Branched.withConsumer(stream -> stream.to("invalid-orders"), "invalid-orders"))
                .defaultBranch(Branched.withConsumer(stream -> stream.to("valid-orders"), "valid-orders"));

        // TODO: Order Cancelled processing
//        bytesStreamWithoutTombstones.filter((key, bytes) -> GENERIC_VALUE_SERDE.deserializer()
//                        .deserialize(TOPIC_NAME, bytes.get())
//                        .getSchema().getFullName().equals(OrderCancelled.getClassSchema().getFullName()))
//                .mapValues(bytes -> ORDER_CANCELLED_SERDE.deserializer().deserialize(TOPIC_NAME, bytes.get()))
//                ...


    }
}
