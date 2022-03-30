package ru.red.deliveryservice.streams;

import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.red.billing.avro.OrderAcknowledged;
import ru.red.billing.avro.OrderAcknowledgmentKey;
import ru.red.delivery.avro.Address;
import ru.red.delivery.avro.DeliveryAggregate;
import ru.red.delivery.avro.DeliveryFinished;
import ru.red.delivery.avro.DeliveryScheduled;
import ru.red.delivery.avro.DeliveryStarted;
import ru.red.delivery.avro.DeliveryStatus;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@Component
@Profile("kafka-streams")
public class OrderProcessor {
    private static final Serde<OrderAcknowledgmentKey> ORDER_ACKNOWLEDGMENT_KEY_SERDE = new SpecificAvroSerde<>();
    private static final Serde<OrderAcknowledged> ORDER_ACKNOWLEDGED_SERDE = new SpecificAvroSerde<>();

    private static final Serde<DeliveryScheduled> DELIVERY_SCHEDULED_SERDE = new SpecificAvroSerde<>();
    private static final Serde<DeliveryStarted> DELIVERY_STARTED_SERDE = new SpecificAvroSerde<>();
    private static final Serde<DeliveryFinished> DELIVERY_FINISHED_SERDE = new SpecificAvroSerde<>();
    private static final Serde<DeliveryAggregate> DELIVERY_AGGREGATE_SERDE = new SpecificAvroSerde<>();

    private static final Serde<GenericRecord> GENERIC_VALUE_SERDE = new GenericAvroSerde();

    private static final String ORDER_ACKNOWLEDGMENT_TOPIC_NAME = "order-acknowledgment";
    private static final String DELIVERY_TOPIC_NAME = "delivery";

    @Getter
    private final String deliveryAggregateStoreName = "delivery-aggregate-store";

    @Autowired
    public OrderProcessor(@Qualifier("serde-topic-record-config") Map<String, String> serdeTopicRecordConfig) {
        ORDER_ACKNOWLEDGMENT_KEY_SERDE.configure(serdeTopicRecordConfig, false);
        ORDER_ACKNOWLEDGED_SERDE.configure(serdeTopicRecordConfig, false);

        DELIVERY_SCHEDULED_SERDE.configure(serdeTopicRecordConfig, false);
        DELIVERY_STARTED_SERDE.configure(serdeTopicRecordConfig, false);
        DELIVERY_FINISHED_SERDE.configure(serdeTopicRecordConfig, false);
        DELIVERY_AGGREGATE_SERDE.configure(serdeTopicRecordConfig, false);

        GENERIC_VALUE_SERDE.configure(serdeTopicRecordConfig, false);
    }

    @Autowired
    void buildPipeline(StreamsBuilder builder) {
        var deliveryStatusesKTable = builder.stream(DELIVERY_TOPIC_NAME,
                        Consumed.with(ORDER_ACKNOWLEDGMENT_KEY_SERDE, Serdes.Bytes()))
                .groupByKey()
                .aggregate(DeliveryAggregate::new, (key, bytes, aggregate) -> {
                    final var genericValue = GENERIC_VALUE_SERDE.deserializer()
                            .deserialize(DELIVERY_TOPIC_NAME, bytes.get());
                    final var schemaName = genericValue.getSchema().getFullName();
                    aggregate.setAddress(new Address("", "", "", -1));
                    aggregate.setItems(new HashMap<>());
                    if (schemaName.equals(DeliveryStarted.getClassSchema().getFullName())) {
                        aggregate.setStatus(DeliveryStatus.STARTED);
                    } else if (schemaName.equals(DeliveryFinished.getClassSchema().getFullName())) {
                        aggregate.setStatus(DeliveryStatus.FINISHED);
                    }

                    return aggregate;
                }, Materialized.<OrderAcknowledgmentKey, DeliveryAggregate, KeyValueStore<Bytes, byte[]>>as("delivery-operations-aggregate")
                        .withKeySerde(ORDER_ACKNOWLEDGMENT_KEY_SERDE)
                        .withValueSerde(DELIVERY_AGGREGATE_SERDE));

        var deliveryAggregateKTable = builder.stream(ORDER_ACKNOWLEDGMENT_TOPIC_NAME,
                        Consumed.with(ORDER_ACKNOWLEDGMENT_KEY_SERDE, Serdes.Bytes()))
                .filterNot((key, bytes) -> bytes == null)
                .filter((key, bytes) -> GENERIC_VALUE_SERDE.deserializer()
                        .deserialize(ORDER_ACKNOWLEDGMENT_TOPIC_NAME, bytes.get()).getSchema().getFullName()
                        .equals(OrderAcknowledged.getClassSchema().getFullName()))
                .mapValues(bytes -> ORDER_ACKNOWLEDGED_SERDE.deserializer()
                        .deserialize(ORDER_ACKNOWLEDGMENT_TOPIC_NAME, bytes.get()))
                .groupByKey()
                .aggregate(DeliveryAggregate::new,
                        (key, value, aggregate) -> {
                            aggregate.setStatus(DeliveryStatus.SCHEDULED);
                            aggregate.setAddress(new Address("Moscow", "Test st.", "404", 402)); // TODO: Address is a part of an order // NOSONAR
                            aggregate.setItems(value.getItems());
                            return aggregate;
                        },
                        Materialized.<OrderAcknowledgmentKey, DeliveryAggregate, KeyValueStore<Bytes, byte[]>>as("delivery-scheduled-aggregate")
                                .withKeySerde(ORDER_ACKNOWLEDGMENT_KEY_SERDE)
                                .withValueSerde(DELIVERY_AGGREGATE_SERDE))
                .leftJoin(deliveryStatusesKTable, (our, their) -> {
                    if (their == null)
                        return our;

                    our.setStatus(their.getStatus());
                    return our;
                }, Materialized.<OrderAcknowledgmentKey, DeliveryAggregate, KeyValueStore<Bytes, byte[]>>as(deliveryAggregateStoreName)
                        .withKeySerde(ORDER_ACKNOWLEDGMENT_KEY_SERDE)
                        .withValueSerde(DELIVERY_AGGREGATE_SERDE));
    }
}
