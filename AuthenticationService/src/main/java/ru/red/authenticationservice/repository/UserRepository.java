package ru.red.authenticationservice.repository;

import reactor.core.publisher.Mono;
import ru.red.authenticationservice.jooq.tables.records.UsersRecord;

public interface UserRepository {
    Mono<UsersRecord> getUser(Long id);

    Mono<UsersRecord> getUser(String username);

    Mono<UsersRecord> createUser(UsersRecord usersRecord);

    Mono<UsersRecord> updateUser(Long id, UsersRecord usersRecord);

    Mono<Integer> deleteUser(Long id);
}
