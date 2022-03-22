package ru.red.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic orderManipulationTopic() {
        return new NewTopic("order-manipulation", 3, (short) 1);
    }
}
