package ru.red.orderservice.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.red.order.avro.OrderCancelled;
import ru.red.order.avro.OrderManipulationKey;
import ru.red.order.avro.OrderPlaced;
import ru.red.orderservice.domain.Order;

@Component
public class OrderProducer {
    private final KafkaTemplate<OrderManipulationKey, OrderPlaced> placedTemplate;
    private final KafkaTemplate<OrderManipulationKey, OrderCancelled> cancelledTemplate;

    @Autowired
    public OrderProducer(KafkaTemplate<OrderManipulationKey, OrderPlaced> placedTemplate,
                         KafkaTemplate<OrderManipulationKey, OrderCancelled> cancelledTemplate) {
        this.placedTemplate = placedTemplate;
        this.cancelledTemplate = cancelledTemplate;
    }

    public Flux<SendResult<OrderManipulationKey, OrderPlaced>> sendCreatedMessage(Flux<Order> order) {
        return order.flatMap(o -> Mono.fromFuture(
                placedTemplate.send("order-manipulation",
                                new OrderManipulationKey(o.getId(), o.getUserId()),
                                new OrderPlaced(o.getTotalPrice(), o.getItems()))
                        .completable()
        ));
    }
}
