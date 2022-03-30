package ru.red.productservice.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class NewTopicConfig {
    public static final String PRODUCT_OPS_TOPIC_NAME = "product-ops";
    public static final String COMMENTS_TOPIC_NAME = "product-comments";

    @Bean
    public KafkaAdmin admin(@Value("${kafka.bootstrapAddress}") String bootstrapServers) {
        var properties = new HashMap<String, Object>();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(properties);
    }

    @Bean("products")
    public NewTopic products() {
        return new NewTopic(PRODUCT_OPS_TOPIC_NAME, 3, (short) 1);
    }

    @Bean("product-comments")
    public NewTopic productComments() {
        return new NewTopic(COMMENTS_TOPIC_NAME, 3, (short) 1)
                .configs(Map.of(
                                TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT
                        )
                );
    }
}
