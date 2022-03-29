package ru.red.authenticationservice.config;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.red.auth.avro.UserCreated;
import ru.red.auth.avro.UserDeleted;
import ru.red.auth.avro.UserUpdatedEmail;

import java.util.HashMap;

@EnableKafka
@Configuration
public class KafkaConfig {
    @Value("${kafka.bootstrapServers}")
    private String bootstrapServers;

    @Value("${kafka.schemaRegistryUrl}")
    private String schemaRegistryUrl;

    private <T> ProducerFactory<Long, T> kafkaProducerFactory() {
        var properties = new HashMap<String, Object>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        properties.put(AbstractKafkaSchemaSerDeConfig.VALUE_SUBJECT_NAME_STRATEGY, TopicRecordNameStrategy.class);
        properties.put("schema.registry.url", schemaRegistryUrl);

        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean("user-created-kafka-template")
    public KafkaTemplate<Long, UserCreated> userCreatedKafkaTemplate() {
        return new KafkaTemplate<>(kafkaProducerFactory());
    }

    @Bean("user-updated-email-kafka-template")
    public KafkaTemplate<Long, UserUpdatedEmail> userUpdatedEmailKafkaTemplate() {
        return new KafkaTemplate<>(kafkaProducerFactory());
    }

    @Bean("user-deleted-kafka-template")
    public KafkaTemplate<Long, UserDeleted> userDeletedKafkaTemplate() {
        return new KafkaTemplate<>(kafkaProducerFactory());
    }
}

