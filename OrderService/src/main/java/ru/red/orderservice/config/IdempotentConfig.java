package ru.red.orderservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration("idempotentConfig")
public class IdempotentConfig {
    @Value("${service.order.idempotency.ttl}")
    public String ttl;
}
