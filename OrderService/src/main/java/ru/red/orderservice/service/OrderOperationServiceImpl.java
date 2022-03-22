package ru.red.orderservice.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.red.orderservice.domain.IdempotentOrder;
import ru.red.orderservice.domain.Order;
import ru.red.orderservice.repository.OrderOperationRepository;

@Log4j2
@Service
public class OrderOperationServiceImpl implements OrderOperationService {
    private final OrderOperationRepository repository;

    @Autowired
    public OrderOperationServiceImpl(OrderOperationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Order> getByIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Mono.empty();
        }

        return repository.findById(idempotencyKey)
                .map(IdempotentOrder::getResponse)
                .doOnNext(element -> log.info("Found record by idempotency key {} order id {}",
                                idempotencyKey,
                                element.getId()
                        )
                );
    }

    @Override
    public Mono<IdempotentOrder> commit(String idempotencyKey, Order response) {
        var container = new IdempotentOrder(response);

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Mono.just(container);
        }

        container.setIdempotencyKey(idempotencyKey);
        return repository.save(container)
                .doOnNext(element -> log.info("Committed record {} order id {}", idempotencyKey, response.getId()))
                .doOnError(element -> log.info("Failed commit record {} order id {}", idempotencyKey, response.getId()));
    }
}
