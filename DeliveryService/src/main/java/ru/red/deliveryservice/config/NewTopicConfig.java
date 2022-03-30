package ru.red.deliveryservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NewTopicConfig {
    @Bean
    public NewTopic deliveryTopic() {
        return new NewTopic("delivery", 3, (short) 1);
    }
}
