package ru.red.streams;

import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.extern.log4j.Log4j2;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;
import ru.red.auth.avro.UserCreated;
import ru.red.auth.avro.UserDeleted;
import ru.red.auth.avro.UserUpdatedEmail;
import ru.red.domain.UserBilling;
import ru.red.service.UserBillingService;

import java.util.Map;

@Log4j2
@Component
@Profile("kafka-streams")
public class AccountProcessor {
    private static final Serde<UserCreated> USER_CREATED_SERDE = new SpecificAvroSerde<>();
    private static final Serde<UserUpdatedEmail> USER_UPDATED_EMAIL_SERDE = new SpecificAvroSerde<>();
    private static final Serde<UserDeleted> USER_DELETED_SERDE = new SpecificAvroSerde<>();

    private static final Serde<GenericRecord> GENERIC_VALUE_SERDE = new GenericAvroSerde();

    private static final String USER_MANIPULATION_TOPIC_NAME = "user-manipulation";
    private final UserBillingService billingService;

    @Autowired
    public AccountProcessor(@Qualifier("serde-config-topic-record") Map<String, String> serdeTopicRecordConfig,
                            UserBillingService billingService) {
        USER_CREATED_SERDE.configure(serdeTopicRecordConfig, false);
        USER_UPDATED_EMAIL_SERDE.configure(serdeTopicRecordConfig, false);
        USER_DELETED_SERDE.configure(serdeTopicRecordConfig, false);

        GENERIC_VALUE_SERDE.configure(serdeTopicRecordConfig, false);
        this.billingService = billingService;
    }

    @Autowired
    void buildPipeline(StreamsBuilder builder) {
        /* TODO: Create thread based on KEY!!! // NOSONAR
         * Проблема в данном кейсе была только в подписках
         * на реактивные потоки.
         * Блокировка в целом решает данный вопрос, но проблема заключается
         * в серийной обработке.
         * Мы должны сделать такой диспетчер, который позволит исполняться только одному действию в очереди,
         * а очереди будут отдельные на каждый ключ. + Это должно работать как boundedElastic,
         * если 60 сек нет вызовов на один ключ, тогда убиваем поток.
         */
        builder.stream(USER_MANIPULATION_TOPIC_NAME,
                        Consumed.with(Serdes.Long(), Serdes.Bytes()))
                .foreach((key, bytes) -> {
                    var genericRecord = GENERIC_VALUE_SERDE.deserializer()
                            .deserialize(USER_MANIPULATION_TOPIC_NAME, bytes.get());

                    if (genericRecord.getSchema().getFullName()
                            .equals(UserCreated.getClassSchema().getFullName())) {
                        // Created
                        var userCreated = USER_CREATED_SERDE.deserializer()
                                .deserialize(USER_MANIPULATION_TOPIC_NAME, bytes.get());
                        var billing = new UserBilling();
                        billing.setId(key);
                        billing.setEmail(userCreated.getEmail().toString());
                        billing.setNew(true);
                        billingService.create(billing)
                                .publishOn(Schedulers.boundedElastic())
                                .doOnSuccess(s -> log.info("Processed UserCreated event [{}] {}",
                                        key, userCreated.getEmail()))
                                .onErrorContinue((e, o) -> log.warn("Failed processing UserCreated event [{}] {} {}",
                                        key, userCreated.getEmail(), e))
                                .block();
                    } else if (genericRecord.getSchema().getFullName()
                            .equals(UserUpdatedEmail.getClassSchema().getFullName())) {
                        // Updated Email
                        var updatedEmail = USER_UPDATED_EMAIL_SERDE.deserializer()
                                .deserialize(USER_MANIPULATION_TOPIC_NAME, bytes.get());
                        billingService.updateEmail(updatedEmail.getEmail().toString(),
                                        updatedEmail.getPreviousEmail().toString())
                                .publishOn(Schedulers.boundedElastic())
                                .doOnSuccess(s -> log.info("Processed UserUpdatedEmail event [{}] {} -> {}",
                                        key,
                                        updatedEmail.getPreviousEmail(),
                                        updatedEmail.getEmail()))
                                .onErrorContinue((e, o) -> log.warn("Failed processing UserUpdatedEmail event [{}] {} -> {} {}",
                                        key,
                                        updatedEmail.getPreviousEmail(),
                                        updatedEmail.getEmail(),
                                        e.getMessage()))
                                .block();
                    } else if (genericRecord.getSchema().getFullName()
                            .equals(UserDeleted.getClassSchema().getFullName())) {
                        // Deleted
                        var deleted = USER_DELETED_SERDE.deserializer()
                                .deserialize(USER_MANIPULATION_TOPIC_NAME, bytes.get());
                        billingService.delete(deleted.getEmail().toString())
                                .publishOn(Schedulers.boundedElastic())
                                .doOnSuccess(s -> log.info("Processed UserDeleted event [{}] {}",
                                        key, deleted.getEmail()))
                                .onErrorContinue((e, o) -> log.warn("Failed processing UserDeleted event [{}] {} {}",
                                        key, deleted.getEmail(), e.getMessage()))
                                .block();
                    }
                });
    }
}
