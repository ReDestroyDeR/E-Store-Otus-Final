package ru.red.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.red.domain.UserBilling;
import ru.red.dto.UserIdentityDTO;
import ru.red.exception.BadRequestException;
import ru.red.exception.NotFoundException;
import ru.red.repository.UserBillingRepository;

@Log4j2
@Service
public class UserBillingServiceImpl implements UserBillingService {
    private final UserBillingRepository repository;

    @Autowired
    public UserBillingServiceImpl(UserBillingRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<UserBilling> create(UserIdentityDTO dto) {
        var domain = new UserBilling();
        domain.setEmail(dto.getEmail());
        domain.setBalance(0);
        return repository.save(domain)
                .onErrorMap(DataAccessException.class, BadRequestException::new)
                .doOnSuccess(s -> log.info("Created User [{}] {}", s.getId(), s.getEmail()))
                .doOnError(e -> log.info("Failed creating User {} {}", dto.getEmail(), e.getMessage()));
    }

    @Override
    public Mono<UserBilling> findById(Long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("No user by id %s".formatted(id))))
                .doOnNext(s -> log.info("Found user [{}] by id {}", s.getEmail(), id))
                .doOnError(e -> log.info("Can't find user by id {}", id));
    }

    @Override
    public Mono<UserBilling> findByEmail(String email) {
        return repository.findByEmail(email)
                .switchIfEmpty(Mono.error(new NotFoundException("No user for email %s".formatted(email))))
                .doOnNext(s -> log.info("Found user [{}] by email {}", s.getId(), email))
                .doOnError(e -> log.info("Can't find user by email {}", email));
    }

    private Mono<UserBilling> addFundsToUser(Mono<UserBilling> userBillingMono, int add) {
        return userBillingMono
                .flatMap(billing -> applyDeltaWithValidation(billing, billing.getBalance() + add))
                .flatMap(repository::save);
    }

    private Mono<UserBilling> removeFundsFromUser(Mono<UserBilling> userBillingMono, int sub) {
        return userBillingMono
                .flatMap(billing -> applyDeltaWithValidation(billing, billing.getBalance() - sub))
                .flatMap(repository::save);
    }

    @Override
    public Mono<UserBilling> addFundsToUser(String email, int add) {
        return fundsValidation(add)
                .doOnError(e -> log.warn("Funds add on {} {}", email, e.getMessage()))
                .then(findByEmail(email)) // Why bother validating negative balance for addition
                .as(mono -> this.addFundsToUser(mono, add))
                .doOnNext(s -> log.info("Funds operation on {} +{} ({})", email, add, s.getBalance()));
    }

    @Override
    public Mono<UserBilling> removeFundsFromUser(String email, int sub) {
        return fundsValidation(sub)
                .doOnError(e -> log.warn("Funds sub on {} {}", email, e.getMessage()))
                .then(findByEmail(email))
                .as(mono -> this.removeFundsFromUser(mono, sub))
                .doOnNext(s -> log.info("Funds operation on {} -{} ({})", email, sub, s.getBalance()));
    }

    @Override
    public Mono<UserBilling> addFundsToUser(Long id, int add) {
        return fundsValidation(add)
                .doOnError(e -> log.warn("Funds add on [{}] {}", id, e.getMessage()))
                .then(findById(id)) // Why bother validating negative balance for addition
                .as(mono -> this.addFundsToUser(mono, add))
                .doOnNext(s -> log.info("Funds operation on {} +{} ({})", s.getEmail(), add, s.getBalance()));
    }

    @Override
    public Mono<UserBilling> removeFundsFromUser(Long id, int sub) {
        return fundsValidation(sub)
                .doOnError(e -> log.warn("Funds sub on [{}] {}", id, e.getMessage()))
                .then(findById(id))
                .as(mono -> this.removeFundsFromUser(mono, sub))
                .doOnNext(s -> log.info("Funds operation on {} -{} ({})", s.getEmail(), sub, s.getBalance()));
    }

    @Override
    public Mono<UserBilling> updateEmail(String newEmail, String previousEmail) {
        return repository.findByEmail(previousEmail)
                .switchIfEmpty(Mono.error(new NotFoundException()))
                .map(billing -> {
                    billing.setEmail(newEmail);
                    return billing;
                })
                .flatMap(repository::save)
                .doOnSuccess(s -> log.info("Updated email for billing {} -> {}", previousEmail, newEmail));
    }

    @Override
    public Mono<Void> delete(String email) {
        return repository.findByEmail(email)
                .switchIfEmpty(Mono.error(new NotFoundException()))
                .flatMap(repository::delete)
                .doOnSuccess(s -> log.info("Deleted user {}", email));
    }

    private Mono<Integer> fundsValidation(Integer delta) {
        return Mono.just(delta)
                .flatMap(d -> d < 0
                        ? Mono.error(new BadRequestException("Negative delta"))
                        : Mono.just(delta));
    }

    private Mono<UserBilling> applyDeltaWithValidation(UserBilling billing, int newBalance) {
        if (newBalance < 0)
            return Mono.error(new BadRequestException("Negative balance"))
                    .doOnError(e -> log.warn("Funds operation to {} {}", billing.getEmail(), e.getMessage()))
                    .dematerialize();

        billing.setBalance(newBalance);
        return Mono.just(billing);
    }
}
