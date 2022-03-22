package ru.red.authenticationservice.repository;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.red.authenticationservice.jooq.tables.Users;
import ru.red.authenticationservice.jooq.tables.records.UsersRecord;


@Repository
public class UserRepositoryImpl implements UserRepository {
    private final DSLContext jooq;

    @Autowired
    public UserRepositoryImpl(DSLContext jooq) {
        this.jooq = jooq;
    }

    @Override
    public Mono<UsersRecord> getUser(Long id) {
        return Mono.create(sink -> {
            try {
                sink.success(jooq.selectFrom(Users.USERS)
                        .where(Users.USERS.ID.eq(id))
                        .fetchOne());
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    @Override
    public Mono<UsersRecord> getUser(String username) {
        return Mono.create(sink -> {
            try {
                sink.success(jooq.selectFrom(Users.USERS)
                        .where(Users.USERS.USERNAME.eq(username))
                        .fetchOne());
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    @Override
    public Mono<UsersRecord> createUser(UsersRecord usersRecord) {
        return Mono.create(sink -> {
            try {
                usersRecord.changed(Users.USERS.ID, false);
                sink.success(jooq.insertInto(Users.USERS)
                        .set(usersRecord)
                        .returning()
                        .fetchOne());
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    @Override
    public Mono<UsersRecord> updateUser(Long id, UsersRecord usersRecord) {
        return Mono.create(sink -> {
            try {
                sink.success(jooq.update(Users.USERS)
                        .set(usersRecord)
                        .where(Users.USERS.ID.eq(id))
                        .returning()
                        .fetchOne());
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    @Override
    public Mono<Integer> deleteUser(Long id) {
        return Mono.create(sink -> {
            try {
                sink.success(jooq.delete(Users.USERS)
                        .where(Users.USERS.ID.eq(id))
                        .execute());
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }
}
