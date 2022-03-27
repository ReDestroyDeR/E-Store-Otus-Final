package ru.red.productservice.config;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.red.product.avro.CommentKey;
import ru.red.product.avro.CommentValue;
import ru.red.product.avro.ProductAdded;
import ru.red.product.avro.ProductSubtracted;

import java.util.HashMap;

@EnableKafka
@Configuration
public class KafkaConfig {
    @Value("${kafka.bootstrapAddress}")
    private String bootstrapAddress;

    @Value("${kafka.schemaRegistryUrl}")
    private String schemaRegistryUrl;

    // Product Operations Kafka-Producer Configuration

    private <T> ProducerFactory<String, T> kafkaProducerFactory() {

        var properties = new HashMap<String, Object>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        properties.put(AbstractKafkaSchemaSerDeConfig.VALUE_SUBJECT_NAME_STRATEGY, TopicRecordNameStrategy.class);
        properties.put("schema.registry.url", schemaRegistryUrl);

        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean("product-added-kafka-template")
    public KafkaTemplate<String, ProductAdded> productAddedKafkaTemplate() {
        return new KafkaTemplate<>(kafkaProducerFactory());
    }

    @Bean("product-reserved-kafka-template")
    public KafkaTemplate<String, ProductSubtracted> productSubtractedKafkaTemplate() {
        return new KafkaTemplate<>(kafkaProducerFactory());
    }

    // Comment Kafka-Producer Configuration

    @Bean("comment-producer-factory")
    public ProducerFactory<CommentKey, CommentValue> commentProducerFactory() {

        var properties = new HashMap<String, Object>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        properties.put(ProducerConfig.ACKS_CONFIG, "1");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        properties.put("schema.registry.url", schemaRegistryUrl);

        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean("comment-kafka-template")
    public KafkaTemplate<CommentKey, CommentValue> commentKafkaTemplate() {
        return new KafkaTemplate<>(commentProducerFactory());
    }
}
