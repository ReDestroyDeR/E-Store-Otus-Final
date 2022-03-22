package ru.red.orderservice.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import ru.red.orderservice.domain.IdempotentOrder;

public interface OrderOperationRepository extends ReactiveMongoRepository<IdempotentOrder, String> {
}
