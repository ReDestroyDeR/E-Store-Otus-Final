package ru.red.deliveryservice.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.red.billing.avro.OrderAcknowledgmentKey;
import ru.red.delivery.avro.DeliveryFinished;
import ru.red.delivery.avro.DeliveryStarted;

@Component
public class DeliveryProducer {
    private static final String TOPIC_NAME = "delivery";
    private final KafkaTemplate<OrderAcknowledgmentKey, DeliveryStarted> deliveryStartedKafkaTemplate;
    private final KafkaTemplate<OrderAcknowledgmentKey, DeliveryFinished> deliveryFinishedKafkaTemplate;

    @Autowired
    public DeliveryProducer(KafkaTemplate<OrderAcknowledgmentKey, DeliveryStarted> deliveryStartedKafkaTemplate,
                            KafkaTemplate<OrderAcknowledgmentKey, DeliveryFinished> deliveryFinishedKafkaTemplate) {
        this.deliveryStartedKafkaTemplate = deliveryStartedKafkaTemplate;
        this.deliveryFinishedKafkaTemplate = deliveryFinishedKafkaTemplate;
    }

    public Mono<SendResult<OrderAcknowledgmentKey, DeliveryStarted>> startDelivery(String orderId, Long userId) {
        return Mono.fromFuture(
                deliveryStartedKafkaTemplate.send(TOPIC_NAME,
                        new OrderAcknowledgmentKey(orderId, userId),
                        new DeliveryStarted()
                ).completable()
        );
    }

    public Mono<SendResult<OrderAcknowledgmentKey, DeliveryFinished>> finishDelivery(String orderId, Long userId) {
        return Mono.fromFuture(
                deliveryFinishedKafkaTemplate.send(TOPIC_NAME,
                        new OrderAcknowledgmentKey(orderId, userId),
                        new DeliveryFinished()
                ).completable()
        );
    }
}
