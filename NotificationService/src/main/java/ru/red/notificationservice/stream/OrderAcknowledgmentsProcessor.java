package ru.red.notificationservice.stream;

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
import ru.red.billing.avro.OrderAcknowledged;
import ru.red.billing.avro.OrderAcknowledgmentKey;
import ru.red.billing.avro.OrderNotAcknowledged;
import ru.red.notificationservice.factory.NotificationDTOFactory;
import ru.red.notificationservice.service.NotificationService;

import java.util.Map;

@Log4j2
@Component
@Profile("kafka-streams")
public class OrderAcknowledgmentsProcessor {
    private static final Serde<OrderAcknowledgmentKey> ORDER_ACKNOWLEDGMENT_KEY_SERDE = new SpecificAvroSerde<>();
    private static final Serde<OrderAcknowledged> ORDER_ACKNOWLEDGED_SERDE = new SpecificAvroSerde<>();
    private static final Serde<OrderNotAcknowledged> ORDER_NOT_ACKNOWLEDGED_SERDE = new SpecificAvroSerde<>();

    private static final Serde<GenericRecord> GENERIC_VALUE_SERDE = new GenericAvroSerde();

    private static final String ACKNOWLEDGMENT_TOPIC = "order-acknowledgment";

    private final NotificationService service;
    private final NotificationDTOFactory dtoFactory;

    @Autowired
    public OrderAcknowledgmentsProcessor(NotificationService service, NotificationDTOFactory dtoFactory,
                                         @Qualifier("serde-topic-record-config") Map<String, String> serdeTopicRecordConfig) {
        this.service = service;
        this.dtoFactory = dtoFactory;

        ORDER_ACKNOWLEDGMENT_KEY_SERDE.configure(serdeTopicRecordConfig, true);
        ORDER_ACKNOWLEDGED_SERDE.configure(serdeTopicRecordConfig, false);
        ORDER_NOT_ACKNOWLEDGED_SERDE.configure(serdeTopicRecordConfig, false);

        GENERIC_VALUE_SERDE.configure(serdeTopicRecordConfig, false);
    }

    @Autowired
    void buildPipeline(StreamsBuilder builder) {
        // Acknowledged processing
        builder.stream(ACKNOWLEDGMENT_TOPIC,
                        Consumed.with(ORDER_ACKNOWLEDGMENT_KEY_SERDE, Serdes.Bytes()))
                .filter(((key, bytes) -> GENERIC_VALUE_SERDE.deserializer()
                        .deserialize(ACKNOWLEDGMENT_TOPIC, bytes.get()).getSchema().getFullName()
                        .equals(OrderAcknowledged.getClassSchema().getFullName())))
                .foreach(((key, bytes) -> {
                    var value = ORDER_ACKNOWLEDGED_SERDE.deserializer()
                            .deserialize(ACKNOWLEDGMENT_TOPIC, bytes.get());
                    log.info("Got message: {} ACK", key.getUserId());
                    service.createNotification(dtoFactory.createDto(key, value))
                            .publishOn(Schedulers.boundedElastic())
                            .subscribe(
                                    s -> logOk(key.getUserId(), "ACK"),
                                    e -> logError(key.getUserId(), "ACK", e));
                }));

        // Not Acknowledged processing
        builder.stream(ACKNOWLEDGMENT_TOPIC,
                        Consumed.with(ORDER_ACKNOWLEDGMENT_KEY_SERDE, Serdes.Bytes()))
                .filter(((key, bytes) -> GENERIC_VALUE_SERDE.deserializer()
                        .deserialize(ACKNOWLEDGMENT_TOPIC, bytes.get()).getSchema().getFullName()
                        .equals(OrderNotAcknowledged.getClassSchema().getFullName())))
                .foreach(((key, bytes) -> {
                    var value = ORDER_NOT_ACKNOWLEDGED_SERDE.deserializer()
                            .deserialize(ACKNOWLEDGMENT_TOPIC, bytes.get());
                    log.info("Got message: {} NACK", key.getUserId());
                    service.createNotification(dtoFactory.createDto(key, value))
                            .publishOn(Schedulers.boundedElastic())
                            .subscribe(
                                    s -> logOk(key.getUserId(), "NACK"),
                                    e -> logError(key.getUserId(), "NACK", e));
                }));
    }

    private void logOk(Long userId, String event) {
        log.info("Successfully created notification for {} {}", userId, event);
    }

    private void logError(Long userId, String event, Throwable error) {
        log.warn("Failed creating notification for {} {} {}", userId, event, error.getMessage());
    }
}
