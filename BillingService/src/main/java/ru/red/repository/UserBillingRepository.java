package ru.red.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.red.domain.UserBilling;

public interface UserBillingRepository extends ReactiveCrudRepository<UserBilling, Long> {
    Mono<UserBilling> findByEmail(String email);
}
