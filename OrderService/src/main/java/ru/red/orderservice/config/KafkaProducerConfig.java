package ru.red.orderservice.config;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.red.order.avro.OrderCancelled;
import ru.red.order.avro.OrderManipulationKey;
import ru.red.order.avro.OrderPlaced;

import java.util.HashMap;

@Configuration
public class KafkaProducerConfig {
    @Value("${kafka.bootstrapAddress}")
    private String bootstrapAddress;

    @Value("${kafka.schema.registry.url}")
    private String schemaRegistryUrl;


    private <T> ProducerFactory<OrderManipulationKey, T> orderProducerFactory() {
        var props = new HashMap<String, Object>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<OrderManipulationKey, OrderPlaced> orderPlacedTemplate() {
        return new KafkaTemplate<>(orderProducerFactory());
    }

    @Bean
    public KafkaTemplate<OrderManipulationKey, OrderCancelled> orderCancelledTemplate() {
        return new KafkaTemplate<>(orderProducerFactory());
    }
}
