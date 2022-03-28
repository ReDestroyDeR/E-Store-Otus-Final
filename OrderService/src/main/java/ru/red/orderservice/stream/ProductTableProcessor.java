package ru.red.orderservice.stream;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.Getter;
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
import ru.red.product.avro.ProductTableValue;

import java.util.Map;

@Component
@Profile("kafka-streams")
public class ProductTableProcessor {
    private static final Serde<ProductTableValue> PRODUCT_TABLE_VALUE_SERDE = new SpecificAvroSerde<>();

    private static final String CHANGELOG_NAME = "product-service-product-store-changelog";

    private static final String STORE_NAME = "product-store";

    @Getter
    private String productStoreName;

    public ProductTableProcessor(@Qualifier("serde-config-table-record") Map<String, String> serdeTableRecordConfig) {
        PRODUCT_TABLE_VALUE_SERDE.configure(serdeTableRecordConfig, false);
    }

    @Autowired
    void buildPipeline(StreamsBuilder builder) {
        KTable<String, ProductTableValue> productKTable = builder.table(CHANGELOG_NAME,
                Consumed.with(Serdes.String(), PRODUCT_TABLE_VALUE_SERDE),
                Materialized.as(STORE_NAME));

        this.productStoreName = productKTable.queryableStoreName();
    }
}
