package ru.red.orderservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NewTopicConfig {
    @Bean("valid-orders")
    public NewTopic validOrders() {
        return new NewTopic("valid-orders", 3, (short) 1);
    }

    @Bean("invalid-orders")
    public NewTopic invalidOrders() {
        return new NewTopic("invalid-orders", 3, (short) 1);
    }
}
