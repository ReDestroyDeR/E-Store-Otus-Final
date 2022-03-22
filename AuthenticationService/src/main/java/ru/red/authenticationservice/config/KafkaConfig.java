package ru.red.authenticationservice.config;

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
import ru.red.avro.ValueUserManipulation;

import java.util.HashMap;

@EnableKafka
@Configuration
public class KafkaConfig {
    @Bean(name = "user-manipulation-producer-factory")
    public ProducerFactory<String, ValueUserManipulation> kafkaProducerFactory(
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

    @Bean
    public KafkaTemplate<String, ValueUserManipulation> kafkaTemplate(
            @Value("${kafka.bootstrapServers}") String bootstrapServers,
            @Value("${kafka.schemaRegistryUrl}") String schemaRegistryUrl) {
        return new KafkaTemplate<>(kafkaProducerFactory(bootstrapServers, schemaRegistryUrl));
    }
}

