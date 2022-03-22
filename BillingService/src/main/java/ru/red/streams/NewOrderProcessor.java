package ru.red.streams;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.red.service.UserBillingService;
import streamprocessing.avro.KeyOrderAcknowledgment;
import streamprocessing.avro.KeyOrderManipulation;
import streamprocessing.avro.OrderAckStatus;
import streamprocessing.avro.ValueOrderAcknowledgment;
import streamprocessing.avro.ValueOrderManipulation;

import java.util.Map;

@Log4j2
@Component
public class NewOrderProcessor {
    private static final Serde<KeyOrderManipulation> KEY_ORDER_MANIPULATION_SERDE = new SpecificAvroSerde<>();
    private static final Serde<ValueOrderManipulation> VALUE_ORDER_MANIPULATION_SERDE = new SpecificAvroSerde<>();

    private static final Serde<KeyOrderAcknowledgment> KEY_ORDER_ACKNOWLEDGMENT_SERDE = new SpecificAvroSerde<>();
    private static final Serde<ValueOrderAcknowledgment> VALUE_ORDER_ACKNOWLEDGMENT_SERDE = new SpecificAvroSerde<>();

    private final UserBillingService billingService;

    @Autowired
    public NewOrderProcessor(UserBillingService billingService,
                             @Qualifier("serde-config") Map<String, String> serdeConfig) {
        this.billingService = billingService;
        KEY_ORDER_MANIPULATION_SERDE.configure(serdeConfig, true);
        VALUE_ORDER_MANIPULATION_SERDE.configure(serdeConfig, false);

        KEY_ORDER_ACKNOWLEDGMENT_SERDE.configure(serdeConfig, true);
        VALUE_ORDER_ACKNOWLEDGMENT_SERDE.configure(serdeConfig, false);
    }

    @Autowired
    void buildPipeline(StreamsBuilder builder) {
        builder.stream("order-manipulation",
                        Consumed.with(KEY_ORDER_MANIPULATION_SERDE, VALUE_ORDER_MANIPULATION_SERDE))
                .map((key, value) -> {
                    var email = key.getUserAddress().toString();
                    var orderId = value.getOrderId().toString();
                    var totalPrice = value.getOrderTotalPrice();

                    var ackStatusBalancePairOptional = billingService.findByEmail(email)
                            .publishOn(Schedulers.boundedElastic())
                            .map(billing -> {
                                if (billing.getBalance() - totalPrice < 0) {
                                    return Pair.of(OrderAckStatus.NACK, billing.getBalance());
                                }

                                billing = billingService.removeFundsFromUser(email, totalPrice).block();
                                assert billing != null;
                                return Pair.of(OrderAckStatus.ACK, billing.getBalance());
                            })
                            .doOnNext(pair -> log.info(
                                    "Order {} {} total price {} {}",
                                    orderId, email, totalPrice, pair.getFirst()))
                            .onErrorResume(e -> {
                                log.warn(
                                        "Accepted order {} to non existent billing {}",
                                        orderId,
                                        email
                                );

                                return Mono.empty();
                            })
                            .blockOptional();

                    if (ackStatusBalancePairOptional.isEmpty())
                        return KeyValue.pair(null, null);

                    var ackStatusBalancePair = ackStatusBalancePairOptional.get();

                    var outputKey = new KeyOrderAcknowledgment();
                    var outputValue = new ValueOrderAcknowledgment();
                    outputKey.setUserAddress(email);
                    outputValue.setOrderId(orderId);
                    outputValue.setOrderTotalPrice(totalPrice);
                    outputValue.setEvent(ackStatusBalancePair.getFirst());
                    outputValue.setUserBalance(ackStatusBalancePair.getSecond());

                    return KeyValue.pair(outputKey, outputValue);
                })
                .filterNot(((key, value) -> key == null && value == null))
                .to("order-acknowledgment",
                        Produced.with(KEY_ORDER_ACKNOWLEDGMENT_SERDE, VALUE_ORDER_ACKNOWLEDGMENT_SERDE));
    }
}
