package ru.red.orderservice.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;

@Configuration
public class NewTopicConfig {
    @Bean
    public KafkaAdmin admin(@Value("${kafka.bootstrapAddress}") String bootstrapServers) {
        var properties = new HashMap<String, Object>();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(properties);
    }

    @Bean("valid-orders")
    public NewTopic validOrders() {
        return new NewTopic("valid-orders", 3, (short) 1);
    }

    @Bean("invalid-orders")
    public NewTopic invalidOrders() {
        return new NewTopic("invalid-orders", 3, (short) 1);
    }
}
