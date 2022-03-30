package ru.red.authenticationservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NewTopicConfig {
    @Bean
    public NewTopic userManipulation() {
        return new NewTopic("user-manipulation", 3, (short) 1);
    }
}
