package ru.red.deliveryservice.config;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.red.billing.avro.OrderAcknowledgmentKey;
import ru.red.delivery.avro.DeliveryFinished;
import ru.red.delivery.avro.DeliveryStarted;

import java.util.HashMap;

@Configuration
@EnableKafka
public class KafkaConfig {
    @Value("${kafka.bootstrapServers}")
    private String bootstrapServers;

    @Value("${kafka.schemaRegistryUrl}")
    private String schemaRegistryUrl;

    // Product Operations Kafka-Producer Configuration

    private <K, V> ProducerFactory<K, V> kafkaProducerFactory() {

        var properties = new HashMap<String, Object>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        properties.put(AbstractKafkaSchemaSerDeConfig.VALUE_SUBJECT_NAME_STRATEGY, TopicRecordNameStrategy.class);
        properties.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);

        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean("product-added-kafka-template")
    public KafkaTemplate<OrderAcknowledgmentKey, DeliveryStarted> productAddedKafkaTemplate() {
        return new KafkaTemplate<>(kafkaProducerFactory());
    }

    @Bean("product-reserved-kafka-template")
    public KafkaTemplate<OrderAcknowledgmentKey, DeliveryFinished> productSubtractedKafkaTemplate() {
        return new KafkaTemplate<>(kafkaProducerFactory());
    }
}

