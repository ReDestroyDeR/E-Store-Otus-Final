package ru.red.orderservice.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.red.orderservice.domain.Order;
import ru.red.orderservice.dto.OrderDTO;

public interface OrderService {
    Mono<Order> createOrder(OrderDTO dto);

    Flux<Order> fetchOrdersByEmail(String address);

    Flux<Order> fetchOrdersByTotalPriceBetween(Integer totalPriceStart, Integer totalPriceEnd);

    Mono<Order> fetchOrderById(String id);
}
