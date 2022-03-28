package ru.red.streams;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;
import ru.red.billing.avro.OrderAcknowledged;
import ru.red.billing.avro.OrderAcknowledgmentKey;
import ru.red.billing.avro.OrderNotAcknowledged;
import ru.red.domain.UserBilling;
import ru.red.order.avro.OrderCancelled;
import ru.red.order.avro.OrderManipulationKey;
import ru.red.order.avro.OrderPlaced;
import ru.red.service.UserBillingService;

import java.util.Map;

@Log4j2
@Component
@Profile("kafka-streams")
public class NewOrderProcessor {
    private static final Serde<OrderManipulationKey> ORDER_MANIPULATION_KEY_SERDE = new SpecificAvroSerde<>();
    private static final Serde<OrderPlaced> ORDER_PLACED_SERDE = new SpecificAvroSerde<>();
    private static final Serde<OrderCancelled> ORDER_CANCELLED_SERDE = new SpecificAvroSerde<>();

    private static final Serde<OrderAcknowledgmentKey> ORDER_ACKNOWLEDGMENT_KEY_SERDE = new SpecificAvroSerde<>();
    private static final Serde<OrderAcknowledged> ORDER_ACKNOWLEDGED_SERDE = new SpecificAvroSerde<>();
    private static final Serde<OrderNotAcknowledged> ORDER_NOT_ACKNOWLEDGED_SERDE = new SpecificAvroSerde<>();

    private static final String ORDER_ACKNOWLEDGMENT_TOPIC_NAME = "order-acknowledgment";

    private final UserBillingService billingService;

    @Autowired
    public NewOrderProcessor(UserBillingService billingService,
                             @Qualifier("serde-config") Map<String, String> serdeConfig) {
        this.billingService = billingService;
        ORDER_MANIPULATION_KEY_SERDE.configure(serdeConfig, true);
        ORDER_PLACED_SERDE.configure(serdeConfig, false);
        ORDER_CANCELLED_SERDE.configure(serdeConfig, false);

        ORDER_ACKNOWLEDGMENT_KEY_SERDE.configure(serdeConfig, true);
        ORDER_ACKNOWLEDGED_SERDE.configure(serdeConfig, false);
        ORDER_NOT_ACKNOWLEDGED_SERDE.configure(serdeConfig, false);
    }

    @Autowired
    void buildPipeline(StreamsBuilder builder) {
        builder.stream("valid-orders",
                        Consumed.with(ORDER_MANIPULATION_KEY_SERDE, ORDER_PLACED_SERDE))
                .split()
                .branch((key, value) ->
                                Boolean.TRUE.equals(billingService.findByEmail(key.getEmail().toString())
                                        .publishOn(Schedulers.boundedElastic())
                                        .map(billing -> billing.getBalance() - value.getTotalPrice() >= 0)
                                        .defaultIfEmpty(false)
                                        .onErrorReturn(false)
                                        .block()),
                        Branched.withConsumer(stream -> stream
                                        .map((key, value) -> {
                                            var balance =
                                                    billingService.removeFundsFromUser(key.getEmail().toString(),
                                                                    value.getTotalPrice())
                                                            .publishOn(Schedulers.boundedElastic())
                                                            .map(UserBilling::getBalance)
                                                            .block();
                                            return new KeyValue<>(
                                                    new OrderAcknowledgmentKey(key.getOrderId(), key.getEmail()),
                                                    new OrderAcknowledged(value.getTotalPrice(), balance, value.getItems()));
                                        })
                                        .to(ORDER_ACKNOWLEDGMENT_TOPIC_NAME,
                                                Produced.with(ORDER_ACKNOWLEDGMENT_KEY_SERDE, ORDER_ACKNOWLEDGED_SERDE)),
                                "acknowledged-orders"))
                .defaultBranch(Branched.withConsumer(stream -> stream
                                .map((key, value) -> {
                                    // Code duplication from predicate
                                    var balance = billingService.findByEmail(key.getEmail().toString())
                                            .publishOn(Schedulers.boundedElastic())
                                            .map(UserBilling::getBalance)
                                            .onErrorReturn(0)
                                            .block();
                                    return new KeyValue<>(
                                            new OrderAcknowledgmentKey(key.getOrderId(), key.getEmail()),
                                            new OrderNotAcknowledged(value.getTotalPrice(), balance, value.getItems()));
                                })
                                .to(ORDER_ACKNOWLEDGMENT_TOPIC_NAME,
                                        Produced.with(ORDER_ACKNOWLEDGMENT_KEY_SERDE, ORDER_NOT_ACKNOWLEDGED_SERDE)),
                        "not-acknowledged-orders"));
    }
}
