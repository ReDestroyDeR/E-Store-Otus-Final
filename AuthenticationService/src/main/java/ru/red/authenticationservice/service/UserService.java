package ru.red.authenticationservice.service;

import reactor.core.publisher.Mono;
import ru.red.authenticationservice.dto.UserDetachedDTO;
import ru.red.authenticationservice.jooq.tables.records.UsersRecord;

public interface UserService {
    /**
     * Registration function returning created User
     *
     * @param userDetachedDTO credentials
     * @return Persisted {@link UsersRecord}
     */
    Mono<UsersRecord> registerUser(UserDetachedDTO userDetachedDTO);

    /**
     * Login function returning JWT
     * <p>
     * Could be fed with password or refresh token
     *
     * @param userDetachedDTO user data transfer object containing raw credentials
     * @return JWT
     */
    Mono<String> login(UserDetachedDTO userDetachedDTO);

    /**
     * Email Update function
     *
     * @param dto   User Credentials
     * @param email New email
     * @return Persisted {@link UsersRecord}
     */
    Mono<UsersRecord> updateEmail(String email, UserDetachedDTO dto);

    /**
     * Email Update function
     *
     * @param dto User Credentials
     * @param password New password
     * @return Persisted {@link UsersRecord}
     */
    Mono<UsersRecord> updatePassword(String password, UserDetachedDTO dto);

    /**
     * Users Delete function
     * Deletes user only if provided DTO has correct password for that user
     *
     * @param userDetachedDTO user instance
     * @return Completion signal, error on failure
     */
    Mono<Void> delete(UserDetachedDTO userDetachedDTO);
}
