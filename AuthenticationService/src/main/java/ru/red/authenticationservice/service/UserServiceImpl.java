package ru.red.authenticationservice.service;

import lombok.extern.log4j.Log4j2;
import org.jooq.exception.DataAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.red.authenticationservice.dto.UserDetachedDTO;
import ru.red.authenticationservice.exception.BadPasswordException;
import ru.red.authenticationservice.exception.BadRequestException;
import ru.red.authenticationservice.exception.NotFoundException;
import ru.red.authenticationservice.jooq.tables.records.UsersRecord;
import ru.red.authenticationservice.mapper.UserMapper;
import ru.red.authenticationservice.producer.UserManipulationProducer;
import ru.red.authenticationservice.repository.UserRepository;
import ru.red.authenticationservice.security.jwt.JwtProvider;
import ru.red.authenticationservice.util.StringUtil;

@Log4j2
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserManipulationProducer producer;
    private final JwtProvider jwtProvider;
    private final Integer saltLength;

    @Autowired
    public UserServiceImpl(UserRepository repository,
                           JwtProvider jwtProvider,
                           PasswordEncoder passwordEncoder,
                           UserMapper userMapper,
                           UserManipulationProducer producer,
                           @Value("${auth.security.salt.length}") Integer saltLength) {
        this.repository = repository;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.producer = producer;
        this.saltLength = saltLength;
    }

    @Override
    public Mono<UsersRecord> registerUser(UserDetachedDTO userDetachedDTO) {
        if (userDetachedDTO.getEmail() == null || userDetachedDTO.getEmail().length() < 3) {
            return Mono.error(new BadRequestException("Email length must be >= 3 characters"));
        }

        UsersRecord usersRecord = userMapper.userDetachedDtoToUsersRecord(userDetachedDTO);
        updateSaltPasswordPair(userDetachedDTO.getPassword(), usersRecord);
        return repository.createUser(usersRecord)
                .onErrorMap(BadRequestException::new)
                .doOnSuccess(s -> log.info("Account created [{}] {}", s.getId(), usersRecord.getEmail()))
                .doOnError(e -> log.info("Account creation failed for {} {}", usersRecord.getEmail(), e.getMessage()))
                .flatMap(usersRec -> producer.created(userMapper.usersRecordToUserIdentityDTO(usersRec))
                        .thenReturn(usersRec));
    }

    @Override
    public Mono<String> login(UserDetachedDTO userDetachedDTO) {
        // TODO: Add refresh token functionality
        return repository.getUser(userDetachedDTO.getEmail())
                .switchIfEmpty(Mono.error(NotFoundException::new))
                .flatMap(user -> passwordEncoder.matches(
                        userDetachedDTO.getPassword().concat(user.getSalt()),
                        user.getPassword())
                        ? Mono.just(jwtProvider.createJwt(user))
                        : Mono.error(new BadPasswordException()));
    }

    @Override
    public Mono<UsersRecord> updateEmail(String email, UserDetachedDTO dto) {
        return validatePassword(dto)
                .flatMap(user -> {
                    user.setEmail(email);
                    return repository.updateUser(user.getId(), user);
                })
                .onErrorMap(e -> e.getClass().equals(DataAccessException.class) ? new BadRequestException(e) : e)
                .doOnSuccess(s -> log.info(
                        "Successfully updated email for user [{}] {} -> {}",
                        s.getId(),
                        dto.getEmail(),
                        s.getEmail()))
                .doOnError(e -> log.info(
                        "Failed updating email for user {} -> {} {}",
                        dto.getEmail(),
                        email,
                        e.getMessage()
                ))
                .flatMap(user -> producer.updatedEmail(userMapper.usersRecordToUserIdentityDTO(user), dto.getEmail())
                        .thenReturn(user));
    }

    @Override
    public Mono<UsersRecord> updatePassword(String password, UserDetachedDTO dto) {
        return validatePassword(dto)
                .flatMap(user -> {
                    updateSaltPasswordPair(password, user);
                    return repository.updateUser(user.getId(), user);
                })
                .doOnSuccess(s -> log.info(
                        "Successfully updated password for user [{}] {}",
                        s.getId(),
                        s.getEmail()
                ))
                .doOnError(e -> log.info(
                        "Failed updating password for user {} {}",
                        dto.getEmail(),
                        e.getMessage()
                ));
    }

    @Override
    public Mono<Void> delete(UserDetachedDTO userDetachedDTO) {
        return validatePassword(userDetachedDTO)
                .flatMap(user -> repository.deleteUser(user.getId()))
                .flatMap(i -> i == 1
                        ? Mono.just(i)
                        : Mono.error(new NotFoundException("Can't delete user " + userDetachedDTO.getEmail())))
                .doOnSuccess(s -> log.info("Successfully deleted user {}", userDetachedDTO.getEmail()))
                .doOnError(e -> log.info("Failed deleting user {} {}", userDetachedDTO.getEmail(), e.getMessage()))
                .then(producer.deleted(userMapper.userDetachedDTOToUserIdentityDTO(userDetachedDTO)))
                .then();
    }

    private void updateSaltPasswordPair(String password, UsersRecord usersRecord) {
        usersRecord.setSalt(StringUtil.generateRandomString(this.saltLength));
        usersRecord.setPassword(passwordEncoder.encode(password.concat(usersRecord.getSalt())));
    }

    private Mono<UsersRecord> validatePassword(UserDetachedDTO userDetachedDTO) {
        return repository.getUser(userDetachedDTO.getEmail())
                .switchIfEmpty(Mono.error(NotFoundException::new))
                .flatMap(user -> passwordEncoder.matches(
                                userDetachedDTO.getPassword() + user.getSalt(),
                                user.getPassword()
                        ) ? Mono.just(user) : Mono.error(new BadPasswordException("Bad password"))
                );
    }
}
