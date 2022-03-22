package ru.red.orderservice.producer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;
import ru.red.orderservice.domain.Order;
import streamprocessing.avro.KeyOrderManipulation;
import streamprocessing.avro.ValueOrderManipulation;

import javax.annotation.PreDestroy;

@Component
public class OrderProducer {
    private final KafkaSender<KeyOrderManipulation, ValueOrderManipulation> producer;

    @Autowired
    public OrderProducer(SenderOptions<KeyOrderManipulation, ValueOrderManipulation> senderOptions) {
        this.producer = KafkaSender.create(senderOptions);
    }

    public Flux<SenderResult<String>> sendCreatedMessage(Flux<Order> order) {
        return producer.send(order.map(o ->
                        SenderRecord.create(
                                new ProducerRecord<KeyOrderManipulation, ValueOrderManipulation>
                                        ("order-manipulation",
                                                new KeyOrderManipulation(o.getUserAddress()),
                                                new ValueOrderManipulation(o.getId(), o.getTotalPrice())
                                        ),
                                "Created " + o.getId()
                        )
                )
        );
    }

    @PreDestroy
    public void closeProducer() {
        producer.close();
    }
}
