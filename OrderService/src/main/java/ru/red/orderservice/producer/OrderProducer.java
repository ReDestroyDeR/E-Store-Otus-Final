package ru.red.orderservice.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.red.orderservice.domain.Order;
import streamprocessing.avro.KeyOrderManipulation;
import streamprocessing.avro.ValueOrderManipulation;

@Component
public class OrderProducer {
    private final KafkaTemplate<KeyOrderManipulation, ValueOrderManipulation> kafka;

    @Autowired
    public OrderProducer(KafkaTemplate<KeyOrderManipulation, ValueOrderManipulation> kafka) {
        this.kafka = kafka;
    }

    public Flux<SendResult<KeyOrderManipulation, ValueOrderManipulation>> sendCreatedMessage(Flux<Order> order) {
        return order.flatMap(o -> Mono.fromFuture(
                kafka.send("order-manipulation",
                                new KeyOrderManipulation(o.getUserAddress()),
                                new ValueOrderManipulation(o.getId(), o.getTotalPrice()))
                        .completable()
        ));
    }
}
