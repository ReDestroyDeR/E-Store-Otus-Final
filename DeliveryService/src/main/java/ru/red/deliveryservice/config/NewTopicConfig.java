package ru.red.deliveryservice.config;

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
    public KafkaAdmin admin(@Value("${kafka.bootstrapServers}") String bootstrapServers) {
        var properties = new HashMap<String, Object>();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(properties);
    }

    @Bean
    public NewTopic deliveryTopic() {
        return new NewTopic("delivery", 3, (short) 1);
    }
}
