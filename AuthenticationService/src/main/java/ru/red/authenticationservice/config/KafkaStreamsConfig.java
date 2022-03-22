package ru.red.authenticationservice.config;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@EnableKafka
@EnableKafkaStreams
@Configuration
public class KafkaStreamsConfig {
    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kafkaStreamsConfiguration(
            @Value("${kafka.bootstrapServers}") String bootstrapServers,
            @Value("${kafka.applicationId}") String applicationId,
            @Value("${kafka.schemaRegistryUrl}") String schemaRegistryUrl) {

        var properties = new HashMap<String, Object>();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put("schema.registry.url", schemaRegistryUrl);

        return new KafkaStreamsConfiguration(properties);
    }

    @Bean("serde-config")
    public Map<String, String> serdeConfig(@Value("${kafka.schemaRegistryUrl}") String schemaRegistryUrl) {
        return Collections.singletonMap("schema.registry.url", schemaRegistryUrl);
    }
}

