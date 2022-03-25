package ru.red.productservice.stream;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.Getter;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.red.avro.CommentKey;
import ru.red.avro.CommentValue;
import ru.red.avro.ProductOpsValue;
import ru.red.avro.ProductTableValue;

import java.util.Map;

@Component
@Profile("kafka-streams")
public class ProductProcessor {
    private static final Serde<ProductOpsValue> PRODUCT_OPS_VALUE_SERDE = new SpecificAvroSerde<>();

    private static final Serde<ProductTableValue> PRODUCT_TABLE_VALUE_SERDE = new SpecificAvroSerde<>();

    private static final Serde<CommentKey> COMMENT_KEY_SERDE = new SpecificAvroSerde<>();
    private static final Serde<CommentValue> COMMENT_VALUE_SERDE = new SpecificAvroSerde<>();

    @Getter
    private String productStockStateStoreName;

    @Autowired
    public ProductProcessor(@Qualifier("serde-config") Map<String, String> serdeConfig) {
        PRODUCT_OPS_VALUE_SERDE.configure(serdeConfig, false);

        PRODUCT_TABLE_VALUE_SERDE.configure(serdeConfig, false);

        COMMENT_KEY_SERDE.configure(serdeConfig, true);
        COMMENT_VALUE_SERDE.configure(serdeConfig, false);
    }

    @Autowired
    void buildPipeline(StreamsBuilder builder) {
        KTable<String, ProductTableValue> products = builder.stream("product-ops",
                        Consumed.with(Serdes.String(), PRODUCT_OPS_VALUE_SERDE))
                .groupByKey()
                .aggregate(ProductTableValue::new,
                        ((key, value, aggregate) -> {
                            if (value == null) {
                                return null;
                            }

                            if (value.getPricePerUnit() != 0) {
                                aggregate.setPrice(value.getPricePerUnit());
                            }
                            aggregate.setQuantity(aggregate.getQuantity() + value.getStockDelta());
                            return aggregate;
                        }),
                        Materialized.<String, ProductTableValue, KeyValueStore<Bytes, byte[]>>as("product-stock-table")
                                .withValueSerde(PRODUCT_TABLE_VALUE_SERDE));

        products.toStream().to("product-stock-stream");

        this.productStockStateStoreName = products.queryableStoreName();
    }
}
