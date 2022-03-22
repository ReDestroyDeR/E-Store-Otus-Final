package ru.red.authenticationservice.streams;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.red.authenticationservice.dto.UserIdentityDTO;
import ru.red.authenticationservice.service.UserService;
import ru.red.avro.ManipulationType;
import ru.red.avro.ValueUserManipulation;

@SuppressWarnings("ClassCanBeRecord")
@Component
public class UserManipulationProducer {
    private final Producer<String, ValueUserManipulation> producer;

    private final UserService userService;

    @Autowired
    public UserManipulationProducer(UserService userService,
                                    @Qualifier("user-manipulation-producer-factory")
                                            ProducerFactory<String, ValueUserManipulation> producerFactory) {
        this.userService = userService;
        this.producer = producerFactory.createProducer();
    }

    public Mono<RecordMetadata> created(UserIdentityDTO identityDTO) {
        return sendMessage(identityDTO.getEmail(),
                new ValueUserManipulation(ManipulationType.CREATED, ""));
    }

    public Mono<RecordMetadata> updatedEmail(UserIdentityDTO identityDTO, String previousEmail) {
        return sendMessage(identityDTO.getEmail(),
                new ValueUserManipulation(ManipulationType.UPDATED_EMAIL, previousEmail));

    }

    public Mono<RecordMetadata> deleted(UserIdentityDTO identityDTO) {
        return sendMessage(identityDTO.getEmail(),
                new ValueUserManipulation(ManipulationType.DELETED, ""));
    }

    private Mono<RecordMetadata> sendMessage(String email, ValueUserManipulation value) {
        var producerRecord = new ProducerRecord<>("user-manipulation", email, value);
        return Mono.create(sink -> producer.send(producerRecord, (metadata, exception) -> {
            if (exception == null) {
                sink.success(metadata);
            } else {
                sink.error(exception);
            }
        }));
    }
}
