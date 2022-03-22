package ru.red.authenticationservice.streams;

import final$.avro.ValueUserManipulation;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.StreamsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.red.authenticationservice.service.UserService;

import java.util.Map;

@Component
public class UserProcessor {
    private static final Serde<ValueUserManipulation> VALUE_USER_MANIPULATION_SERDE = new SpecificAvroSerde<>();

    private final UserService userService;

    @Autowired
    public UserProcessor(UserService userService,
                         @Qualifier("serde-config") Map<String, String> serdeConfig) {
        this.userService = userService;
        VALUE_USER_MANIPULATION_SERDE.configure(serdeConfig, false);
    }

    @Autowired
    void buildPipeline(StreamsBuilder builder) {

    }
}
