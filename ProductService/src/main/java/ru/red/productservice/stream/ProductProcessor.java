package ru.red.productservice.stream;

import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.Getter;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.red.product.avro.CommentKey;
import ru.red.product.avro.CommentValue;
import ru.red.product.avro.ProductAdded;
import ru.red.product.avro.ProductReserved;
import ru.red.product.avro.ProductSubtracted;
import ru.red.product.avro.ProductTableValue;
import ru.red.product.avro.ProductUnreserved;

import java.util.Map;

@Component
@Profile("kafka-streams")
public class ProductProcessor {
    private static final Serde<ProductAdded> PRODUCT_ADDED_VALUE_SERDE = new SpecificAvroSerde<>();
    private static final Serde<ProductSubtracted> PRODUCT_SUBTRACTED_VALUE_SERDE = new SpecificAvroSerde<>();
    private static final Serde<ProductReserved> PRODUCT_RESERVED_VALUE_SERDE = new SpecificAvroSerde<>();
    private static final Serde<ProductUnreserved> PRODUCT_UNRESERVED_VALUE_SERDE = new SpecificAvroSerde<>();

    private static final Serde<ProductTableValue> PRODUCT_TABLE_VALUE_SERDE = new SpecificAvroSerde<>();

    private static final Serde<CommentKey> COMMENT_KEY_SERDE = new SpecificAvroSerde<>();
    private static final Serde<CommentValue> COMMENT_VALUE_SERDE = new SpecificAvroSerde<>();

    private static final Serde<GenericRecord> GENERIC_VALUE_SERDE = new GenericAvroSerde();

    private static final String TOPIC_NAME = "product-ops";
    private static final String STORE_NAME = "product-store";

    @Getter
    private String productStockStateStoreName;

    @Autowired
    public ProductProcessor(
            @Qualifier("serde-config") Map<String, String> serdeConfig,
            @Qualifier("serde-config-topic-record") Map<String, String> serdeTopicRecordConfig
    ) {
        PRODUCT_ADDED_VALUE_SERDE.configure(serdeTopicRecordConfig, false);
        PRODUCT_SUBTRACTED_VALUE_SERDE.configure(serdeTopicRecordConfig, false);
        PRODUCT_RESERVED_VALUE_SERDE.configure(serdeTopicRecordConfig, false);
        PRODUCT_UNRESERVED_VALUE_SERDE.configure(serdeTopicRecordConfig, false);

        PRODUCT_TABLE_VALUE_SERDE.configure(serdeConfig, false);

        COMMENT_KEY_SERDE.configure(serdeConfig, true);
        COMMENT_VALUE_SERDE.configure(serdeConfig, false);

        GENERIC_VALUE_SERDE.configure(serdeConfig, false);
    }

    // We don't do exception handling. We suppose stream data is already state-valid
    // TODO: TEST DISTRIBUTED STATE STORE BEHAVIOUR WITH INVALID DATA // NOSONAR
    @Autowired
    void buildPipeline(StreamsBuilder builder) {
        final String productAddedSchemaFullname = ProductAdded.getClassSchema().getFullName();
        final String productSubtractedSchemaFullname = ProductSubtracted.getClassSchema().getFullName();
        final String productReservedSchemaFullname = ProductReserved.getClassSchema().getFullName();
        final String productUnreservedSchemaFullname = ProductUnreserved.getClassSchema().getFullName();

        KTable<String, ProductTableValue> products = builder.stream(TOPIC_NAME,
                        Consumed.with(Serdes.String(), Serdes.Bytes()))
                .groupByKey()
                .aggregate(ProductTableValue::new, (key, bytes, aggregate) -> {
                    // Tombstone check
                    if (bytes.get() == null) {
                        return null;
                    }

                    final String genericRecordSchemaFullName = GENERIC_VALUE_SERDE.deserializer()
                            .deserialize(TOPIC_NAME, bytes.get())
                            .getSchema()
                            .getFullName();

                    // What event type are we deserializing
                    if (productAddedSchemaFullname.equals(genericRecordSchemaFullName)) {
                        // ProductAdded
                        var value = PRODUCT_ADDED_VALUE_SERDE.deserializer()
                                .deserialize(TOPIC_NAME, bytes.get());

                        if (value.getPricePerUnit() != 0) {
                            aggregate.setPrice(value.getPricePerUnit());
                        }
                        aggregate.setQuantity(aggregate.getQuantity() + value.getAdded());
                    } else if (productSubtractedSchemaFullname.equals(genericRecordSchemaFullName)) {
                        // ProductSubtracted
                        var value = PRODUCT_SUBTRACTED_VALUE_SERDE.deserializer()
                                .deserialize(TOPIC_NAME, bytes.get());

                        aggregate.setQuantity(aggregate.getQuantity() - value.getSubtracted());
                    } else if (productReservedSchemaFullname.equals(genericRecordSchemaFullName)) {
                        // ProductReserved
                        var value = PRODUCT_RESERVED_VALUE_SERDE.deserializer()
                                .deserialize(TOPIC_NAME, bytes.get());

                        aggregate.setQuantity(aggregate.getQuantity() - value.getReserved());
                        aggregate.setReserved(aggregate.getReserved() + value.getReserved());
                    } else if (productUnreservedSchemaFullname.equals(genericRecordSchemaFullName)) {
                        // ProductUnreserved
                        var value = PRODUCT_UNRESERVED_VALUE_SERDE.deserializer()
                                .deserialize(TOPIC_NAME, bytes.get());

                        aggregate.setQuantity(aggregate.getQuantity() + value.getUnreserved());
                        aggregate.setReserved(aggregate.getReserved() - value.getUnreserved());
                    }

                    return aggregate;
                }, Materialized.as(STORE_NAME));

        this.productStockStateStoreName = products.queryableStoreName();
    }
}
