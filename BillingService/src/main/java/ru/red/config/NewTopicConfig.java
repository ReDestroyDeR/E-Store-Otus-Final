package ru.red.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NewTopicConfig {
    @Bean
    public NewTopic orderAcknowledgment() {
        return new NewTopic("order-acknowledgment", 3, (short) 1);
    }
}
