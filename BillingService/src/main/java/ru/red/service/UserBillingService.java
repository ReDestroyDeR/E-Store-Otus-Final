package ru.red.service;

import reactor.core.publisher.Mono;
import ru.red.domain.UserBilling;
import ru.red.dto.UserIdentityDTO;

public interface UserBillingService {
    Mono<UserBilling> create(UserBilling domain);

    Mono<UserBilling> create(UserIdentityDTO dto);

    Mono<UserBilling> findById(Long id);

    Mono<UserBilling> findByEmail(String email);

    Mono<UserBilling> addFundsToUser(String email, int add);

    Mono<UserBilling> removeFundsFromUser(String email, int sub);

    Mono<UserBilling> addFundsToUser(Long id, int add);

    Mono<UserBilling> removeFundsFromUser(Long id, int sub);

    Mono<UserBilling> updateEmail(String newEmail, String previousEmail);

    Mono<Void> delete(String email);
}
