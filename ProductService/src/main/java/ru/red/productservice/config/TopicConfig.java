package ru.red.productservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopicConfig {
    public final String productOpsTopicName = "product-ops";

    @Bean
    public NewTopic products() {
        return new NewTopic(productOpsTopicName, 3, (short) 1);
    }
}
