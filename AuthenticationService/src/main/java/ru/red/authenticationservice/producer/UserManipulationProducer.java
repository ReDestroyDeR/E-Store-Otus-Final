package ru.red.authenticationservice.producer;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.red.auth.avro.UserCreated;
import ru.red.auth.avro.UserDeleted;
import ru.red.auth.avro.UserUpdatedEmail;
import ru.red.authenticationservice.dto.UserIdentityDTO;

@Log4j2
@SuppressWarnings("ClassCanBeRecord")
@Component
public class UserManipulationProducer {
    private static final String TOPIC_NAME = "user-manipulation";

    private final KafkaTemplate<String, UserCreated> userCreatedTemplate;
    private final KafkaTemplate<String, UserUpdatedEmail> userUpdatedEmailTemplate;
    private final KafkaTemplate<String, UserDeleted> userDeletedTemplate;

    @Autowired
    public UserManipulationProducer(KafkaTemplate<String, UserCreated> userCreatedTemplate,
                                    KafkaTemplate<String, UserUpdatedEmail> userUpdatedEmailTemplate,
                                    KafkaTemplate<String, UserDeleted> userDeletedTemplate) {
        this.userCreatedTemplate = userCreatedTemplate;
        this.userUpdatedEmailTemplate = userUpdatedEmailTemplate;
        this.userDeletedTemplate = userDeletedTemplate;
    }

    public Mono<SendResult<String, UserCreated>> created(UserIdentityDTO identityDTO) {
        return Mono.fromFuture(
                userCreatedTemplate.send(TOPIC_NAME, identityDTO.getEmail(), new UserCreated())
                        .completable()
        );
    }

    public Mono<SendResult<String, UserUpdatedEmail>> updatedEmail(UserIdentityDTO identityDTO, String previousEmail) {
        return Mono.fromFuture(
                userUpdatedEmailTemplate.send(TOPIC_NAME, identityDTO.getEmail(), new UserUpdatedEmail(previousEmail))
                        .completable()
        );
    }

    // FIXME: Deleted messages get produced twice. They're idempotent so it should be alright // NOSONAR
    public Mono<SendResult<String, UserDeleted>> deleted(UserIdentityDTO identityDTO) {
        return Mono.fromFuture(
                userDeletedTemplate.send(TOPIC_NAME, identityDTO.getEmail(), new UserDeleted())
                        .completable()
        );
    }
}
