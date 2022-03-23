package ru.red.productservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class NewTopicConfig {
    public static final String PRODUCT_OPS_TOPIC_NAME = "product-ops";
    public static final String COMMENTS_TOPIC_NAME = "product-comments";

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
