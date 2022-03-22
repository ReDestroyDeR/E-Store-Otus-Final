package ru.red.authenticationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient businessServiceWebClient(@Value("${external.business.url}") String url) {
        return WebClient.builder()
                .baseUrl("http://" + url)
                .build();
    }
}
