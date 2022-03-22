package ru.red.authenticationservice.streams;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.red.authenticationservice.dto.UserIdentityDTO;
import ru.red.avro.ManipulationType;
import ru.red.avro.ValueUserManipulation;

@SuppressWarnings("ClassCanBeRecord")
@Component
public class UserManipulationProducer {
    private final KafkaTemplate<String, ValueUserManipulation> kafkaTemplate;

    @Autowired
    public UserManipulationProducer(KafkaTemplate<String, ValueUserManipulation> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public Mono<SendResult<String, ValueUserManipulation>> created(UserIdentityDTO identityDTO) {
        return sendMessage(identityDTO.getEmail(),
                new ValueUserManipulation(ManipulationType.CREATED, ""));
    }

    public Mono<SendResult<String, ValueUserManipulation>> updatedEmail(UserIdentityDTO identityDTO, String previousEmail) {
        return sendMessage(identityDTO.getEmail(),
                new ValueUserManipulation(ManipulationType.UPDATED_EMAIL, previousEmail));

    }

    public Mono<SendResult<String, ValueUserManipulation>> deleted(UserIdentityDTO identityDTO) {
        return sendMessage(identityDTO.getEmail(),
                new ValueUserManipulation(ManipulationType.DELETED, ""));
    }

    private Mono<SendResult<String, ValueUserManipulation>> sendMessage(String email, ValueUserManipulation value) {
        return Mono.fromFuture(kafkaTemplate.send("user-manipulation", email, value).completable());
    }
}
