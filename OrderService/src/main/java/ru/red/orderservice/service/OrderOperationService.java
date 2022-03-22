package ru.red.orderservice.service;

import reactor.core.publisher.Mono;
import ru.red.orderservice.domain.IdempotentOrder;
import ru.red.orderservice.domain.Order;

public interface OrderOperationService {
    Mono<Order> getByIdempotencyKey(String idempotencyKey);

    Mono<IdempotentOrder> commit(String idempotencyKey, Order response);
}
