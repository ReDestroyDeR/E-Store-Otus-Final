package ru.red.deliveryservice.config;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafkaStreams
@Profile("kafka-streams")
public class KafkaStreamsConfig {
    @Value("${kafka.bootstrapServers}")
    private String bootstrapServers;

    @Value("${kafka.applicationId}")
    private String applicationId;

    @Value("${kafka.schemaRegistryUrl}")
    private String schemaRegistryUrl;

    @Bean("defaultKafkaStreamsConfig")
    public KafkaStreamsConfiguration kafkaStreamsConfiguration() {
        var properties = new HashMap<String, Object>();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        properties.put(AbstractKafkaSchemaSerDeConfig.VALUE_SUBJECT_NAME_STRATEGY, TopicRecordNameStrategy.class);
        properties.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);

        return new KafkaStreamsConfiguration(properties);
    }

    @Bean("serde-topic-record-config")
    public Map<String, String> serdeTopicRecordConfig() {
        return Map.of(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl,
                AbstractKafkaSchemaSerDeConfig.VALUE_SUBJECT_NAME_STRATEGY, TopicRecordNameStrategy.class.getName());
    }

    @Bean("serde-config")
    public Map<String, String> serdeConfig() {
        return Collections.singletonMap(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
    }
}

