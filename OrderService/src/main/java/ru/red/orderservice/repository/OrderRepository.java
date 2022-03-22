package ru.red.orderservice.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import ru.red.orderservice.domain.Order;

public interface OrderRepository extends ReactiveMongoRepository<Order, String> {
    Flux<Order> findAllByUserAddress(String userAddress);

    Flux<Order> findAllByTotalPriceBetween(Integer totalPriceStart, Integer totalPriceEnd);
}
