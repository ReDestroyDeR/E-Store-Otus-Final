package ru.red.notificationservice.consumer;

import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;
import ru.red.notificationservice.factory.NotificationDTOFactory;
import ru.red.notificationservice.service.NotificationService;
import streamprocessing.avro.KeyOrderAcknowledgment;
import streamprocessing.avro.ValueOrderAcknowledgment;

@Log4j2
@Component
public class OrderAcknowledgmentConsumer {
    private final NotificationService service;
    private final NotificationDTOFactory dtoFactory;

    @Autowired
    public OrderAcknowledgmentConsumer(NotificationService service,
                                       NotificationDTOFactory dtoFactory) {
        this.service = service;
        this.dtoFactory = dtoFactory;
    }

    @KafkaListener(topics = "order-acknowledgment", groupId = "notification-service")
    public void listenAcknowledgments(ConsumerRecord<KeyOrderAcknowledgment, ValueOrderAcknowledgment> record) {
        log.info("Got message in group notification-service: {} {}",
                record.key().getUserAddress(),
                record.value().getEvent());
        var userAddress = record.key().getUserAddress().toString();
        var value = record.value();
        var dto = dtoFactory.createDto(userAddress, value);
        service.createNotification(dto)
                .publishOn(Schedulers.boundedElastic())
                .subscribe(
                        s -> log.info(
                                "Successfully created notification for {} {}",
                                userAddress,
                                value.getEvent().toString()
                        ),
                        e -> log.warn("Failed creating notification for {} {} {}",
                                userAddress,
                                value.getEvent().toString(),
                                e.getMessage()
                        )
                );
    }
}
