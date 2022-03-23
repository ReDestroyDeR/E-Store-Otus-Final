package ru.red.productservice.config;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.red.avro.CommentKey;
import ru.red.avro.CommentValue;
import ru.red.avro.ProductOpsValue;

import java.util.HashMap;

@EnableKafka
@Configuration
public class KafkaConfig {
    // Product Operations Kafka-Producer Configuration

    @Bean("product-ops-producer-factory")
    public ProducerFactory<String, ProductOpsValue> productOpsProducerFactory(
            @Value("${kafka.bootstrapServers}") String bootstrapServers,
            @Value("${kafka.schemaRegistryUrl}") String schemaRegistryUrl) {

        var properties = new HashMap<String, Object>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        properties.put("schema.registry.url", schemaRegistryUrl);

        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean("product-ops-kafka-template")
    public KafkaTemplate<String, ProductOpsValue> productOpsKafkaTemplate(
            @Value("${kafka.bootstrapServers}") String bootstrapServers,
            @Value("${kafka.schemaRegistryUrl}") String schemaRegistryUrl) {
        return new KafkaTemplate<>(productOpsProducerFactory(bootstrapServers, schemaRegistryUrl));
    }

    // Comment Kafka-Producer Configuration

    @Bean("comment-producer-factory")
    public ProducerFactory<CommentKey, CommentValue> commentProducerFactory(
            @Value("${kafka.bootstrapServers}") String bootstrapServers,
            @Value("${kafka.schemaRegistryUrl}") String schemaRegistryUrl) {

        var properties = new HashMap<String, Object>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.ACKS_CONFIG, "1");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        properties.put("schema.registry.url", schemaRegistryUrl);

        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean("comment-kafka-template")
    public KafkaTemplate<CommentKey, CommentValue> commentKafkaTemplate(
            @Value("${kafka.bootstrapServers}") String bootstrapServers,
            @Value("${kafka.schemaRegistryUrl}") String schemaRegistryUrl) {
        return new KafkaTemplate<>(commentProducerFactory(bootstrapServers, schemaRegistryUrl));
    }
}
