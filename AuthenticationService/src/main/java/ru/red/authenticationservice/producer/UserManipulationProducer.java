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

@Log4j2
@SuppressWarnings("ClassCanBeRecord")
@Component
public class UserManipulationProducer {
    private static final String TOPIC_NAME = "user-manipulation";

    private final KafkaTemplate<Long, UserCreated> userCreatedTemplate;
    private final KafkaTemplate<Long, UserUpdatedEmail> userUpdatedEmailTemplate;
    private final KafkaTemplate<Long, UserDeleted> userDeletedTemplate;

    @Autowired
    public UserManipulationProducer(KafkaTemplate<Long, UserCreated> userCreatedTemplate,
                                    KafkaTemplate<Long, UserUpdatedEmail> userUpdatedEmailTemplate,
                                    KafkaTemplate<Long, UserDeleted> userDeletedTemplate) {
        this.userCreatedTemplate = userCreatedTemplate;
        this.userUpdatedEmailTemplate = userUpdatedEmailTemplate;
        this.userDeletedTemplate = userDeletedTemplate;
    }

    public Mono<SendResult<Long, UserCreated>> created(Long id, String email) {
        return Mono.fromFuture(
                userCreatedTemplate.send(TOPIC_NAME, id, new UserCreated(email))
                        .completable()
        );
    }

    public Mono<SendResult<Long, UserUpdatedEmail>> updatedEmail(Long id, String email, String previousEmail) {
        return Mono.fromFuture(
                userUpdatedEmailTemplate.send(TOPIC_NAME, id, new UserUpdatedEmail(email, previousEmail))
                        .completable()
        );
    }

    // FIXME: Deleted messages get produced twice. They're idempotent so it should be alright // NOSONAR
    public Mono<SendResult<Long, UserDeleted>> deleted(Long id, String email) {
        return Mono.fromFuture(
                userDeletedTemplate.send(TOPIC_NAME, id, new UserDeleted(email))
                        .completable()
        );
    }
}
